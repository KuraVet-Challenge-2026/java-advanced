package br.com.fiap.kuravet.ia;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Avaliacao do caminho deterministico da triagem (palavras-chave + motor de regras) sobre
 * relatos realistas em src/test/resources/ia/casos-triagem.csv. Nao chama a LLM.
 * Os relatos sao ficticios, por isso podem aparecer no log.
 */
class AvaliacaoTriagemTest {

    private static final Logger log = LoggerFactory.getLogger(AvaliacaoTriagemTest.class);

    private static final String ARQUIVO = "/ia/casos-triagem.csv";
    static final double ACURACIA_MINIMA = 0.85;

    private final ExtratorPalavrasChave extrator = new ExtratorPalavrasChave();
    private final MotorRegrasTriagem motor = new MotorRegrasTriagem();

    record Caso(int linha, String relato, String especie, int idadeAnos, Urgencia esperada, String observacao) {
    }

    record Resultado(Caso caso, Urgencia obtida, SintomasExtraidos sintomas, List<String> regras) {

        boolean acertou() {
            return caso.esperada() == obtida;
        }
    }

    @Test
    void acuraciaDoCaminhoDeterministicoDeveSerDePeloMenos85PorCento() throws IOException {
        List<Caso> casos = lerCasos();
        assertThat(casos).as("conjunto de avaliacao").hasSizeGreaterThanOrEqualTo(30);
        assertThat(casos).extracting(Caso::esperada).as("todos os niveis cobertos").contains(Urgencia.values());

        List<Resultado> resultados = casos.stream().map(this::avaliar).toList();
        long acertos = resultados.stream().filter(Resultado::acertou).count();
        double acuracia = (double) acertos / resultados.size();

        log.info("\n{}", resumo(resultados, acertos, acuracia));

        assertThat(acuracia).as("acurácia geral").isGreaterThanOrEqualTo(ACURACIA_MINIMA);
    }

    private Resultado avaliar(Caso caso) {
        SintomasExtraidos sintomas = extrator.extrair(caso.relato());
        ResultadoTriagem resultado = motor.avaliar(sintomas, new PerfilPet(caso.especie(), caso.idadeAnos()));
        return new Resultado(caso, resultado.urgencia(), sintomas, resultado.regrasAplicadas());
    }

    private static List<Caso> lerCasos() throws IOException {
        List<Caso> casos = new ArrayList<>();
        try (InputStream entrada = Objects.requireNonNull(
                AvaliacaoTriagemTest.class.getResourceAsStream(ARQUIVO), ARQUIVO);
             BufferedReader leitor = new BufferedReader(new InputStreamReader(entrada, StandardCharsets.UTF_8))) {

            String linha;
            int numero = 0;
            while ((linha = leitor.readLine()) != null) {
                numero++;
                if (numero == 1 || linha.isBlank()) {
                    continue; // cabecalho
                }
                String[] colunas = linha.split(";", -1);
                if (colunas.length != 5) {
                    throw new IllegalStateException(ARQUIVO + ", linha " + numero + ": esperadas 5 colunas, veio " + colunas.length);
                }
                casos.add(new Caso(numero, colunas[0].trim(), colunas[1].trim(), Integer.parseInt(colunas[2].trim()),
                        Urgencia.valueOf(colunas[3].trim()), colunas[4].trim()));
            }
        }
        return casos;
    }

    private static String resumo(List<Resultado> resultados, long acertos, double acuracia) {
        StringBuilder texto = new StringBuilder()
                .append("===== Avaliação da triagem (palavras-chave + motor de regras) =====\n")
                .append(String.format(Locale.ROOT, "Acurácia geral: %d/%d = %.1f%% (mínimo %.0f%%)%n",
                        acertos, resultados.size(), acuracia * 100, ACURACIA_MINIMA * 100))
                .append("\nAcurácia por nível (esperado):\n");

        for (Urgencia nivel : Urgencia.values()) {
            List<Resultado> doNivel = resultados.stream().filter(r -> r.caso().esperada() == nivel).toList();
            long certos = doNivel.stream().filter(Resultado::acertou).count();
            texto.append(String.format(Locale.ROOT, "  %-10s %2d/%2d = %5.1f%%%n", nivel, certos, doNivel.size(),
                    doNivel.isEmpty() ? 0.0 : 100.0 * certos / doNivel.size()));
        }

        Map<Urgencia, Map<Urgencia, Integer>> matriz = new EnumMap<>(Urgencia.class);
        for (Resultado r : resultados) {
            matriz.computeIfAbsent(r.caso().esperada(), k -> new EnumMap<>(Urgencia.class))
                    .merge(r.obtida(), 1, Integer::sum);
        }
        texto.append("\nMatriz de confusão (linhas = esperado, colunas = obtido):\n").append(String.format("  %-10s", ""));
        for (Urgencia obtida : Urgencia.values()) {
            texto.append(String.format("%11s", obtida));
        }
        texto.append('\n');
        for (Urgencia esperada : Urgencia.values()) {
            texto.append(String.format("  %-10s", esperada));
            for (Urgencia obtida : Urgencia.values()) {
                texto.append(String.format("%11d", matriz.getOrDefault(esperada, Map.of()).getOrDefault(obtida, 0)));
            }
            texto.append('\n');
        }

        List<Resultado> erros = resultados.stream().filter(r -> !r.acertou()).toList();
        texto.append("\nCasos errados: ").append(erros.size()).append('\n');
        for (Resultado r : erros) {
            texto.append(String.format("  linha %d: esperado %s, obtido %s | sintomas %s, duração %s | %s%n    \"%s\" (%s)%n",
                    r.caso().linha(), r.caso().esperada(), r.obtida(), r.sintomas().sintomas(),
                    r.sintomas().duracaoDias(), r.regras(), r.caso().relato(), r.caso().observacao()));
        }
        return texto.toString();
    }
}
