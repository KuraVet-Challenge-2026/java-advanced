package br.com.fiap.kuravet.controller.web;

import br.com.fiap.kuravet.dto.consulta.ConsultaRequestDTO;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

// Mesmas validacoes do ConsultaRequestDTO, com mensagens voltadas para quem preenche a tela.
@Data
public class SolicitacaoConsultaForm {

    @NotNull(message = "Selecione o pet.")
    private Long idPet;

    @NotNull(message = "Selecione o veterinário.")
    private Long idVeterinario;

    @NotNull(message = "Informe a data desejada.")
    @Future(message = "A data desejada precisa ser futura.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dataConsulta;

    @NotBlank(message = "Informe o tipo da consulta.")
    @Size(max = 40, message = "O tipo da consulta deve ter no máximo 40 caracteres.")
    private String tipoConsulta;

    public ConsultaRequestDTO paraDTO() {
        return new ConsultaRequestDTO(idPet, idVeterinario, dataConsulta, tipoConsulta);
    }
}
