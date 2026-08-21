package br.com.fiap.kuravet.dto.consulta;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * Payload de entrada para agendamento/atualizacao de CONSULTA via API mobile.
 * Nao trafega {@code status} nem {@code diagnostico}: essas transicoes sao
 * exclusivas do fluxo {@code PATCH /api/consultas/{id}/diagnostico}
 * ({@link br.com.fiap.kuravet.service.ConsultaService#realizarConsultaComDiagnostico}).
 */
public record ConsultaRequestDTO(

        @NotNull(message = "O ID do pet e obrigatorio.")
        Long idPet,

        @NotNull(message = "O ID do veterinario e obrigatorio.")
        Long idVeterinario,

        @NotNull(message = "A data da consulta e obrigatoria.")
        LocalDate dataConsulta,

        @NotBlank(message = "O tipo da consulta e obrigatorio.")
        String tipoConsulta
) {
}
