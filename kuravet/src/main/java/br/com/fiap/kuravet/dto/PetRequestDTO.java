package br.com.fiap.kuravet.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDate;

public record PetRequestDTO(

        @NotNull(message = "O ID do tutor responsável é obrigatório")
        Long idTutor,

        @NotBlank(message = "O nome do pet é obrigatório")
        String nome,

        @NotBlank(message = "A espécie do pet (ex: Cachorro, Gato) é obrigatória")
        String especie,

        String raca,

        @PastOrPresent(message = "A data de nascimento não pode estar no futuro")
        LocalDate dataNascimento
) {
}