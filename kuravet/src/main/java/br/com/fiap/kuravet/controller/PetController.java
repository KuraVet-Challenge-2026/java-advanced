package br.com.fiap.kuravet.controller;

import br.com.fiap.kuravet.dto.PetRequestDTO;
import br.com.fiap.kuravet.dto.PetResponseDTO;
import br.com.fiap.kuravet.service.PetService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/pets")
public class PetController {

    @Autowired
    private PetService petService;

    @PostMapping
    public ResponseEntity<PetResponseDTO> cadastrar(@RequestBody @Valid PetRequestDTO dto) {
        PetResponseDTO response = petService.salvar(dto);
        return ResponseEntity.ok(response);
    }

    // Listagem com paginação e ordenação (ex: /pets?page=0&size=5&sort=nome,asc)
    @GetMapping
    public ResponseEntity<Page<PetResponseDTO>> listarTodos(
            @PageableDefault(size = 10, sort = "nome") Pageable pageable) {
        return ResponseEntity.ok(petService.listarTodos(pageable));
    }

    // Busca por parâmetro (ex: /pets/busca?especie=Gato)
    @GetMapping("/busca")
    public ResponseEntity<Page<PetResponseDTO>> buscarPorEspecie(
            @RequestParam String especie,
            @PageableDefault(size = 10, sort = "nome") Pageable pageable) {
        return ResponseEntity.ok(petService.buscarPorEspecie(especie, pageable));
    }
}