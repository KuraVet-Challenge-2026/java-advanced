package br.com.fiap.kuravet.dto.veterinario;

import br.com.fiap.kuravet.model.Veterinario;

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
