package br.com.fiap.kuravet.dto.pet;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record PetRequestDTO(

        @NotBlank(message = "O nome do pet é obrigatório.")
        String nome,

        @NotBlank(message = "A espécie do pet é obrigatória.")
        String especie,

        String raca,

        @NotNull(message = "A data de nascimento é obrigatória.")
        @Past(message = "A data de nascimento deve estar no passado.")
        LocalDate dataNascimento,

        @NotNull(message = "O sexo do pet é obrigatório (M ou F).")
        Character sexo,

        @Size(max = 40, message = "O código de identificação deve ter no máximo 40 caracteres.")
        @Pattern(regexp = "^\\s*[A-Za-z0-9._-]*\\s*$", message = "O código de identificação deve conter apenas letras, números, ponto, hífen ou underscore.")
        String codigoIdentificacao
) {
}
