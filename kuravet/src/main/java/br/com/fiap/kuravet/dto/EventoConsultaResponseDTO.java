package br.com.fiap.kuravet.dto;

public record EventoConsultaResponseDTO(
        Long id,
        Long idPet,
        String diagnostico,
        String tratamento
) {
}