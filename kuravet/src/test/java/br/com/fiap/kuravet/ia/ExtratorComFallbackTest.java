package br.com.fiap.kuravet.ia;

import br.com.fiap.kuravet.ia.llm.ExtratorGemini;
import br.com.fiap.kuravet.ia.llm.FalhaExtracaoLlmException;
import br.com.fiap.kuravet.ia.llm.GeminiProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class ExtratorComFallbackTest {

    private static final String RELATO = "meu cachorro está com tosse";

    @Mock
    private ExtratorGemini gemini;

    private final ExtratorPalavrasChave palavrasChave = new ExtratorPalavrasChave();

    private ExtratorComFallback comChave;

    @BeforeEach
    void setUp() {
        comChave = criar(true, "chave-de-teste");
    }

    private ExtratorComFallback criar(boolean habilitada, String chave) {
        GeminiProperties propriedades = new GeminiProperties(habilitada, chave, "http://localhost", "modelo", 1000);
        return new ExtratorComFallback(propriedades, gemini, palavrasChave);
    }

    @Test
    void chaveVaziaDeveUsarPalavrasChaveSemChamarOGemini() {
        ResultadoExtracao resultado = criar(true, "").extrair(RELATO);

        assertThat(resultado.metodo()).isEqualTo(MetodoExtracao.PALAVRAS_CHAVE);
        assertThat(resultado.sintomas().sintomas()).contains(Sintoma.TOSSE);
        verifyNoInteractions(gemini);
    }

    @Test
    void chaveNulaDeveUsarPalavrasChaveSemChamarOGemini() {
        ResultadoExtracao resultado = criar(true, null).extrair(RELATO);

        assertThat(resultado.metodo()).isEqualTo(MetodoExtracao.PALAVRAS_CHAVE);
        verify(gemini, never()).extrair(anyString());
    }

    @Test
    void llmDesabilitadaDeveUsarPalavrasChaveSemChamarOGemini() {
        ResultadoExtracao resultado = criar(false, "chave-de-teste").extrair(RELATO);

        assertThat(resultado.metodo()).isEqualTo(MetodoExtracao.PALAVRAS_CHAVE);
        verifyNoInteractions(gemini);
    }

    @Test
    void geminiOkDeveRetornarLlm() {
        SintomasExtraidos esperado = new SintomasExtraidos(Set.of(Sintoma.FEBRE), 3);
        when(gemini.extrair(RELATO)).thenReturn(esperado);

        ResultadoExtracao resultado = comChave.extrair(RELATO);

        assertThat(resultado.metodo()).isEqualTo(MetodoExtracao.LLM);
        assertThat(resultado.sintomas()).isEqualTo(esperado);
    }

    @Test
    void falhaDoGeminiDeveCairParaPalavrasChave() {
        when(gemini.extrair(RELATO)).thenThrow(new FalhaExtracaoLlmException("HTTP 503"));

        ResultadoExtracao resultado = comChave.extrair(RELATO);

        assertThat(resultado.metodo()).isEqualTo(MetodoExtracao.PALAVRAS_CHAVE);
        assertThat(resultado.sintomas().sintomas()).contains(Sintoma.TOSSE);
    }

    @Test
    void excecaoInesperadaDeveCairParaPalavrasChave() {
        when(gemini.extrair(RELATO)).thenThrow(new IllegalStateException("qualquer erro"));

        ResultadoExtracao resultado = comChave.extrair(RELATO);

        assertThat(resultado.metodo()).isEqualTo(MetodoExtracao.PALAVRAS_CHAVE);
    }
}
