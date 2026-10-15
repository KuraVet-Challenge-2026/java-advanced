package br.com.fiap.kuravet.ia;

import java.util.function.BiPredicate;

public record RegraTriagem(String codigo,
                           String descricao,
                           Urgencia urgencia,
                           BiPredicate<SintomasExtraidos, PerfilPet> condicao) {

    public boolean dispara(SintomasExtraidos sintomas, PerfilPet perfil) {
        return condicao.test(sintomas, perfil);
    }

    public String formatar() {
        return codigo + " - " + descricao;
    }
}
