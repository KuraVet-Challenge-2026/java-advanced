package br.com.fiap.kuravet.dto;

import java.time.LocalDate;

public record TutorResponseDTO(
        Long id,
        String nome,
        String email,
        String telefone,
        LocalDate dataCadastro
) {
}