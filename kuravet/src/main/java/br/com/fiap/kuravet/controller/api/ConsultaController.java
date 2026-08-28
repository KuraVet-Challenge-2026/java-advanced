package br.com.fiap.kuravet.controller.api;

import br.com.fiap.kuravet.dto.consulta.ConsultaRequestDTO;
import br.com.fiap.kuravet.dto.consulta.ConsultaResponseDTO;
import br.com.fiap.kuravet.dto.consulta.DiagnosticoRequestDTO;
import br.com.fiap.kuravet.dto.consulta.RecusaRequestDTO;
import br.com.fiap.kuravet.enums.StatusConsulta;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

/**
 * Endpoints de CONSULTA consumidos pelo app mobile. Trafega apenas DTOs e
 * delega as regras ao {@link ConsultaService}. As permissoes por perfil estao
 * declaradas em {@code SecurityConfig}.
 */
@RestController
@RequestMapping("/api/consultas")
public class ConsultaController {

    private final ConsultaService consultaService;


    public ConsultaController(ConsultaService consultaService) {
        this.consultaService = consultaService;
    }

    @GetMapping
    public ResponseEntity<List<ConsultaResponseDTO>> listar(@AuthenticationPrincipal UsuarioPrincipal principal,
                                                            @RequestParam(required = false) StatusConsulta status) {
        List<Consulta> consultas = (status == null)
                ? consultaService.listar(principal)
                : consultaService.listarPorStatus(principal, status);

        return ResponseEntity.ok(consultas.stream().map(ConsultaResponseDTO::fromEntity).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ConsultaResponseDTO> buscarPorId(@AuthenticationPrincipal UsuarioPrincipal principal,
                                                           @PathVariable Long id) {
        return ResponseEntity.ok(ConsultaResponseDTO.fromEntity(consultaService.buscarPorId(principal, id)));
    }

    /** Fluxo 1, passo 1: o tutor solicita a teleconsulta pelo app. */
    @PostMapping("/solicitacoes")
    public ResponseEntity<ConsultaResponseDTO> solicitar(@AuthenticationPrincipal UsuarioPrincipal principal,
                                                         @RequestBody @Valid ConsultaRequestDTO dto) {
        Consulta solicitacao = consultaService.solicitarTeleconsulta(principal, dto);
        return ResponseEntity.created(URI.create("/api/consultas/" + solicitacao.getIdConsulta()))
                .body(ConsultaResponseDTO.fromEntity(solicitacao));
    }

    /** Fluxo 1, passo 2a: o veterinario aprova. */
    @PatchMapping("/{id}/aprovacao")
    public ResponseEntity<ConsultaResponseDTO> aprovar(@PathVariable Long id) {
        return ResponseEntity.ok(ConsultaResponseDTO.fromEntity(consultaService.aprovarSolicitacao(id)));
    }

    /** Fluxo 1, passo 2b: o veterinario recusa, com justificativa. */
    @PatchMapping("/{id}/recusa")
    public ResponseEntity<ConsultaResponseDTO> recusar(@PathVariable Long id,
                                                       @RequestBody @Valid RecusaRequestDTO dto) {
        return ResponseEntity.ok(ConsultaResponseDTO.fromEntity(
                consultaService.recusarSolicitacao(id, dto.getMotivo())));
    }

    /** Fluxo 2: o veterinario encerra o atendimento emitindo o diagnostico. */
    @PatchMapping("/{id}/diagnostico")
    public ResponseEntity<ConsultaResponseDTO> emitirDiagnostico(@PathVariable Long id,
                                                                 @RequestBody @Valid DiagnosticoRequestDTO dto) {
        return ResponseEntity.ok(ConsultaResponseDTO.fromEntity(
                consultaService.realizarConsultaComDiagnostico(id, dto.getDiagnostico())));
    }

    @PatchMapping("/{id}/cancelamento")
    public ResponseEntity<ConsultaResponseDTO> cancelar(@AuthenticationPrincipal UsuarioPrincipal principal,
                                                        @PathVariable Long id) {
        return ResponseEntity.ok(ConsultaResponseDTO.fromEntity(consultaService.cancelar(principal, id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ConsultaResponseDTO> atualizar(@AuthenticationPrincipal UsuarioPrincipal principal,
                                                         @PathVariable Long id,
                                                         @RequestBody @Valid ConsultaRequestDTO dto) {
        return ResponseEntity.ok(ConsultaResponseDTO.fromEntity(
                consultaService.atualizar(principal, id, dto)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@AuthenticationPrincipal UsuarioPrincipal principal, @PathVariable Long id) {
        consultaService.excluir(principal, id);
        return ResponseEntity.noContent().build();
    }
}