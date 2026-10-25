package br.com.fiap.kuravet.ia;

import br.com.fiap.kuravet.ia.llm.FalhaLlmException;
import br.com.fiap.kuravet.ia.llm.GeminiProperties;
import br.com.fiap.kuravet.ia.llm.GeradorOrientacaoGemini;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GeradorOrientacaoComFallbackTest {

    private static final ContextoOrientacao CONTEXTO = new ContextoOrientacao("Thor", "Cachorro", 4,
            Urgencia.ALTA, "Solicite uma teleconsulta prioritária ainda hoje.",
            Set.of(Sintoma.VOMITO, Sintoma.DIARREIA), "vomitou e está com diarreia", List.of());

    @Mock
    private GeradorOrientacaoGemini gemini;

    private GeradorOrientacaoComFallback criar(boolean habilitada, String chave) {
        GeminiProperties propriedades = new GeminiProperties(habilitada, chave, "http://localhost", "modelo", 1000);
        return new GeradorOrientacaoComFallback(propriedades, gemini, new GeradorOrientacaoModeloFixo());
    }

    @Test
    void semChaveDeveUsarModeloFixoSemChamarOGemini() {
        OrientacaoGerada orientacao = criar(true, "").gerar(CONTEXTO);

        assertThat(orientacao.origem()).isEqualTo(OrigemOrientacao.MODELO_FIXO);
        assertThat(orientacao.texto()).isEqualTo("Sintomas identificados em Thor: vômito e diarreia. "
                + "Como a urgência é ALTA, solicite uma teleconsulta ainda hoje. "
                + GeradorOrientacaoComFallback.AVISO);
        verifyNoInteractions(gemini);
    }

    @Test
    void llmDesabilitadaDeveUsarModeloFixoSemChamarOGemini() {
        OrientacaoGerada orientacao = criar(false, "chave-de-teste").gerar(CONTEXTO);

        assertThat(orientacao.origem()).isEqualTo(OrigemOrientacao.MODELO_FIXO);
        verifyNoInteractions(gemini);
    }

    @Test
    void geminiOkDeveRetornarIaGenerativaComAviso() {
        when(gemini.gerar(CONTEXTO)).thenReturn("Thor está com vômito e diarreia; procure o veterinário hoje.");

        OrientacaoGerada orientacao = criar(true, "chave-de-teste").gerar(CONTEXTO);

        assertThat(orientacao.origem()).isEqualTo(OrigemOrientacao.IA_GENERATIVA);
        assertThat(orientacao.texto()).isEqualTo("Thor está com vômito e diarreia; procure o veterinário hoje. "
                + GeradorOrientacaoComFallback.AVISO);
    }

    @Test
    void falhaDoGeminiDeveCairParaModeloFixo() {
        when(gemini.gerar(CONTEXTO)).thenThrow(new FalhaLlmException("HTTP 503"));

        OrientacaoGerada orientacao = criar(true, "chave-de-teste").gerar(CONTEXTO);

        assertThat(orientacao.origem()).isEqualTo(OrigemOrientacao.MODELO_FIXO);
        assertThat(orientacao.texto()).endsWith(GeradorOrientacaoComFallback.AVISO);
    }

    @Test
    void excecaoInesperadaDeveCairParaModeloFixo() {
        when(gemini.gerar(CONTEXTO)).thenThrow(new IllegalStateException("qualquer erro"));

        OrientacaoGerada orientacao = criar(true, "chave-de-teste").gerar(CONTEXTO);

        assertThat(orientacao.origem()).isEqualTo(OrigemOrientacao.MODELO_FIXO);
        assertThat(orientacao.texto()).endsWith(GeradorOrientacaoComFallback.AVISO);
    }

    @Test
    void modeloFixoSemSintomasDeveDizerQueNaoIdentificou() {
        ContextoOrientacao semSintomas = new ContextoOrientacao("Luna", "Gato", 2, Urgencia.BAIXA,
                "Descreva melhor os sintomas.", Set.of(), "está estranha", List.of());

        OrientacaoGerada orientacao = criar(false, "").gerar(semSintomas);

        assertThat(orientacao.texto())
                .startsWith("Não identificamos sintomas específicos no relato sobre Luna.")
                .contains("Como a urgência é BAIXA")
                .endsWith(GeradorOrientacaoComFallback.AVISO);
    }
}
