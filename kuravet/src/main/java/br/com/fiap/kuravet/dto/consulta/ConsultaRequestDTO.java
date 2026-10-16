package br.com.fiap.kuravet.dto.consulta;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record ConsultaRequestDTO(

        @NotNull(message = "O ID do pet é obrigatório.")
        Long idPet,

        @NotNull(message = "O ID do veterinário é obrigatório.")
        Long idVeterinario,

        @NotNull(message = "A data desejada é obrigatória.")
        @Future(message = "A data desejada precisa ser futura.")
        LocalDate dataConsulta,

        @NotBlank(message = "O tipo da consulta é obrigatório.")
        @Size(max = 40, message = "O tipo da consulta deve ter no máximo 40 caracteres.")
        String tipoConsulta
) {
}