package br.com.fiap.kuravet.dto.auth;

import br.com.fiap.kuravet.dto.tutor.TutorRequestDTO;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


public record CadastroRequestDTO(

        @NotBlank(message = "O nome do tutor é obrigatório.")
        String nome,

        @NotBlank(message = "O CPF do tutor é obrigatório.")
        String cpf,

        String telefone,

        @Email(message = "O e-mail informado é inválido.")
        String email,

        String endereco,

        @NotBlank(message = "O nome de usuário é obrigatório.")
        String username,

        @NotBlank(message = "A senha é obrigatória.")
        @Size(min = 6, message = "A senha deve ter pelo menos 6 caracteres.")
        String senha
) {


    public TutorRequestDTO paraTutorDTO() {
        return new TutorRequestDTO(nome, cpf, telefone, email, endereco);
    }
}
