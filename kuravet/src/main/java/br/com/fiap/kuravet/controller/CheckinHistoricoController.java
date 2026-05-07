package br.com.fiap.kuravet.controller;

import br.com.fiap.kuravet.dto.CheckinHistoricoRequestDTO;
import br.com.fiap.kuravet.dto.CheckinHistoricoResponseDTO;
import br.com.fiap.kuravet.service.CheckinHistoricoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/checkins")
public class CheckinHistoricoController {

    @Autowired
    private CheckinHistoricoService checkinService;

    @PostMapping
    public ResponseEntity<CheckinHistoricoResponseDTO> registrar(@RequestBody @Valid CheckinHistoricoRequestDTO dto) {
        return ResponseEntity.ok(checkinService.salvar(dto));
    }
}