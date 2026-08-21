package br.com.fiap.kuravet.service;

import br.com.fiap.kuravet.dto.pet.PetRequestDTO;
import br.com.fiap.kuravet.exception.PetNaoEncontradoException;
import br.com.fiap.kuravet.exception.TutorNaoEncontradoException;
import br.com.fiap.kuravet.model.Pet;
import br.com.fiap.kuravet.model.Tutor;
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

    public PetService(PetRepository petRepository, TutorRepository tutorRepository) {
        this.petRepository = petRepository;
        this.tutorRepository = tutorRepository;
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

    @Transactional
    public Pet criar(UsuarioPrincipal principal, PetRequestDTO dto) {
        Tutor tutor = buscarTutorOuLancar(principal.getIdTutor());

        Pet pet = Pet.builder()
                .nome(dto.nome())
                .especie(dto.especie())
                .raca(dto.raca())
                .dataNascimento(dto.dataNascimento())
                .sexo(dto.sexo())
                .tutor(tutor)
                .build();

        return petRepository.save(pet);
    }

    @Transactional
    public Pet atualizar(UsuarioPrincipal principal, Long id, PetRequestDTO dto) {
        Pet pet = buscarPorId(principal, id);

        pet.setNome(dto.nome());
        pet.setEspecie(dto.especie());
        pet.setRaca(dto.raca());
        pet.setDataNascimento(dto.dataNascimento());
        pet.setSexo(dto.sexo());

        return petRepository.save(pet);
    }

    @Transactional
    public void excluir(UsuarioPrincipal principal, Long id) {
        Pet pet = buscarPorId(principal, id);
        petRepository.delete(pet);
    }

    private Tutor buscarTutorOuLancar(Long idTutor) {
        return tutorRepository.findById(idTutor)
                .orElseThrow(() -> new TutorNaoEncontradoException(idTutor));
    }
}
