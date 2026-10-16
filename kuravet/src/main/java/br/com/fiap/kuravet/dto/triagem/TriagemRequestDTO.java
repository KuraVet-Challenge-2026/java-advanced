package br.com.fiap.kuravet.dto.triagem;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TriagemRequestDTO(

        @NotNull(message = "O ID do pet é obrigatório.")
        Long idPet,

        @NotBlank(message = "O relato dos sintomas é obrigatório.")
        @Size(min = 10, max = 1000, message = "O relato deve ter entre 10 e 1000 caracteres.")
        String relato
) {
}
