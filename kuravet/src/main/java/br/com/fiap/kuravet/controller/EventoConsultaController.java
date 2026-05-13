package br.com.fiap.kuravet.controller;
import java.util.List;

import br.com.fiap.kuravet.dto.EventoConsultaRequestDTO;
import br.com.fiap.kuravet.dto.EventoConsultaResponseDTO;
import br.com.fiap.kuravet.service.EventoConsultaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/eventos")
public class EventoConsultaController {

    @Autowired
    private EventoConsultaService eventoService;

    @Operation(summary = "Registrar um Evento de Consulta", description = "Cria um registro clínico contendo diagnóstico e tratamento para um Pet específico.")
    @PostMapping
    public ResponseEntity<EventoConsultaResponseDTO> registrar(@RequestBody @Valid EventoConsultaRequestDTO dto) {
        return ResponseEntity.ok(eventoService.salvar(dto));
    }

    @Operation(summary = "Atualizar um Evento de Consulta", description = "Modifica o diagnóstico ou tratamento de um evento existente.")
    @PutMapping("/{id}")
    public ResponseEntity<EventoConsultaResponseDTO> atualizar(@PathVariable Long id, @RequestBody @Valid EventoConsultaRequestDTO dto) {
        return ResponseEntity.ok(eventoService.atualizar(id, dto));
    }

    @Operation(summary = "Excluir um Evento de Consulta", description = "Deleta o registro de uma consulta do banco de dados.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        eventoService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Listar todos os Eventos de Consulta", description = "Retorna uma lista com todos os registros clínicos de consultas cadastradas.")
    @GetMapping
    public ResponseEntity<List<EventoConsultaResponseDTO>> listarTodos() {
        return ResponseEntity.ok(eventoService.listarTodos());
    }

}