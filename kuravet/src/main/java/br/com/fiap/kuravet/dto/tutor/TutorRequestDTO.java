package br.com.fiap.kuravet.dto.tutor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Payload de entrada para cadastro/atualizacao de TUTOR via API mobile.
 */
public record TutorRequestDTO(

        @NotBlank(message = "O nome do tutor e obrigatorio.")
        String nome,

        @NotBlank(message = "O CPF do tutor e obrigatorio.")
        String cpf,

        String telefone,

        @Email(message = "O e-mail informado e invalido.")
        String email,

        String endereco
) {
}
