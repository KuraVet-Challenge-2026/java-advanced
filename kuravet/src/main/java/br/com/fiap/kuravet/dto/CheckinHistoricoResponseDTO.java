package br.com.fiap.kuravet.dto;

import java.time.LocalDateTime;

public record CheckinHistoricoResponseDTO(
        Long id, Long idPet, LocalDateTime dataCheckin,
        String statusVitalidade, String urlFotoPet, String observacao
) {}