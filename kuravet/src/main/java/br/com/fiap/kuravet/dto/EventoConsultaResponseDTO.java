package br.com.fiap.kuravet.dto;

import java.time.LocalDate;

public record EventoConsultaResponseDTO(
        Long id, Long idPet, String tipoEvento, LocalDate dataEvento,
        String descricaoEvento, String nomeVeterinario
) {}