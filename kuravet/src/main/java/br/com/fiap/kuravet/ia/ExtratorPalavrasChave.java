package br.com.fiap.kuravet.ia;

import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extrai sintomas do relato por palavras-chave. E o fallback da extracao por LLM.
 * O relato e dado do tutor e nunca deve ser logado.
 */
@Component
public class ExtratorPalavrasChave implements ExtratorSintomas {

    /** Termos ja normalizados (minusculas, sem acento). */
    static final Map<Sintoma, List<String>> DICIONARIO = criarDicionario();

    private static final Set<String> NEGACOES = Set.of("nao", "nem", "sem");
    private static final int JANELA_NEGACAO = 2;

    private static final Map<String, Integer> NUMEROS_POR_EXTENSO = Map.of(
            "um", 1, "uma", 1, "dois", 2, "duas", 2, "tres", 3,
            "quatro", 4, "cinco", 5, "seis", 6, "sete", 7);

    private static final Pattern DURACAO = Pattern.compile(
            "\\b(\\d{1,4}|um|uma|dois|duas|tres|quatro|cinco|seis|sete) (dias?|semanas?)\\b");
    private static final Pattern ONTEM = Pattern.compile("\\bontem\\b");
    private static final Pattern HOJE = Pattern.compile("\\bhoje\\b");

    private final Map<Sintoma, List<Termo>> termos = compilarTermos();

    @Override
    public SintomasExtraidos extrair(String relato) {
        if (relato == null || relato.isBlank()) {
            return new SintomasExtraidos(Set.of(), null);
        }
        String texto = normalizar(relato);

        Set<Sintoma> encontrados = EnumSet.noneOf(Sintoma.class);
        termos.forEach((sintoma, lista) -> {
            if (lista.stream().anyMatch(termo -> termo.casaSemNegacao(texto))) {
                encontrados.add(sintoma);
            }
        });
        return new SintomasExtraidos(encontrados, extrairDuracao(texto));
    }

    @Override
    public MetodoExtracao metodo() {
        return MetodoExtracao.PALAVRAS_CHAVE;
    }

    static String normalizar(String texto) {
        String semAcento = Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return semAcento.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", " ")
                .trim();
    }

    private static boolean negadoAntes(String texto, int inicio) {
        String antes = texto.substring(0, inicio).trim();
        if (antes.isEmpty()) {
            return false;
        }
        String[] palavras = antes.split(" ");
        for (int i = Math.max(0, palavras.length - JANELA_NEGACAO); i < palavras.length; i++) {
            if (NEGACOES.contains(palavras[i])) {
                return true;
            }
        }
        return false;
    }

    private static Integer extrairDuracao(String texto) {
        Integer maior = null;
        Matcher matcher = DURACAO.matcher(texto);
        while (matcher.find()) {
            String quantidade = matcher.group(1);
            int valor = NUMEROS_POR_EXTENSO.getOrDefault(quantidade, -1);
            if (valor < 0) {
                valor = Integer.parseInt(quantidade);
            }
            if (matcher.group(2).startsWith("semana")) {
                valor *= 7;
            }
            maior = maior == null ? valor : Math.max(maior, valor);
        }
        if (ONTEM.matcher(texto).find()) {
            maior = maior == null ? 1 : Math.max(maior, 1);
        }
        if (HOJE.matcher(texto).find()) {
            maior = maior == null ? 0 : Math.max(maior, 0);
        }
        return maior;
    }

    private static Map<Sintoma, List<Termo>> compilarTermos() {
        Map<Sintoma, List<Termo>> compilados = new EnumMap<>(Sintoma.class);
        DICIONARIO.forEach((sintoma, lista) -> compilados.put(sintoma, lista.stream().map(Termo::new).toList()));
        return compilados;
    }

    /**
     * Termo do dicionario casado com limites de palavra. Termos que ja comecam com negacao
     * ("nao come", "sem urinar") sao casados inteiros e nao passam pela regra de negacao.
     */
    private record Termo(Pattern padrao, boolean jaNegado) {

