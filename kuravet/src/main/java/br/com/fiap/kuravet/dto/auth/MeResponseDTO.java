package br.com.fiap.kuravet.dto.auth;

import br.com.fiap.kuravet.enums.Perfil;

/**
 * Payload de saida de {@code GET /api/auth/me}. E o ponto unico e correto
 * para o app mobile validar login e obter o perfil de quem esta autenticado,
 * em vez de inferir a partir de um endpoint de negocio.
 */
public record MeResponseDTO(
        Long idUsuario,
        String username,
        Perfil perfil,
        Long idTutor,
        String nomeTutor
) {
}
