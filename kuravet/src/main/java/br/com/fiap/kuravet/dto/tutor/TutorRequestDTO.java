package br.com.fiap.kuravet.dto.tutor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record TutorRequestDTO(

        @NotBlank(message = "O nome do tutor é obrigatório.")
        String nome,

        @NotBlank(message = "O CPF do tutor é obrigatório.")
        String cpf,

        String telefone,

        @Email(message = "O e-mail informado é inválido.")
        String email,

        String endereco
) {
}
