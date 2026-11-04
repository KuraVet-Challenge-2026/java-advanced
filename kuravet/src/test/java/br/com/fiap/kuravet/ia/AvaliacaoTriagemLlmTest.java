package br.com.fiap.kuravet.ia;

import br.com.fiap.kuravet.ia.llm.ClienteGemini;
import br.com.fiap.kuravet.ia.llm.ExtratorGemini;
import br.com.fiap.kuravet.ia.llm.FalhaLlmException;
import br.com.fiap.kuravet.ia.llm.GeminiProperties;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.support.ResourcePropertySource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class AvaliacaoTriagemLlmTest {

    private static final Logger log = LoggerFactory.getLogger(AvaliacaoTriagemLlmTest.class);

    static final String ARQUIVO_AJUSTE = "/ia/casos-triagem.csv";
    static final String ARQUIVO_TESTE = "/avaliacao/relatos_teste.csv";
    static final String CONJUNTO_AJUSTE = "ajuste";
    static final String CONJUNTO_TESTE = "teste";
    static final Path DESTINO = Path.of("target", "avaliacao-llm");
    static final long PAUSA_PADRAO_MS = 4000;

    record Relato(String conjunto, String id, String texto, String especie, int idadeAnos, Urgencia esperada) {
    }

    record Linha(Relato relato, SintomasExtraidos sintomasPalavras, Urgencia obtidaPalavras,
                 SintomasExtraidos sintomasLlm, Urgencia obtidaLlm, long latenciaLlmMs, String statusLlm) {

        boolean llmOk() {
            return obtidaLlm != null;
        }
    }

    record Execucao(List<Linha> linhas, int totalRelatos, Relato interrompidoEm, String motivoInterrupcao) {
    }

    @Test
    void compararExtracaoPorLlmEPorPalavrasChave() throws IOException {
        assumeTrue("true".equalsIgnoreCase(System.getenv("LLM_EVAL")), "defina LLM_EVAL=true para rodar");
        String chave = System.getenv("LLM_API_KEY");
        assumeTrue(chave != null && !chave.isBlank(), "LLM_API_KEY ausente");

        // Nao logar o record: o toString inclui a chave
        GeminiProperties propriedades = propriedadesDaAplicacao();
        long pausaMs = pausaMs();

        List<Relato> relatos = new ArrayList<>(lerAjuste());
        relatos.addAll(lerTeste());

        Execucao execucao = executar(relatos, new ExtratorGemini(new ClienteGemini(propriedades)), pausaMs);
        String configuracao = String.format(Locale.ROOT, "modelo `%s`, thinking-level `%s`, timeout %d ms, pausa %d ms",
                propriedades.model(), propriedades.temThinkingLevel() ? propriedades.thinkingLevel() : "(não enviado)",
                propriedades.timeoutMs(), pausaMs);
        gravarRelatorio(execucao, configuracao, DESTINO);

        if (execucao.interrompidoEm() != null) {
            log.warn("Avaliação interrompida por cota no relato {}/{} ({} de {} avaliados). Relatório parcial em {}",
                    execucao.interrompidoEm().conjunto(), execucao.interrompidoEm().id(),
                    execucao.linhas().size(), execucao.totalRelatos(), DESTINO.toAbsolutePath());
        } else {
            log.info("Avaliação concluída: {} relatos. Relatório em {}", execucao.linhas().size(), DESTINO.toAbsolutePath());
        }
        assertThat(DESTINO.resolve("resultados.md")).exists();
        assertThat(DESTINO.resolve("resultados.csv")).exists();
    }

    // Execucao

    static Execucao executar(List<Relato> relatos, ExtratorSintomas extratorLlm, long pausaMs) {
        ExtratorPalavrasChave extratorPalavras = new ExtratorPalavrasChave();
        MotorRegrasTriagem motor = new MotorRegrasTriagem();
        List<Linha> linhas = new ArrayList<>();

        for (int i = 0; i < relatos.size(); i++) {
            Relato relato = relatos.get(i);
            if (i > 0) {
                pausar(pausaMs);
            }
            PerfilPet pet = new PerfilPet(relato.especie(), relato.idadeAnos());

            SintomasExtraidos sintomasPalavras = extratorPalavras.extrair(relato.texto());
            Urgencia obtidaPalavras = motor.avaliar(sintomasPalavras, pet).urgencia();

            SintomasExtraidos sintomasLlm = null;
            String status;
            long inicio = System.nanoTime();
            try {
                sintomasLlm = extratorLlm.extrair(relato.texto());
                status = "OK";
            } catch (FalhaLlmException e) {
                if (e.getMessage() != null && e.getMessage().startsWith("HTTP 429")) {
                    return new Execucao(linhas, relatos.size(), relato, e.getMessage());
                }
                status = "falha LLM: " + e.getMessage();
            } catch (RuntimeException e) {
                status = "falha LLM: " + e.getClass().getSimpleName();
            }
            long latenciaMs = (System.nanoTime() - inicio) / 1_000_000;
            Urgencia obtidaLlm = sintomasLlm == null ? null : motor.avaliar(sintomasLlm, pet).urgencia();

            linhas.add(new Linha(relato, sintomasPalavras, obtidaPalavras, sintomasLlm, obtidaLlm, latenciaMs, status));
        }
        return new Execucao(linhas, relatos.size(), null, null);
    }

    private static void pausar(long pausaMs) {
        if (pausaMs <= 0) {
            return;
        }
        try {
            Thread.sleep(pausaMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("avaliação interrompida", e);
        }
    }

    // Configuracao

    static GeminiProperties propriedadesDaAplicacao() throws IOException {
        StandardEnvironment ambiente = new StandardEnvironment();
        ambiente.getPropertySources().addLast(new ResourcePropertySource("classpath:application.properties"));
        return Binder.get(ambiente).bind("kuravet.ia.llm", GeminiProperties.class).get();
    }

    private static long pausaMs() {
        String valor = System.getenv("LLM_EVAL_PAUSA_MS");
        if (valor == null || valor.isBlank()) {
            return PAUSA_PADRAO_MS;
        }
        try {
            return Long.parseLong(valor.strip());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("LLM_EVAL_PAUSA_MS deve ser um número de milissegundos");
        }
    }

    // Leitura dos conjuntos

    static List<Relato> lerAjuste() throws IOException {
        return lerCsv(ARQUIVO_AJUSTE, 5, (numero, c) -> new Relato(CONJUNTO_AJUSTE, String.valueOf(numero),
                c[0].trim(), c[1].trim(), Integer.parseInt(c[2].trim()), Urgencia.valueOf(c[3].trim())));
    }

    static List<Relato> lerTeste() throws IOException {
        return lerCsv(ARQUIVO_TESTE, 6, (numero, c) -> new Relato(CONJUNTO_TESTE, c[0].trim(),
                c[1].trim(), c[2].trim(), Integer.parseInt(c[3].trim()), Urgencia.valueOf(c[4].trim())));
    }

    private interface LeitorLinha {
        Relato ler(int numero, String[] colunas);
    }

    private static List<Relato> lerCsv(String arquivo, int quantidadeColunas, LeitorLinha leitorLinha) throws IOException {
        List<Relato> relatos = new ArrayList<>();
        try (InputStream entrada = Objects.requireNonNull(
                AvaliacaoTriagemLlmTest.class.getResourceAsStream(arquivo), arquivo);
             BufferedReader leitor = new BufferedReader(new InputStreamReader(entrada, StandardCharsets.UTF_8))) {

            String linha;
            int numero = 0;
            while ((linha = leitor.readLine()) != null) {
                numero++;
                if (numero == 1 || linha.isBlank()) {
                    continue; // cabecalho
                }
                String[] colunas = linha.split(";", -1);
                if (colunas.length != quantidadeColunas) {
                    throw new IllegalStateException(arquivo + ", linha " + numero + ": esperadas "
                            + quantidadeColunas + " colunas, veio " + colunas.length);
                }
                relatos.add(leitorLinha.ler(numero, colunas));
            }
        }
        return relatos;
    }

    // Relatorio

    static void gravarRelatorio(Execucao execucao, String configuracao, Path destino) throws IOException {
        Files.createDirectories(destino);
        Files.writeString(destino.resolve("resultados.csv"), csv(execucao), StandardCharsets.UTF_8);
        Files.writeString(destino.resolve("resultados.md"), markdown(execucao, configuracao), StandardCharsets.UTF_8);
    }

    private static String csv(Execucao execucao) {
        StringBuilder texto = new StringBuilder("id;conjunto;especie;idade;esperado;obtido_palavras;obtido_llm;"
                + "sintomas_palavras;sintomas_llm;latencia_llm_ms;status_llm\n");
        for (Linha l : execucao.linhas()) {
            Relato r = l.relato();
            texto.append(String.join(";", r.id(), r.conjunto(), r.especie(), String.valueOf(r.idadeAnos()),
                    r.esperada().name(), l.obtidaPalavras().name(), l.llmOk() ? l.obtidaLlm().name() : "",
                    sintomas(l.sintomasPalavras()), sintomas(l.sintomasLlm()), String.valueOf(l.latenciaLlmMs()),
                    l.statusLlm().replace(';', ',').replace('\n', ' '))).append('\n');
        }
        return texto.toString();
    }

    private static String markdown(Execucao execucao, String configuracao) {
        StringBuilder md = new StringBuilder("# Avaliação da triagem: extração por LLM x palavras-chave\n\n")
                .append("- Gerado em: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")))
                .append("\n- Configuração: ").append(configuracao)
                .append("\n- Mesmo motor de regras e mesmo pet (espécie, idade) para os dois métodos; só a extração usa a LLM.\n")
                .append(String.format(Locale.ROOT, "- Relatos avaliados: %d de %d%n",
                        execucao.linhas().size(), execucao.totalRelatos()));

        if (execucao.interrompidoEm() != null) {
            md.append(String.format(Locale.ROOT, "%n> **Relatório PARCIAL**: a avaliação parou no relato `%s/%s` (%s). "
                            + "Esse relato e os seguintes não foram avaliados por nenhum dos métodos.%n",
                    execucao.interrompidoEm().conjunto(), execucao.interrompidoEm().id(), execucao.motivoInterrupcao()));
        }

        for (String conjunto : List.of(CONJUNTO_AJUSTE, CONJUNTO_TESTE)) {
            List<Linha> doConjunto = execucao.linhas().stream()
                    .filter(l -> l.relato().conjunto().equals(conjunto)).toList();
            md.append("\n## Conjunto \"").append(conjunto).append("\"\n\n");
            if (doConjunto.isEmpty()) {
                md.append("Sem relatos avaliados neste conjunto.\n");
                continue;
            }
            md.append(secaoMetodo("Palavras-chave", doConjunto, Linha::obtidaPalavras, false));
            md.append(secaoMetodo("LLM", doConjunto, Linha::obtidaLlm, true));
            md.append(divergencias(doConjunto));
        }
        return md.toString();
    }

    private static String secaoMetodo(String metodo, List<Linha> linhas, Function<Linha, Urgencia> obtida, boolean llm) {
        int total = linhas.size();
        List<Linha> comResultado = linhas.stream().filter(l -> obtida.apply(l) != null).toList();
        long acertos = comResultado.stream().filter(l -> obtida.apply(l) == l.relato().esperada()).count();
        long sub = comResultado.stream().filter(l -> obtida.apply(l).compareTo(l.relato().esperada()) < 0).count();
        long sobre = comResultado.stream().filter(l -> obtida.apply(l).compareTo(l.relato().esperada()) > 0).count();

        StringBuilder md = new StringBuilder("### ").append(metodo).append("\n\n")
                .append("| Métrica | Valor |\n|---|---|\n")
                .append(String.format(Locale.ROOT, "| Acurácia | %d/%d = %s |%n", acertos, total, pct(acertos, total)))
                .append(String.format(Locale.ROOT, "| Sub-triagem (obtido abaixo do esperado) | %d/%d = %s |%n", sub, total, pct(sub, total)))
                .append(String.format(Locale.ROOT, "| Sobre-triagem (obtido acima do esperado) | %d/%d = %s |%n", sobre, total, pct(sobre, total)));
        if (llm) {
            long falhas = total - comResultado.size();
            List<Long> latencias = linhas.stream().filter(Linha::llmOk).map(Linha::latenciaLlmMs).sorted().toList();
            md.append(String.format(Locale.ROOT, "| Falha da LLM (conta como erro na acurácia) | %d/%d = %s |%n", falhas, total, pct(falhas, total)))
                    .append(String.format(Locale.ROOT, "| Latência média (chamadas OK) | %s |%n",
                            latencias.isEmpty() ? "-" : Math.round(latencias.stream().mapToLong(Long::longValue).average().orElse(0)) + " ms"))
                    .append(String.format(Locale.ROOT, "| Latência p95 (chamadas OK) | %s |%n",
                            latencias.isEmpty() ? "-" : p95(latencias) + " ms"));
        }

        Map<Urgencia, Map<Urgencia, Integer>> matriz = new EnumMap<>(Urgencia.class);
        for (Linha l : comResultado) {
            matriz.computeIfAbsent(l.relato().esperada(), k -> new EnumMap<>(Urgencia.class))
                    .merge(obtida.apply(l), 1, Integer::sum);
        }
        md.append("\nMatriz de confusão (linhas = esperado, colunas = obtido")
                .append(llm ? "; falhas da LLM ficam fora" : "").append("):\n\n| esperado \\ obtido |");
        for (Urgencia u : Urgencia.values()) {
            md.append(' ').append(u).append(" |");
        }
        md.append("\n|---|").append("---|".repeat(Urgencia.values().length)).append('\n');
        for (Urgencia esperada : Urgencia.values()) {
            md.append("| **").append(esperada).append("** |");
            for (Urgencia o : Urgencia.values()) {
                md.append(' ').append(matriz.getOrDefault(esperada, Map.of()).getOrDefault(o, 0)).append(" |");
            }
            md.append('\n');
        }
        return md.append('\n').toString();
    }

    private static String divergencias(List<Linha> linhas) {
        List<Linha> divergentes = linhas.stream().filter(l -> l.obtidaPalavras() != l.obtidaLlm()).toList();
        StringBuilder md = new StringBuilder("### Relatos em que os métodos divergem (")
                .append(divergentes.size()).append(")\n\n");
        if (divergentes.isEmpty()) {
            return md.append("Nenhum.\n").toString();
        }
        md.append("| id | esperado | palavras-chave | LLM | sintomas palavras-chave | sintomas LLM | relato |\n")
                .append("|---|---|---|---|---|---|---|\n");
        for (Linha l : divergentes) {
            md.append(String.format("| %s | %s | %s | %s | %s | %s | %s |%n", l.relato().id(), l.relato().esperada(),
                    l.obtidaPalavras(), l.llmOk() ? l.obtidaLlm() : l.statusLlm(),
                    sintomasOuNenhum(l.sintomasPalavras()), l.llmOk() ? sintomasOuNenhum(l.sintomasLlm()) : "-",
                    l.relato().texto().replace("|", "\\|")));
        }
        return md.toString();
    }

    private static String sintomas(SintomasExtraidos extraidos) {
        if (extraidos == null) {
            return "";
        }
        return extraidos.sintomas().stream().sorted().map(Sintoma::name).collect(Collectors.joining(","));
    }

    private static String sintomasOuNenhum(SintomasExtraidos extraidos) {
        String texto = sintomas(extraidos);
        return texto.isEmpty() ? "(nenhum)" : texto;
    }

    private static String pct(long parte, long total) {
        return total == 0 ? "-" : String.format(Locale.ROOT, "%.1f%%", 100.0 * parte / total);
    }

    private static long p95(List<Long> ordenadas) {
        int posicao = (int) Math.ceil(0.95 * ordenadas.size()) - 1;
        return ordenadas.get(Math.max(posicao, 0));
    }
}
