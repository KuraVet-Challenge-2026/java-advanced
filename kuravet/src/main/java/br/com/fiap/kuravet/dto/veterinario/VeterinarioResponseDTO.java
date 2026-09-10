package br.com.fiap.kuravet.dto.veterinario;

import br.com.fiap.kuravet.model.Veterinario;

/**
 * Payload de saida com os dados do VETERINARIO, usado pelo app mobile para
 * montar a escolha de profissional ao solicitar uma teleconsulta.
 */
public record VeterinarioResponseDTO(
        Long idVeterinario,
        String nome,
        String crmv,
        String especialidade,
        String telefone,
        String email
) {

    public static VeterinarioResponseDTO fromEntity(Veterinario veterinario) {
        return new VeterinarioResponseDTO(
                veterinario.getIdVeterinario(),
                veterinario.getNome(),
                veterinario.getCrmv(),
                veterinario.getEspecialidade(),
                veterinario.getTelefone(),
                veterinario.getEmail()
        );
    }
}
