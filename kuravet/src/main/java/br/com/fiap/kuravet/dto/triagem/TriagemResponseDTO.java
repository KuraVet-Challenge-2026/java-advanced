package br.com.fiap.kuravet.dto.triagem;

import br.com.fiap.kuravet.ia.MetodoExtracao;
import br.com.fiap.kuravet.ia.OrigemOrientacao;
import br.com.fiap.kuravet.ia.Sintoma;
import br.com.fiap.kuravet.ia.Urgencia;
import br.com.fiap.kuravet.model.Triagem;

import java.time.LocalDateTime;
import java.util.List;

public record TriagemResponseDTO(
        Long idTriagem,
        Long idPet,
        String nomePet,
        Urgencia urgencia,
        String recomendacao,
        List<String> sintomas,
        List<String> regrasAplicadas,
        MetodoExtracao metodoExtracao,
        LocalDateTime dataTriagem,
        boolean sugereTeleconsultaPrioritaria,
        String orientacao,
        OrigemOrientacao origemOrientacao
) {

    public static TriagemResponseDTO fromEntity(Triagem triagem) {
        return new TriagemResponseDTO(
                triagem.getIdTriagem(),
                triagem.getPet().getIdPet(),
                triagem.getPet().getNome(),
                triagem.getUrgencia(),
                triagem.getRecomendacao(),
                triagem.getSintomas().stream().sorted().map(Sintoma::getDescricao).toList(),
                List.copyOf(triagem.getRegrasAplicadas()),
                triagem.getMetodoExtracao(),
                triagem.getDataTriagem(),
                triagem.getUrgencia().sugereTeleconsultaPrioritaria(),
                triagem.getOrientacao(),
                triagem.getOrigemOrientacao()
        );
    }
}
