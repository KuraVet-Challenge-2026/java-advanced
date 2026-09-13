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

    public Pet buscarPorId(UsuarioPrincipal principal, Long id) {
        Pet pet = petRepository.findById(id)
                .orElseThrow(() -> new PetNaoEncontradoException(id));

        if (principal.isTutor() && !pet.getTutor().getIdTutor().equals(principal.getIdTutor())) {
            throw new PetNaoEncontradoException(id);
        }

        return pet;
    }

    @Transactional
    public Pet criar(UsuarioPrincipal principal, PetRequestDTO dto) {
        return cadastrar(principal.getIdTutor(), dto);
    }

    @Transactional
    public Pet cadastrar(Long idTutor, PetRequestDTO dto) {
        Pet pet = Pet.builder()
                .tutor(buscarTutorOuLancar(idTutor))
                .build();

        aplicarDados(pet, dto);
        return petRepository.save(pet);
    }

    @Transactional
    public Pet atualizar(UsuarioPrincipal principal, Long id, PetRequestDTO dto) {
        Pet pet = buscarPorId(principal, id);
        aplicarDados(pet, dto);
        return petRepository.save(pet);
    }

    @Transactional
    public Pet atualizarComTutor(UsuarioPrincipal principal, Long id, Long idTutor, PetRequestDTO dto) {
        Pet pet = buscarPorId(principal, id);
        pet.setTutor(buscarTutorOuLancar(idTutor));
        aplicarDados(pet, dto);
        return petRepository.save(pet);
    }

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