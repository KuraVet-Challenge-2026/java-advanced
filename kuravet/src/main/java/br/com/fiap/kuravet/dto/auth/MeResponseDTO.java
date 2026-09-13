package br.com.fiap.kuravet.dto.auth;

import br.com.fiap.kuravet.enums.Perfil;


public record MeResponseDTO(
        Long idUsuario,
        String username,
        Perfil perfil,
        Long idTutor,
        String nomeTutor
) {
}
