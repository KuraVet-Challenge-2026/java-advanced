package br.com.fiap.kuravet.dto.consulta;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Justificativa obrigatoria da recusa de uma solicitacao, compartilhada entre
 * o endpoint REST e o formulario do portal.
 */
@Data
public class RecusaRequestDTO {

    @NotBlank(message = "Informe o motivo da recusa.")
    @Size(min = 10, max = 300, message = "O motivo deve ter entre 10 e 300 caracteres.")
    private String motivo;
}