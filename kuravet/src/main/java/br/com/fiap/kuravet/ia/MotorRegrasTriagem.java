package br.com.fiap.kuravet.ia;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static br.com.fiap.kuravet.ia.Sintoma.APATIA;
import static br.com.fiap.kuravet.ia.Sintoma.COCEIRA;
import static br.com.fiap.kuravet.ia.Sintoma.CONVULSAO;
import static br.com.fiap.kuravet.ia.Sintoma.DESMAIO;
import static br.com.fiap.kuravet.ia.Sintoma.DIARREIA;
import static br.com.fiap.kuravet.ia.Sintoma.DIFICULDADE_RESPIRAR;
import static br.com.fiap.kuravet.ia.Sintoma.ESPIRROS;
import static br.com.fiap.kuravet.ia.Sintoma.FALTA_DE_APETITE;
import static br.com.fiap.kuravet.ia.Sintoma.FEBRE;
import static br.com.fiap.kuravet.ia.Sintoma.FERIDA;
import static br.com.fiap.kuravet.ia.Sintoma.INGESTAO_TOXICA;
import static br.com.fiap.kuravet.ia.Sintoma.MANCANDO;
import static br.com.fiap.kuravet.ia.Sintoma.NAO_URINA;
import static br.com.fiap.kuravet.ia.Sintoma.SANGRAMENTO;
import static br.com.fiap.kuravet.ia.Sintoma.TOSSE;
import static br.com.fiap.kuravet.ia.Sintoma.VOMITO;

/**
 * Motor deterministico da triagem. E o unico lugar que decide a urgencia:
 * a LLM apenas extrai os sintomas do relato, nunca classifica o caso.
 */
@Component
public class MotorRegrasTriagem {

    static final String AVISO =
            "Esta triagem é orientativa e não substitui a avaliação do veterinário.";

    static final int DIAS_PARA_ESCALONAR = 3;

    private static final List<RegraTriagem> REGRAS = List.of(
            new RegraTriagem("R1", "sinal de emergência (dificuldade para respirar, convulsão, sangramento, ingestão tóxica ou desmaio)",
                    Urgencia.EMERGENCIA,
                    (s, p) -> s.contemAlgum(DIFICULDADE_RESPIRAR, CONVULSAO, SANGRAMENTO, INGESTAO_TOXICA, DESMAIO)),
            new RegraTriagem("R2", "gato que não urina (risco de obstrução urinária)",
                    Urgencia.EMERGENCIA,
                    (s, p) -> p.isGato() && s.contem(NAO_URINA)),
            new RegraTriagem("R3", "não urina (espécie diferente de gato)",
                    Urgencia.ALTA,
                    (s, p) -> !p.isGato() && s.contem(NAO_URINA)),
            new RegraTriagem("R4", "vômito e diarreia juntos",
                    Urgencia.ALTA,
                    (s, p) -> s.contem(VOMITO) && s.contem(DIARREIA)),
            new RegraTriagem("R5", "filhote ou idoso com vômito ou diarreia",
                    Urgencia.ALTA,
                    (s, p) -> (p.isFilhote() || p.isIdoso()) && s.contemAlgum(VOMITO, DIARREIA)),
            new RegraTriagem("R6", "febre com apatia",
                    Urgencia.ALTA,
                    (s, p) -> s.contem(FEBRE) && s.contem(APATIA)),
            new RegraTriagem("R7", "sintoma que exige acompanhamento veterinário",
                    Urgencia.MODERADA,
                    (s, p) -> s.contemAlgum(VOMITO, DIARREIA, FALTA_DE_APETITE, APATIA, FEBRE, TOSSE, FERIDA, MANCANDO)),
            new RegraTriagem("R8", "coceira ou espirros",
                    Urgencia.BAIXA,
                    (s, p) -> s.contemAlgum(COCEIRA, ESPIRROS))
    );

    private static final Map<Urgencia, String> RECOMENDACOES = Map.of(
            Urgencia.EMERGENCIA, "Procure atendimento presencial imediato em um pronto-socorro veterinário.",
            Urgencia.ALTA, "Solicite uma teleconsulta prioritária ainda hoje.",
            Urgencia.MODERADA, "Agende uma teleconsulta nas próximas 48 horas e observe a evolução.",
            Urgencia.BAIXA, "Observe por 48 horas; se surgirem novos sintomas, refaça a triagem."
    );

    private static final String RECOMENDACAO_SEM_SINTOMAS =
            "Descreva melhor os sintomas ou agende uma teleconsulta de rotina.";

    public ResultadoTriagem avaliar(SintomasExtraidos sintomas, PerfilPet perfil) {
        Objects.requireNonNull(sintomas, "sintomas");
        Objects.requireNonNull(perfil, "perfil");

        List<RegraTriagem> disparadas = REGRAS.stream()
                .filter(regra -> regra.dispara(sintomas, perfil))
                .toList();

        if (disparadas.isEmpty()) {
            return new ResultadoTriagem(Urgencia.BAIXA,
                    comAviso(RECOMENDACAO_SEM_SINTOMAS),
                    List.of("R10 - nenhum sintoma reconhecido no relato"));
        }

        Urgencia urgencia = disparadas.stream()
                .map(RegraTriagem::urgencia)
                .reduce(Urgencia.BAIXA, Urgencia::maior);
        List<String> regrasAplicadas = new ArrayList<>(disparadas.stream()
                .map(RegraTriagem::formatar)
                .toList());

        Integer dias = sintomas.duracaoDias();
        if (urgencia == Urgencia.MODERADA && dias != null && dias >= DIAS_PARA_ESCALONAR) {
            urgencia = Urgencia.ALTA;
            regrasAplicadas.add("R9 - sintomas persistentes há " + dias + " dias");
        }

        return new ResultadoTriagem(urgencia, comAviso(RECOMENDACOES.get(urgencia)), regrasAplicadas);
    }

    private static String comAviso(String recomendacao) {
        return recomendacao + " " + AVISO;
    }
}
