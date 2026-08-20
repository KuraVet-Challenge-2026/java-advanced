package br.com.fiap.kuravet.controller.api;

import br.com.fiap.kuravet.dto.ConsultaResponseDTO;
import br.com.fiap.kuravet.dto.DiagnosticoRequestDTO;
import br.com.fiap.kuravet.model.Consulta;
import br.com.fiap.kuravet.service.ConsultaService;
import br.com.fiap.kuravet.repository.ConsultaRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/consultas")
@CrossOrigin(origins = "*")
public class ConsultaApiController {

    private final ConsultaService consultaService;
    private final ConsultaRepository consultaRepository;

    // O Spring injeta o Service e o Repository automaticamente aqui
    public ConsultaApiController(ConsultaService consultaService, ConsultaRepository consultaRepository) {
        this.consultaService = consultaService;
        this.consultaRepository = consultaRepository;
    }

    // Agora buscamos direto do banco usando o findAll() nativo do JPA
    @GetMapping
    public ResponseEntity<List<ConsultaResponseDTO>> listar() {
        List<ConsultaResponseDTO> consultas = consultaRepository.findAll().stream()
                .map(ConsultaResponseDTO::fromEntity)
                .toList();
        return ResponseEntity.ok(consultas);
    }

    @PatchMapping("/{id}/diagnostico")
    public ResponseEntity<ConsultaResponseDTO> emitirDiagnostico(@PathVariable Long id,
                                                                 @RequestBody @Valid DiagnosticoRequestDTO dto) {
        Consulta consulta = consultaService.realizarConsultaComDiagnostico(id, dto.diagnostico());
        return ResponseEntity.ok(ConsultaResponseDTO.fromEntity(consulta));
    }
}