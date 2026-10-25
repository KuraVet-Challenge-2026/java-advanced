package br.com.fiap.kuravet.ia;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

/**
 * Orientacao montada por modelo fixo, sem IA: uma frase com os sintomas e outra com a acao
 * esperada para a urgencia. Deterministica; usada quando a LLM esta desligada ou falha.
 */
@Component
public class GeradorOrientacaoModeloFixo implements GeradorOrientacao {

    @Override
    public String gerar(ContextoOrientacao contexto) {
        String nome = contexto.nomePet() == null || contexto.nomePet().isBlank() ? "seu pet" : contexto.nomePet();
        return fraseSintomas(nome, contexto) + " " + fraseUrgencia(nome, contexto.urgencia());
    }

    @Override
    public OrigemOrientacao origem() {
        return OrigemOrientacao.MODELO_FIXO;
    }

    private static String fraseSintomas(String nome, ContextoOrientacao contexto) {
        if (contexto.sintomas().isEmpty()) {
            return "Não identificamos sintomas específicos no relato sobre " + nome + ".";
        }
        List<String> sintomas = contexto.sintomas().stream()
                .sorted()
                .map(s -> s.getDescricao().toLowerCase(Locale.ROOT))
                .toList();
        return "Sintomas identificados em " + nome + ": " + juntar(sintomas) + ".";
    }

    private static String fraseUrgencia(String nome, Urgencia urgencia) {
        return switch (urgencia) {
            case EMERGENCIA -> "Como a urgência é EMERGENCIA, procure agora um pronto-socorro veterinário.";
            case ALTA -> "Como a urgência é ALTA, solicite uma teleconsulta ainda hoje.";
            case MODERADA -> "Como a urgência é MODERADA, agende uma teleconsulta nas próximas 48 horas "
                    + "e observe a evolução.";
            case BAIXA -> "Como a urgência é BAIXA, observe " + nome
                    + " nos próximos dias e refaça a triagem se surgirem novos sintomas.";
        };
    }

    // "a", "a e b", "a, b e c"
    private static String juntar(List<String> itens) {
        if (itens.size() == 1) {
            return itens.get(0);
        }
        return String.join(", ", itens.subList(0, itens.size() - 1)) + " e " + itens.get(itens.size() - 1);
    }
}
