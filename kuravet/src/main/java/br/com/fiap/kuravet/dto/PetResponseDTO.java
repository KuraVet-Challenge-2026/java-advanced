package br.com.fiap.kuravet.dto;

import java.time.LocalDate;

public record PetResponseDTO(
        Long id,
        Long idTutor,
        String nome,
        String especie,
        String raca,
        LocalDate dataNascimento,
        Integer scoreVitalidade
) {
}