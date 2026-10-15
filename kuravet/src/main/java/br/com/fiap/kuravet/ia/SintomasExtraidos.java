package br.com.fiap.kuravet.ia;

import java.util.Set;

/**
 * Sintomas estruturados extraidos do relato do tutor.
 *
 * @param sintomas     conjunto imutavel de sintomas (null vira conjunto vazio)
 * @param duracaoDias  ha quantos dias os sintomas aparecem; null quando nao informado
 */
public record SintomasExtraidos(Set<Sintoma> sintomas, Integer duracaoDias) {

    public SintomasExtraidos {
        sintomas = sintomas == null ? Set.of() : Set.copyOf(sintomas);
        if (duracaoDias != null && duracaoDias < 0) {
            throw new IllegalArgumentException("A duração dos sintomas não pode ser negativa.");
        }
    }

    public boolean contem(Sintoma sintoma) {
        return sintomas.contains(sintoma);
    }

    public boolean contemAlgum(Sintoma... candidatos) {
        for (Sintoma candidato : candidatos) {
            if (sintomas.contains(candidato)) {
                return true;
            }
        }
        return false;
    }
}
