package br.com.fiap.kuravet.controller;

import br.com.fiap.kuravet.dto.TutorRequestDTO;
import br.com.fiap.kuravet.dto.TutorResponseDTO;
import br.com.fiap.kuravet.service.TutorService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/tutores")
public class TutorController {

    @Autowired
    private TutorService tutorService;

    @Operation(summary = "Cadastrar um novo Tutor", description = "Cria um tutor no sistema com nome, CPF, e-mail e telefone.")
    @PostMapping
    public ResponseEntity<TutorResponseDTO> cadastrar(@RequestBody @Valid TutorRequestDTO dto) {
        return ResponseEntity.ok(tutorService.salvar(dto));
    }

    @Operation(summary = "Listar todos os Tutores", description = "Retorna uma lista com todos os tutores cadastrados no banco de dados.")
    @GetMapping
    public ResponseEntity<List<TutorResponseDTO>> listar() {
        return ResponseEntity.ok(tutorService.listarTodos());
    }

    @Operation(summary = "Atualizar um Tutor", description = "Atualiza os dados de um tutor existente buscando pelo seu ID.")
    @PutMapping("/{id}")
    public ResponseEntity<TutorResponseDTO> atualizar(@PathVariable Long id, @RequestBody @Valid TutorRequestDTO dto) {
        return ResponseEntity.ok(tutorService.atualizar(id, dto));
    }

    @Operation(summary = "Excluir um Tutor", description = "Remove um tutor do sistema pelo seu ID.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        tutorService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}

