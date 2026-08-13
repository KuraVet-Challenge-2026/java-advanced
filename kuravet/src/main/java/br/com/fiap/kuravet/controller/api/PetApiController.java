package br.com.fiap.kuravet.controller.api;

import br.com.fiap.kuravet.dto.PetRequestDTO;
import br.com.fiap.kuravet.dto.PetResponseDTO;
import br.com.fiap.kuravet.exception.PetNaoEncontradoException;
import br.com.fiap.kuravet.exception.TutorNaoEncontradoException;
import br.com.fiap.kuravet.model.Pet;
import br.com.fiap.kuravet.model.Tutor;
import br.com.fiap.kuravet.repository.PetRepository;
import br.com.fiap.kuravet.repository.TutorRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

/**
 * CRUD de PET consumido pelo app mobile (React Native). Trafega apenas
 * DTOs ({@link PetRequestDTO}/{@link PetResponseDTO}) para nao expor a
 * entidade JPA {@link Pet} diretamente (evita LazyInitializationException
 * e recursao bidirecional no JSON).
 */
@RestController
@RequestMapping("/api/pets")
public class PetApiController {

    private final PetRepository petRepository;
    private final TutorRepository tutorRepository;

    public PetApiController(PetRepository petRepository, TutorRepository tutorRepository) {
        this.petRepository = petRepository;
        this.tutorRepository = tutorRepository;
    }

    @GetMapping
    public ResponseEntity<List<PetResponseDTO>> listar() {
        List<PetResponseDTO> pets = petRepository.findAll().stream()
                .map(PetResponseDTO::fromEntity)
                .toList();

        return ResponseEntity.ok(pets);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PetResponseDTO> buscarPorId(@PathVariable Long id) {
        Pet pet = buscarPetOuLancar(id);
        return ResponseEntity.ok(PetResponseDTO.fromEntity(pet));
    }

    @PostMapping
    public ResponseEntity<PetResponseDTO> cadastrar(@RequestBody @Valid PetRequestDTO dto) {
        Tutor tutor = buscarTutorOuLancar(dto.idTutor());

        Pet pet = Pet.builder()
                .nome(dto.nome())
                .especie(dto.especie())
                .raca(dto.raca())
                .dataNascimento(dto.dataNascimento())
                .sexo(dto.sexo())
                .tutor(tutor)
                .build();

        Pet petSalvo = petRepository.save(pet);

        return ResponseEntity.created(URI.create("/api/pets/" + petSalvo.getIdPet()))
                .body(PetResponseDTO.fromEntity(petSalvo));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PetResponseDTO> atualizar(@PathVariable Long id, @RequestBody @Valid PetRequestDTO dto) {
        Pet pet = buscarPetOuLancar(id);
        Tutor tutor = buscarTutorOuLancar(dto.idTutor());

        pet.setNome(dto.nome());
        pet.setEspecie(dto.especie());
        pet.setRaca(dto.raca());
        pet.setDataNascimento(dto.dataNascimento());
        pet.setSexo(dto.sexo());
        pet.setTutor(tutor);

        Pet petAtualizado = petRepository.save(pet);

        return ResponseEntity.ok(PetResponseDTO.fromEntity(petAtualizado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        Pet pet = buscarPetOuLancar(id);
        petRepository.delete(pet);

        return ResponseEntity.noContent().build();
    }

    private Pet buscarPetOuLancar(Long id) {
        return petRepository.findById(id)
                .orElseThrow(() -> new PetNaoEncontradoException(id));
    }

    private Tutor buscarTutorOuLancar(Long idTutor) {
        return tutorRepository.findById(idTutor)
                .orElseThrow(() -> new TutorNaoEncontradoException(idTutor));
    }
}
