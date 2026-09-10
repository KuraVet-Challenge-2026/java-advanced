package br.com.fiap.kuravet.dto.auth;

import br.com.fiap.kuravet.enums.Perfil;
import br.com.fiap.kuravet.model.Tutor;
import br.com.fiap.kuravet.model.Usuario;

import java.time.LocalDate;

/**
 * Payload de saida do autocadastro. Achata TUTOR e USUARIO em um unico
 * objeto para o app mobile sair do cadastro sabendo com quem logar. A senha
 * nunca aparece aqui, nem em texto nem em hash.
 */
public record CadastroResponseDTO(
        Long idTutor,
        String nome,
        String cpf,
        String telefone,
        String email,
        String endereco,
        LocalDate dataCadastro,
        String username,
        Perfil perfil
) {

    public static CadastroResponseDTO de(Usuario usuario) {
        Tutor tutor = usuario.getTutor();
        return new CadastroResponseDTO(
                tutor.getIdTutor(),
                tutor.getNome(),
                tutor.getCpf(),
                tutor.getTelefone(),
                tutor.getEmail(),
                tutor.getEndereco(),
                tutor.getDataCadastro(),
                usuario.getUsername(),
                usuario.getPerfil()
        );
    }
}
