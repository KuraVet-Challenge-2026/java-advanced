package br.com.fiap.kuravet.dto.consulta;

import jakarta.validation.constraints.NotBlank;

/**
 * Payload de entrada para o encerramento de uma CONSULTA com emissao de
 * diagnostico (fluxo consumido por {@code PATCH /api/consultas/{id}/diagnostico}).
 */
public record DiagnosticoRequestDTO(

        @NotBlank(message = "O diagnostico e obrigatorio para realizar a consulta.")
        String diagnostico
) {
}
