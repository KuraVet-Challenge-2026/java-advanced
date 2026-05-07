package br.com.fiap.kuravet.dto;

import jakarta.validation.constraints.NotNull;

public record EventoConsultaRequestDTO(
        @NotNull(message = "O ID do pet é obrigatório")
        Long idPet,
        String diagnostico,
        String tratamento
) {
}