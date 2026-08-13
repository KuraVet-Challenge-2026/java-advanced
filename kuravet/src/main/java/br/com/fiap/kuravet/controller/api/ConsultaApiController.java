package br.com.fiap.kuravet.controller.api;

import br.com.fiap.kuravet.dto.ConsultaResponseDTO;
import br.com.fiap.kuravet.dto.DiagnosticoRequestDTO;
import br.com.fiap.kuravet.model.Consulta;
import br.com.fiap.kuravet.service.ConsultaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints de CONSULTA consumidos pelo app mobile (React Native). Trafega
 * apenas DTOs, delegando as regras de negocio ao {@link ConsultaService}.
 */
@RestController
@RequestMapping("/api/consultas")
public class ConsultaApiController {

    private final ConsultaService consultaService;

    public ConsultaApiController(ConsultaService consultaService) {
        this.consultaService = consultaService;
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
