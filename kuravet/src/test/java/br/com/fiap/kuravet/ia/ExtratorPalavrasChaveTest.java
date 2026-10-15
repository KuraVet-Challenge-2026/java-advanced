package br.com.fiap.kuravet.ia;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;
import java.util.stream.Stream;

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
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class ExtratorPalavrasChaveTest {

    private final ExtratorSintomas extrator = new ExtratorPalavrasChave();

    static Stream<Arguments> relatos() {
        return Stream.of(
                arguments("Meu cachorro vomitou duas vezes e está com diarreia há 3 dias", Set.of(VOMITO, DIARREIA), 3),
                arguments("Ele não vomitou, mas está coçando muito", Set.of(COCEIRA), null),
                arguments("Comeu chocolate ontem", Set.of(INGESTAO_TOXICA), 1),
                arguments("Está com tosse há uma semana", Set.of(TOSSE), 7),
                arguments("Choveu muito e ele ficou molhado", Set.of(), null),
                arguments("Passou a tarde toda na chuva", Set.of(), null),
                arguments("MINHA GATA ESTÁ APÁTICA E NÃO COME HÁ DOIS DIAS", Set.of(APATIA, FALTA_DE_APETITE), 2),
                arguments("O gato NÃO faz Xixi desde hoje cedo", Set.of(NAO_URINA), 0),
                arguments("Está ofegante e com a língua roxa", Set.of(DIFICULDADE_RESPIRAR), null),
                arguments("Teve uma convulsão hoje de manhã", Set.of(CONVULSAO), 0),
                arguments("Está mancando há 2 semanas", Set.of(MANCANDO), 14),
                arguments("Está espirrando muito e com febre há 4 dias", Set.of(ESPIRROS, FEBRE), 4),
                arguments("Comeu uvas no quintal depois da chuva", Set.of(INGESTAO_TOXICA), null),
                arguments("Tem uma ferida na pata que está sangrando", Set.of(FERIDA, SANGRAMENTO), null),
                arguments("Desmaiou ontem e de novo hoje", Set.of(DESMAIO), 1),
                arguments("Não vomitou nem teve diarreia", Set.of(), null),
                arguments("Sem febre, mas está tossindo", Set.of(TOSSE), null),
                arguments("Está com fezes moles e sem energia há cinco dias", Set.of(DIARREIA, APATIA), 5),
                arguments("Acho que ingeriu raticida", Set.of(INGESTAO_TOXICA), null),
                arguments("Vomitando há 1 dia, piorou faz 3 dias e há uma semana já estava estranho", Set.of(VOMITO), 7),
                arguments("Não come e não comeu chocolate", Set.of(FALTA_DE_APETITE), null)
        );
    }

    @ParameterizedTest(name = "\"{0}\" -> {1}, {2} dias")
    @MethodSource("relatos")
    void deveExtrairSintomasEDuracaoDoRelato(String relato, Set<Sintoma> esperados, Integer duracaoEsperada) {
        SintomasExtraidos extraidos = extrator.extrair(relato);

        assertThat(extraidos.sintomas()).containsExactlyInAnyOrderElementsOf(esperados);
        assertThat(extraidos.duracaoDias()).isEqualTo(duracaoEsperada);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t\n"})
    void relatoNuloOuVazioDeveResultarEmExtracaoVazia(String relato) {
        SintomasExtraidos extraidos = extrator.extrair(relato);

        assertThat(extraidos.sintomas()).isEmpty();
        assertThat(extraidos.duracaoDias()).isNull();
    }

    @Test
    void metodoDeveSerPalavrasChave() {
        assertThat(extrator.metodo()).isEqualTo(MetodoExtracao.PALAVRAS_CHAVE);
    }

    @Test
    void dicionarioDeveCobrirTodosOsSintomasComPeloMenosTresTermos() {
        for (Sintoma sintoma : Sintoma.values()) {
            assertThat(ExtratorPalavrasChave.DICIONARIO.get(sintoma))
                    .as("termos de %s", sintoma)
                    .hasSizeGreaterThanOrEqualTo(3)
                    .allSatisfy(termo -> assertThat(ExtratorPalavrasChave.normalizar(termo)).isEqualTo(termo));
        }
    }

    @Test
    void normalizacaoDeveRemoverAcentosCaixaEEspacosExtras() {
        assertThat(ExtratorPalavrasChave.normalizar("  Está   APÁTICO,\tcoçando!  "))
                .isEqualTo("esta apatico cocando");
    }
}
