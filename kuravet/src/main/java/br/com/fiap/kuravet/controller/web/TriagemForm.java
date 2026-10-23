package br.com.fiap.kuravet.controller.web;

import br.com.fiap.kuravet.dto.triagem.TriagemRequestDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

// Mesmas validacoes do TriagemRequestDTO, com mensagens voltadas para quem preenche a tela.
@Data
public class TriagemForm {

    @NotNull(message = "Selecione o pet.")
    private Long idPet;

    @NotBlank(message = "Descreva o que o seu pet está sentindo.")
    @Size(min = 10, max = 1000, message = "O relato deve ter entre 10 e 1000 caracteres.")
    private String relato;

    public TriagemRequestDTO paraDTO() {
        return new TriagemRequestDTO(idPet, relato);
    }
}
