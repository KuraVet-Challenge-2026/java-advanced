package br.com.fiap.kuravet.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CheckinHistoricoRequestDTO(
        @NotNull(message = "O ID do pet é obrigatório") Long idPet,
        @NotBlank(message = "O status de vitalidade é obrigatório") String statusVitalidade,
        String urlFotoPet, String observacao
) {}