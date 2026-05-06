package br.com.fiap.kuravet.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record EventoConsultaRequestDTO(
        @NotNull(message = "O ID do pet é obrigatório") Long idPet,
        @NotBlank(message = "O tipo de evento é obrigatório") String tipoEvento,
        @NotNull(message = "A data do evento é obrigatória")
        @FutureOrPresent(message = "A data não pode estar no passado") LocalDate dataEvento,
        @NotBlank(message = "A descrição do evento é obrigatória") String descricaoEvento,
        String nomeVeterinario
) {}