package br.com.fiap.kuravet.dto.auth;

import br.com.fiap.kuravet.dto.tutor.TutorRequestDTO;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Payload de entrada do autocadastro publico via API mobile. Reune os dados
 * do TUTOR e as credenciais de acesso em uma unica requisicao.
 */
public record CadastroRequestDTO(

        @NotBlank(message = "O nome do tutor e obrigatorio.")
        String nome,

        @NotBlank(message = "O CPF do tutor e obrigatorio.")
        String cpf,

        String telefone,

        @Email(message = "O e-mail informado e invalido.")
        String email,

        String endereco,

        @NotBlank(message = "O nome de usuario e obrigatorio.")
        String username,

        @NotBlank(message = "A senha e obrigatoria.")
        @Size(min = 6, message = "A senha deve ter pelo menos 6 caracteres.")
        String senha
) {

    /** Reaproveita o mesmo DTO/fluxo que TutorService.criar ja usa para o portal. */
    public TutorRequestDTO paraTutorDTO() {
        return new TutorRequestDTO(nome, cpf, telefone, email, endereco);
    }
}
