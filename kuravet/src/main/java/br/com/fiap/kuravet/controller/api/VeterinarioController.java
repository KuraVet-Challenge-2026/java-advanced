package br.com.fiap.kuravet.controller.api;

import br.com.fiap.kuravet.dto.veterinario.VeterinarioResponseDTO;
import br.com.fiap.kuravet.model.Veterinario;
import br.com.fiap.kuravet.service.VeterinarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/veterinarios")
public class VeterinarioController {

    private final VeterinarioService veterinarioService;

    public VeterinarioController(VeterinarioService veterinarioService) {
        this.veterinarioService = veterinarioService;
    }

    @GetMapping
    public ResponseEntity<List<VeterinarioResponseDTO>> listar() {
        List<VeterinarioResponseDTO> veterinarios = veterinarioService.listar().stream()
                .map(VeterinarioResponseDTO::fromEntity)
                .toList();

        return ResponseEntity.ok(veterinarios);
    }

    @GetMapping("/{id}")
    public ResponseEntity<VeterinarioResponseDTO> buscarPorId(@PathVariable Long id) {
        Veterinario veterinario = veterinarioService.buscarPorId(id);
        return ResponseEntity.ok(VeterinarioResponseDTO.fromEntity(veterinario));
    }
}
