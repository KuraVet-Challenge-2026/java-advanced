package br.com.fiap.kuravet.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * Payload de entrada para cadastro/atualizacao de PET via API mobile.
 * Trafega apenas os dados necessarios, sem expor a entidade JPA
 * {@link br.com.fiap.kuravet.model.Pet} diretamente ao cliente React Native.
 */
public record PetRequestDTO(

        @NotBlank(message = "O nome do pet e obrigatorio.")
        String nome,

        @NotBlank(message = "A especie do pet e obrigatoria.")
        String especie,

        String raca,

        @NotNull(message = "A data de nascimento e obrigatoria.")
        LocalDate dataNascimento,

        @NotNull(message = "O sexo do pet e obrigatorio (M ou F).")
        Character sexo,

        @NotNull(message = "O ID do tutor e obrigatorio para vincular o pet.")
        Long idTutor
) {
}
