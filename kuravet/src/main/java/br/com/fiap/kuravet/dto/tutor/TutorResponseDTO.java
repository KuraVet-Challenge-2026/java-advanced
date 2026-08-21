package br.com.fiap.kuravet.dto.tutor;

import br.com.fiap.kuravet.model.Tutor;

import java.time.LocalDate;

/**
 * Payload de saida com os dados do TUTOR.
 */
public record TutorResponseDTO(
        Long idTutor,
        String nome,
        String cpf,
        String telefone,
        String email,
        String endereco,
        LocalDate dataCadastro
) {

    public static TutorResponseDTO fromEntity(Tutor tutor) {
        return new TutorResponseDTO(
                tutor.getIdTutor(),
                tutor.getNome(),
                tutor.getCpf(),
                tutor.getTelefone(),
                tutor.getEmail(),
                tutor.getEndereco(),
                tutor.getDataCadastro()
        );
    }
}
