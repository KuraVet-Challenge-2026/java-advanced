package br.com.fiap.kuravet.controller.api;

import br.com.fiap.kuravet.dto.tutor.TutorRequestDTO;
import br.com.fiap.kuravet.dto.tutor.TutorResponseDTO;
import br.com.fiap.kuravet.model.Tutor;
import br.com.fiap.kuravet.security.UsuarioPrincipal;
import br.com.fiap.kuravet.service.TutorService;
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
 * CRUD de TUTOR consumido pelo app mobile (React Native). Trafega apenas
 * DTOs ({@link TutorRequestDTO}/{@link TutorResponseDTO}) para nao expor a
 * entidade JPA {@link Tutor} diretamente ao cliente. Leitura, edicao e
 * exclusao sao restritas ao proprio dono quando quem chama e TUTOR (ver
 * {@code TutorService}); VETERINARIO acessa qualquer tutor.
 */
@RestController
@RequestMapping("/api/tutores")
public class TutorController {

    private final TutorService tutorService;

    public TutorController(TutorService tutorService) {
        this.tutorService = tutorService;
    }

    @GetMapping
    public ResponseEntity<List<TutorResponseDTO>> listar(@AuthenticationPrincipal UsuarioPrincipal principal) {
        List<TutorResponseDTO> tutores = tutorService.listar(principal).stream()
                .map(TutorResponseDTO::fromEntity)
                .toList();

        return ResponseEntity.ok(tutores);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TutorResponseDTO> buscarPorId(@AuthenticationPrincipal UsuarioPrincipal principal,
                                                          @PathVariable Long id) {
        Tutor tutor = tutorService.buscarPorId(principal, id);
        return ResponseEntity.ok(TutorResponseDTO.fromEntity(tutor));
    }

    @PostMapping
    public ResponseEntity<TutorResponseDTO> cadastrar(@RequestBody @Valid TutorRequestDTO dto) {
        Tutor tutorSalvo = tutorService.criar(dto);
        return ResponseEntity.created(URI.create("/api/tutores/" + tutorSalvo.getIdTutor()))
                .body(TutorResponseDTO.fromEntity(tutorSalvo));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TutorResponseDTO> atualizar(@AuthenticationPrincipal UsuarioPrincipal principal,
                                                        @PathVariable Long id, @RequestBody @Valid TutorRequestDTO dto) {
        Tutor tutorAtualizado = tutorService.atualizar(principal, id, dto);
        return ResponseEntity.ok(TutorResponseDTO.fromEntity(tutorAtualizado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@AuthenticationPrincipal UsuarioPrincipal principal, @PathVariable Long id) {
        tutorService.excluir(principal, id);
        return ResponseEntity.noContent().build();
    }
}
