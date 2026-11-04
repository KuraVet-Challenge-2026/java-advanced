package br.com.fiap.kuravet.ia;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public record ContextoOrientacao(String nomePet,
                                 String especie,
                                 int idadeAnos,
                                 Urgencia urgencia,
                                 String recomendacao,
                                 Set<Sintoma> sintomas,
                                 String relato,
                                 List<TriagemAnterior> anteriores) {

    public ContextoOrientacao {
        sintomas = sintomas == null || sintomas.isEmpty()
                ? EnumSet.noneOf(Sintoma.class)
                : EnumSet.copyOf(sintomas);
        anteriores = anteriores == null ? List.of() : List.copyOf(anteriores);
    }

    public record TriagemAnterior(LocalDateTime data, Urgencia urgencia, Set<Sintoma> sintomas) {

        public TriagemAnterior {
            sintomas = sintomas == null || sintomas.isEmpty()
                    ? EnumSet.noneOf(Sintoma.class)
                    : EnumSet.copyOf(sintomas);
        }
    }
}
