package br.com.fiap.kuravet.service;

import br.com.fiap.kuravet.dto.pet.PetRequestDTO;
import br.com.fiap.kuravet.exception.PetNaoEncontradoException;
import br.com.fiap.kuravet.exception.RegraDeNegocioException;
import br.com.fiap.kuravet.exception.TutorNaoEncontradoException;
import br.com.fiap.kuravet.model.Pet;
import br.com.fiap.kuravet.model.Tutor;
import br.com.fiap.kuravet.repository.ConsultaRepository;
import br.com.fiap.kuravet.repository.PetRepository;
import br.com.fiap.kuravet.repository.TutorRepository;
import br.com.fiap.kuravet.security.UsuarioPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Regras de negocio do PET.
 *
 * <p>O dono do pet nunca vem do corpo da requisicao. Existem duas origens
 * legitimas: no app mobile e sempre o TUTOR autenticado; no portal e o tutor
 * escolhido pelo VETERINARIO no formulario. As duas convergem para
 * {@link #cadastrar(Long, PetRequestDTO)}.
 */
@Service
public class PetService {

    private final PetRepository petRepository;
    private final TutorRepository tutorRepository;
    private final ConsultaRepository consultaRepository;

    public PetService(PetRepository petRepository,
                      TutorRepository tutorRepository,
                      ConsultaRepository consultaRepository) {
        this.petRepository = petRepository;
        this.tutorRepository = tutorRepository;
        this.consultaRepository = consultaRepository;
    }

    public List<Pet> listar(UsuarioPrincipal principal) {
        if (principal.isTutor()) {
            return petRepository.findByTutorIdTutor(principal.getIdTutor());
        }
        return petRepository.findAll();
    }

    /**
     * Um pet de outro tutor "nao existe" para o TUTOR autenticado (404 em vez
     * de 403), para nao revelar a existencia de registros de terceiros.
     */
    public Pet buscarPorId(UsuarioPrincipal principal, Long id) {
        Pet pet = petRepository.findById(id)
                .orElseThrow(() -> new PetNaoEncontradoException(id));

        if (principal.isTutor() && !pet.getTutor().getIdTutor().equals(principal.getIdTutor())) {
            throw new PetNaoEncontradoException(id);
        }

        return pet;
    }

    /** CREATE pelo app mobile: o dono e o proprio tutor autenticado. */
    @Transactional
    public Pet criar(UsuarioPrincipal principal, PetRequestDTO dto) {
        return cadastrar(principal.getIdTutor(), dto);
    }

    /** CREATE pelo portal: o veterinario escolhe o tutor dono. */
    @Transactional
    public Pet cadastrar(Long idTutor, PetRequestDTO dto) {
        Pet pet = Pet.builder()
                .tutor(buscarTutorOuLancar(idTutor))
                .build();

        aplicarDados(pet, dto);
        return petRepository.save(pet);
    }

    /** UPDATE pelo app mobile: o dono nao muda. */
    @Transactional
    public Pet atualizar(UsuarioPrincipal principal, Long id, PetRequestDTO dto) {
        Pet pet = buscarPorId(principal, id);
        aplicarDados(pet, dto);
        return petRepository.save(pet);
    }

    /** UPDATE pelo portal: o veterinario pode ate transferir o pet de tutor. */
    @Transactional
    public Pet atualizarComTutor(UsuarioPrincipal principal, Long id, Long idTutor, PetRequestDTO dto) {
        Pet pet = buscarPorId(principal, id);
        pet.setTutor(buscarTutorOuLancar(idTutor));
        aplicarDados(pet, dto);
        return petRepository.save(pet);
    }

    /**
     * DELETE. Um pet com historico clinico nao pode sumir: alem de violar a FK
     * KV_FK_CONS_PET, apagaria o rastro de atendimentos ja realizados.
     */
    @Transactional
    public void excluir(UsuarioPrincipal principal, Long id) {
        Pet pet = buscarPorId(principal, id);

        if (consultaRepository.existsByPetIdPet(id)) {
            throw new RegraDeNegocioException(
                    "O pet " + pet.getNome() + " possui consultas registradas e nao pode ser excluido.");
        }

        petRepository.delete(pet);
    }

    private void aplicarDados(Pet pet, PetRequestDTO dto) {
        pet.setNome(dto.nome());
        pet.setEspecie(dto.especie());
        pet.setRaca(dto.raca());
        pet.setDataNascimento(dto.dataNascimento());
        pet.setSexo(dto.sexo());
    }

    private Tutor buscarTutorOuLancar(Long idTutor) {
        return tutorRepository.findById(idTutor)
                .orElseThrow(() -> new TutorNaoEncontradoException(idTutor));
    }
}