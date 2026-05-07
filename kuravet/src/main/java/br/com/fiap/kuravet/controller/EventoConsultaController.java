package br.com.fiap.kuravet.controller;

import br.com.fiap.kuravet.dto.EventoConsultaRequestDTO;
import br.com.fiap.kuravet.dto.EventoConsultaResponseDTO;
import br.com.fiap.kuravet.service.EventoConsultaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/eventos")
public class EventoConsultaController {

    @Autowired
    private EventoConsultaService eventoService;

    @PostMapping
    public ResponseEntity<EventoConsultaResponseDTO> registrar(@RequestBody @Valid EventoConsultaRequestDTO dto) {
        return ResponseEntity.ok(eventoService.salvar(dto));
    }
}