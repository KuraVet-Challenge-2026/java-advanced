package br.com.fiap.kuravet.controller.api;

import br.com.fiap.kuravet.dto.triagem.TriagemRequestDTO;
import br.com.fiap.kuravet.dto.triagem.TriagemResponseDTO;
import br.com.fiap.kuravet.model.Triagem;
import br.com.fiap.kuravet.security.UsuarioPrincipal;
import br.com.fiap.kuravet.service.TriagemService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/triagens")
public class TriagemController {

    private final TriagemService triagemService;

    public TriagemController(TriagemService triagemService) {
        this.triagemService = triagemService;
    }

    @PostMapping
    public ResponseEntity<TriagemResponseDTO> realizar(@AuthenticationPrincipal UsuarioPrincipal principal,
                                                       @RequestBody @Valid TriagemRequestDTO dto) {
        Triagem triagem = triagemService.realizar(principal, dto);
        return ResponseEntity.created(URI.create("/api/triagens/" + triagem.getIdTriagem()))
                .body(TriagemResponseDTO.fromEntity(triagem));
    }

    @GetMapping
    public ResponseEntity<List<TriagemResponseDTO>> listar(@AuthenticationPrincipal UsuarioPrincipal principal) {
        return ResponseEntity.ok(triagemService.listar(principal).stream()
                .map(TriagemResponseDTO::fromEntity)
                .toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TriagemResponseDTO> buscarPorId(@AuthenticationPrincipal UsuarioPrincipal principal,
                                                          @PathVariable Long id) {
        return ResponseEntity.ok(TriagemResponseDTO.fromEntity(triagemService.buscarPorId(principal, id)));
    }
}
