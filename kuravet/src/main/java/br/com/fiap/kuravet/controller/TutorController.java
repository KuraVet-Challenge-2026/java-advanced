package br.com.fiap.kuravet.controller;

import br.com.fiap.kuravet.dto.TutorRequestDTO;
import br.com.fiap.kuravet.dto.TutorResponseDTO;
import br.com.fiap.kuravet.service.TutorService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tutores")
public class TutorController {

    @Autowired
    private TutorService tutorService;

    @PostMapping
    public ResponseEntity<TutorResponseDTO> cadastrar(@RequestBody @Valid TutorRequestDTO dto) {
        TutorResponseDTO response = tutorService.salvar(dto);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<TutorResponseDTO>> listar() {
        List<TutorResponseDTO> lista = tutorService.listarTodos();
        return ResponseEntity.ok(lista);
    }
}