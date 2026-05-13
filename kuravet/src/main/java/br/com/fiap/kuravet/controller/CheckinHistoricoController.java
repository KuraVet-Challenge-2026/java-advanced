package br.com.fiap.kuravet.controller;
import java.util.List;

import br.com.fiap.kuravet.dto.CheckinHistoricoRequestDTO;
import br.com.fiap.kuravet.dto.CheckinHistoricoResponseDTO;
import br.com.fiap.kuravet.service.CheckinHistoricoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/checkins")
public class CheckinHistoricoController {

    @Autowired
    private CheckinHistoricoService checkinService;

    @Operation(summary = "Registrar Check-in de um Pet", description = "Cria um registro de entrada de um Pet na clínica, com espaço para observações.")
    @PostMapping
    public ResponseEntity<CheckinHistoricoResponseDTO> registrar(@RequestBody @Valid CheckinHistoricoRequestDTO dto) {
        return ResponseEntity.ok(checkinService.salvar(dto));
    }

    @Operation(summary = "Atualizar Observações do Check-in", description = "Altera as observações de um check-in já realizado.")
    @PutMapping("/{id}")
    public ResponseEntity<CheckinHistoricoResponseDTO> atualizar(@PathVariable Long id, @RequestBody @Valid CheckinHistoricoRequestDTO dto) {
        return ResponseEntity.ok(checkinService.atualizar(id, dto));
    }

    @Operation(summary = "Excluir um Check-in", description = "Remove o registro de check-in histórico do sistema.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        checkinService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Listar todos os Check-ins", description = "Retorna uma lista com o histórico completo de check-ins de todos os pets.")
    @GetMapping
    public ResponseEntity<List<CheckinHistoricoResponseDTO>> listarTodos() {
        return ResponseEntity.ok(checkinService.listarTodos());
    }


}