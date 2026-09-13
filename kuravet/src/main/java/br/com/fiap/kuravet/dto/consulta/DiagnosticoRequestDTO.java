package br.com.fiap.kuravet.dto.consulta;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;


@Data
public class DiagnosticoRequestDTO {

    @NotBlank(message = "Descreva o diagnostico antes de encerrar a consulta.")
    @Size(min = 15, max = 400, message = "O diagnostico deve ter entre 15 e 400 caracteres.")
    private String diagnostico;
}