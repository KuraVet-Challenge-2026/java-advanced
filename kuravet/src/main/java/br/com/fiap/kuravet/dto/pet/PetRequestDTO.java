package br.com.fiap.kuravet.dto.pet;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

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
        Character sexo,

        @Size(max = 40, message = "O codigo de identificacao deve ter no maximo 40 caracteres.")
        @Pattern(regexp = "^[A-Za-z0-9._-]*$", message = "O codigo de identificacao deve conter apenas letras, numeros, ponto, hifen ou underscore.")
        String codigoIdentificacao
) {
}
