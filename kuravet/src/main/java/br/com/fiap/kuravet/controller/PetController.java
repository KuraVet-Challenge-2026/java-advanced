package br.com.fiap.kuravet.controller;

import br.com.fiap.kuravet.dto.PetRequestDTO;
import br.com.fiap.kuravet.dto.PetResponseDTO;
import br.com.fiap.kuravet.service.PetService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pets")
public class PetController {

    @Autowired
    private PetService petService;

    // Endpoint para CADASTRAR um novo Pet
    @PostMapping
    public ResponseEntity<PetResponseDTO> cadastrar(@RequestBody @Valid PetRequestDTO dto) {
        PetResponseDTO response = petService.salvar(dto);
        // Retorna Status 200 OK com os dados do Pet salvo
        return ResponseEntity.ok(response);
    }

    // Endpoint para LISTAR todos os Pets (Opcional, mas recomendado)
    @GetMapping
    public ResponseEntity<List<PetResponseDTO>> listarTodos() {
        List<PetResponseDTO> pets = petService.listarTodos();
        return ResponseEntity.ok(pets);
    }
}