        Termo(String termo) {
            this(Pattern.compile("\\b" + Pattern.quote(termo) + "\\b"),
                    NEGACOES.contains(termo.split(" ")[0]));
        }

        boolean casaSemNegacao(String texto) {
            Matcher matcher = padrao.matcher(texto);
            while (matcher.find()) {
                if (jaNegado || !negadoAntes(texto, matcher.start())) {
                    return true;
                }
            }
            return false;
        }
    }

    private static Map<Sintoma, List<String>> criarDicionario() {
        Map<Sintoma, List<String>> dicionario = new EnumMap<>(Sintoma.class);
        dicionario.put(Sintoma.VOMITO, List.of(
                "vomito", "vomitos", "vomitou", "vomitando", "vomitar", "vomita",
                "enjoo", "enjoado", "enjoada", "botou pra fora", "botando pra fora"));
        dicionario.put(Sintoma.DIARREIA, List.of(
                "diarreia", "fezes moles", "fezes mole", "coco mole", "fezes liquidas", "intestino solto",
                "caganeira"));
        dicionario.put(Sintoma.FALTA_DE_APETITE, List.of(
                "nao come", "nao quer comer", "nao esta comendo", "nao ta comendo", "sem comer", "sem apetite",
                "parou de comer", "recusa comida", "recusando comida", "falta de apetite"));
        dicionario.put(Sintoma.APATIA, List.of(
                "apatico", "apatica", "apatia", "desanimado", "desanimada",
                "prostrado", "prostrada", "sem energia", "abatido", "abatida"));
        dicionario.put(Sintoma.FEBRE, List.of(
                "febre", "febril", "corpo quente", "temperatura alta"));
        dicionario.put(Sintoma.TOSSE, List.of(
                "tosse", "tossindo", "tossiu", "tossir", "tosse seca"));
        dicionario.put(Sintoma.ESPIRROS, List.of(
                "espirro", "espirros", "espirrando", "espirrou", "espirrar"));
        dicionario.put(Sintoma.COCEIRA, List.of(
                "coceira", "cocando", "cocou", "se coca", "cocar", "comichao"));
        dicionario.put(Sintoma.FERIDA, List.of(
                "ferida", "feridas", "ferido", "ferimento", "machucado", "machucada", "lesao"));
        dicionario.put(Sintoma.MANCANDO, List.of(
                "mancando", "manca", "mancou", "nao apoia a pata", "puxando a pata", "mancar"));
        dicionario.put(Sintoma.DIFICULDADE_RESPIRAR, List.of(
                "falta de ar", "respirando com dificuldade", "dificuldade para respirar",
                "dificuldade de respirar", "ofegante", "lingua roxa", "sem ar"));
        dicionario.put(Sintoma.CONVULSAO, List.of(
                "convulsao", "convulsoes", "convulsionando", "convulsionou",
                "crise epileptica", "ataque epileptico"));
        dicionario.put(Sintoma.SANGRAMENTO, List.of(
                "sangramento", "sangrando", "sangrou", "sangue", "hemorragia"));
        dicionario.put(Sintoma.INGESTAO_TOXICA, List.of(
                "chocolate", "veneno", "envenenado", "envenenada", "comeu remedio",
                "engoliu remedio", "uva", "uvas", "raticida", "veneno de rato"));
        dicionario.put(Sintoma.NAO_URINA, List.of(
                "nao faz xixi", "nao fez xixi", "nao urina", "nao urinou", "sem urinar",
                "nao consegue urinar", "nao consegue fazer xixi", "nao esta urinando",
                "nao ta urinando", "nao esta fazendo xixi", "nao ta fazendo xixi"));
        dicionario.put(Sintoma.DESMAIO, List.of(
                "desmaio", "desmaiou", "desmaiando", "desmaiar", "desfaleceu", "perdeu a consciencia"));
        return dicionario;
    }
}
