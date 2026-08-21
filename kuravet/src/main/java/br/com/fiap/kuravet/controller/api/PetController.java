package br.com.fiap.kuravet.controller.api;

import br.com.fiap.kuravet.dto.pet.PetRequestDTO;
import br.com.fiap.kuravet.dto.pet.PetResponseDTO;
import br.com.fiap.kuravet.model.Pet;
import br.com.fiap.kuravet.security.UsuarioPrincipal;
import br.com.fiap.kuravet.service.PetService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
 * entidade JPA {@link Pet} diretamente ao cliente. Escrita e restrita a
 * TUTOR (ver {@code SecurityConfig}); o dono do pet e sempre o autenticado.
 */
@RestController
@RequestMapping("/api/pets")
public class PetController {

    private final PetService petService;

    public PetController(PetService petService) {
        this.petService = petService;
    }

    @GetMapping
    public ResponseEntity<List<PetResponseDTO>> listar(@AuthenticationPrincipal UsuarioPrincipal principal) {
        List<PetResponseDTO> pets = petService.listar(principal).stream()
                .map(PetResponseDTO::fromEntity)
                .toList();

        return ResponseEntity.ok(pets);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PetResponseDTO> buscarPorId(@AuthenticationPrincipal UsuarioPrincipal principal,
                                                        @PathVariable Long id) {
        Pet pet = petService.buscarPorId(principal, id);
        return ResponseEntity.ok(PetResponseDTO.fromEntity(pet));
    }

    @PostMapping
    public ResponseEntity<PetResponseDTO> cadastrar(@AuthenticationPrincipal UsuarioPrincipal principal,
                                                      @RequestBody @Valid PetRequestDTO dto) {
        Pet petSalvo = petService.criar(principal, dto);
        return ResponseEntity.created(URI.create("/api/pets/" + petSalvo.getIdPet()))
                .body(PetResponseDTO.fromEntity(petSalvo));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PetResponseDTO> atualizar(@AuthenticationPrincipal UsuarioPrincipal principal,
                                                      @PathVariable Long id, @RequestBody @Valid PetRequestDTO dto) {
        Pet petAtualizado = petService.atualizar(principal, id, dto);
        return ResponseEntity.ok(PetResponseDTO.fromEntity(petAtualizado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@AuthenticationPrincipal UsuarioPrincipal principal, @PathVariable Long id) {
        petService.excluir(principal, id);
        return ResponseEntity.noContent().build();
    }
}
