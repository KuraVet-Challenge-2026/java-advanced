package br.com.fiap.kuravet.controller.api;

import br.com.fiap.kuravet.dto.consulta.ConsultaRequestDTO;
import br.com.fiap.kuravet.dto.consulta.ConsultaResponseDTO;
import br.com.fiap.kuravet.dto.consulta.DiagnosticoRequestDTO;
import br.com.fiap.kuravet.model.Consulta;
import br.com.fiap.kuravet.security.UsuarioPrincipal;
import br.com.fiap.kuravet.service.ConsultaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

/**
 * CRUD de CONSULTA consumido pelo app mobile (React Native). Trafega apenas
 * DTOs, delegando as regras de negocio ao {@link ConsultaService}. TUTOR so
 * enxerga/altera consultas dos proprios pets; VETERINARIO tem acesso irrestrito
 * e e o unico que pode emitir diagnostico (ver {@code SecurityConfig}).
 */
@RestController
@RequestMapping("/api/consultas")
public class ConsultaController {

    private final ConsultaService consultaService;

    public ConsultaController(ConsultaService consultaService) {
        this.consultaService = consultaService;
    }

    @GetMapping
    public ResponseEntity<List<ConsultaResponseDTO>> listar(@AuthenticationPrincipal UsuarioPrincipal principal) {
        List<ConsultaResponseDTO> consultas = consultaService.listar(principal).stream()
                .map(ConsultaResponseDTO::fromEntity)
                .toList();

        return ResponseEntity.ok(consultas);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ConsultaResponseDTO> buscarPorId(@AuthenticationPrincipal UsuarioPrincipal principal,
                                                             @PathVariable Long id) {
        Consulta consulta = consultaService.buscarPorId(principal, id);
        return ResponseEntity.ok(ConsultaResponseDTO.fromEntity(consulta));
    }

    @PostMapping
    public ResponseEntity<ConsultaResponseDTO> agendar(@AuthenticationPrincipal UsuarioPrincipal principal,
                                                         @RequestBody @Valid ConsultaRequestDTO dto) {
        Consulta consultaSalva = consultaService.criar(principal, dto);
        return ResponseEntity.created(URI.create("/api/consultas/" + consultaSalva.getIdConsulta()))
                .body(ConsultaResponseDTO.fromEntity(consultaSalva));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ConsultaResponseDTO> atualizar(@AuthenticationPrincipal UsuarioPrincipal principal,
                                                           @PathVariable Long id,
                                                           @RequestBody @Valid ConsultaRequestDTO dto) {
        Consulta consultaAtualizada = consultaService.atualizar(principal, id, dto);
        return ResponseEntity.ok(ConsultaResponseDTO.fromEntity(consultaAtualizada));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@AuthenticationPrincipal UsuarioPrincipal principal, @PathVariable Long id) {
        consultaService.excluir(principal, id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Encerra o atendimento aplicando o fluxo de negocio "Realizacao de
     * Consulta e Emissao de Diagnostico" implementado em
     * {@link ConsultaService#realizarConsultaComDiagnostico(Long, String)}.
     */
    @PatchMapping("/{id}/diagnostico")
    public ResponseEntity<ConsultaResponseDTO> emitirDiagnostico(@PathVariable Long id,
                                                                   @RequestBody @Valid DiagnosticoRequestDTO dto) {
        Consulta consulta = consultaService.realizarConsultaComDiagnostico(id, dto.diagnostico());
        return ResponseEntity.ok(ConsultaResponseDTO.fromEntity(consulta));
    }
}
