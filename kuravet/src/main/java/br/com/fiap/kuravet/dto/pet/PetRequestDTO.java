package br.com.fiap.kuravet.dto.pet;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;

import java.time.LocalDate;

/**
 * Payload de entrada para cadastro/atualizacao de PET via API mobile.
 * Nao carrega idTutor: o dono do pet e sempre o TUTOR autenticado
 * (ver {@link br.com.fiap.kuravet.service.PetService}), nunca um valor
 * vindo do cliente.
 */
public record PetRequestDTO(

        @NotBlank(message = "O nome do pet e obrigatorio.")
        String nome,

        @NotBlank(message = "A especie do pet e obrigatoria.")
        String especie,

        String raca,

        @NotNull(message = "A data de nascimento e obrigatoria.")
        @Past(message = "A data de nascimento deve estar no passado.")
        LocalDate dataNascimento,

        @NotNull(message = "O sexo do pet e obrigatorio (M ou F).")
        Character sexo
) {
}
