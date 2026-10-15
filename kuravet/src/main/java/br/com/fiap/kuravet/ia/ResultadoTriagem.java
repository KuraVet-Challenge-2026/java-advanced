package br.com.fiap.kuravet.ia;

import java.util.List;

public record ResultadoTriagem(Urgencia urgencia, String recomendacao, List<String> regrasAplicadas) {

    public ResultadoTriagem {
        regrasAplicadas = regrasAplicadas == null ? List.of() : List.copyOf(regrasAplicadas);
    }
}
