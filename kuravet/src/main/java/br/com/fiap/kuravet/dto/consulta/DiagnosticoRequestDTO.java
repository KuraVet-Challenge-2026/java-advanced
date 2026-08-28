package br.com.fiap.kuravet.dto.consulta;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Payload da emissao de diagnostico, usado tanto pelo endpoint REST
 * {@code PATCH /api/consultas/{id}/diagnostico} quanto pelo formulario do
 * portal. Classe mutavel em vez de record porque o {@code th:field} do
 * Thymeleaf precisa de getter e setter para repopular o campo quando a
 * validacao falha.
 */
@Data
public class DiagnosticoRequestDTO {

    @NotBlank(message = "Descreva o diagnostico antes de encerrar a consulta.")
    @Size(min = 15, max = 400, message = "O diagnostico deve ter entre 15 e 400 caracteres.")
    private String diagnostico;
}