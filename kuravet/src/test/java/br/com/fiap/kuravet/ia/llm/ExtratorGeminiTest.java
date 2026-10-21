package br.com.fiap.kuravet.ia.llm;

import br.com.fiap.kuravet.ia.MetodoExtracao;
import br.com.fiap.kuravet.ia.Sintoma;
import br.com.fiap.kuravet.ia.SintomasExtraidos;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class ExtratorGeminiTest {

    private static final String CHAVE_FALSA = "chave-de-teste-nao-e-real";
    private static final String BASE_URL = "https://gemini.teste/v1beta";
    private static final String URL = BASE_URL + "/models/modelo-teste:generateContent";

    private MockRestServiceServer server;
    private ExtratorGemini extrator;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        GeminiProperties propriedades = new GeminiProperties(true, CHAVE_FALSA, BASE_URL, "modelo-teste", 1000);
        extrator = new ExtratorGemini(propriedades, builder.build());
    }

    /** Monta a resposta do Gemini, com o JSON extraido dentro de candidates[0].content.parts[0].text. */
    private static String respostaGemini(String jsonExtraido) {
        String textoEscapado = jsonExtraido.replace("\\", "\\\\").replace("\"", "\\\"");
        return "{\"candidates\":[{\"content\":{\"parts\":[{\"text\":\"" + textoEscapado + "\"}]},"
                + "\"finishReason\":\"STOP\"}]}";
    }

    @Test
    void deveExtrairSintomasEDuracaoDaResposta() {
        server.expect(requestTo(URL))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("x-goog-api-key", CHAVE_FALSA))
                .andExpect(content().string(containsString("<relato>meu cachorro vomita há 2 dias</relato>")))
                .andExpect(content().string(containsString("\"temperature\":0")))
                .andExpect(content().string(containsString("application/json")))
                .andExpect(content().string(not(containsString(CHAVE_FALSA))))
                .andRespond(withSuccess(
                        respostaGemini("{\"sintomas\":[\"VOMITO\",\"APATIA\"],\"duracaoDias\":2}"),
                        MediaType.APPLICATION_JSON));

        SintomasExtraidos resultado = extrator.extrair("meu cachorro vomita há 2 dias");

        assertThat(resultado.sintomas()).containsExactlyInAnyOrder(Sintoma.VOMITO, Sintoma.APATIA);
        assertThat(resultado.duracaoDias()).isEqualTo(2);
        server.verify();
    }

    @Test
    void naoDeveColocarAChaveNaUrl() {
        server.expect(requestTo(not(containsString(CHAVE_FALSA))))
                .andExpect(requestTo(URL))
                .andRespond(withSuccess(respostaGemini("{\"sintomas\":[]}"), MediaType.APPLICATION_JSON));

        extrator.extrair("qualquer relato");

        server.verify();
    }

    @Test
    void deveIgnorarSintomaForaDoEnum() {
        server.expect(requestTo(URL))
                .andRespond(withSuccess(
                        respostaGemini("{\"sintomas\":[\"TOSSE\",\"SINTOMA_INVENTADO\"],\"duracaoDias\":null}"),
                        MediaType.APPLICATION_JSON));

        SintomasExtraidos resultado = extrator.extrair("tosse");

        assertThat(resultado.sintomas()).containsExactly(Sintoma.TOSSE);
        assertThat(resultado.duracaoDias()).isNull();
    }

    @Test
    void duracaoForaDoIntervaloDeveVirarNull() {
        server.expect(requestTo(URL))
                .andRespond(withSuccess(
                        respostaGemini("{\"sintomas\":[\"TOSSE\"],\"duracaoDias\":999}"),
                        MediaType.APPLICATION_JSON));

        assertThat(extrator.extrair("tosse").duracaoDias()).isNull();
    }

    @Test
    void duracaoNegativaDeveVirarNull() {
        server.expect(requestTo(URL))
                .andRespond(withSuccess(
                        respostaGemini("{\"sintomas\":[\"TOSSE\"],\"duracaoDias\":-3}"),
                        MediaType.APPLICATION_JSON));

        assertThat(extrator.extrair("tosse").duracaoDias()).isNull();
    }

    @Test
    void deveFalharQuandoStatusNaoForSucesso() {
        server.expect(requestTo(URL))
                .andRespond(withStatus(org.springframework.http.HttpStatus.UNAUTHORIZED)
                        .body("{\"error\":\"detalhe que nao pode vazar\"}")
                        .contentType(MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> extrator.extrair("tosse"))
                .isInstanceOf(FalhaExtracaoLlmException.class)
                .hasMessage("HTTP 401");
    }

    @Test
    void deveFalharComJsonInvalido() {
        server.expect(requestTo(URL))
                .andRespond(withSuccess("isto nao e json", MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> extrator.extrair("tosse"))
                .isInstanceOf(FalhaExtracaoLlmException.class);
    }

    @Test
    void deveFalharQuandoOTextoInternoNaoForJson() {
        server.expect(requestTo(URL))
                .andRespond(withSuccess(respostaGemini("nao sou json"), MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> extrator.extrair("tosse"))
                .isInstanceOf(FalhaExtracaoLlmException.class);
    }

    @Test
    void deveFalharSemCandidates() {
        server.expect(requestTo(URL))
                .andRespond(withSuccess("{\"candidates\":[]}", MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> extrator.extrair("tosse"))
                .isInstanceOf(FalhaExtracaoLlmException.class)
                .hasMessage("resposta sem candidates");
    }

    @Test
    void deveFalharQuandoBloqueadaPorSeguranca() {
        server.expect(requestTo(URL))
                .andRespond(withSuccess("{\"promptFeedback\":{\"blockReason\":\"SAFETY\"}}",
                        MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> extrator.extrair("tosse"))
                .isInstanceOf(FalhaExtracaoLlmException.class);
    }

    @Test
    void metodoDeveSerLlm() {
        assertThat(extrator.metodo()).isEqualTo(MetodoExtracao.LLM);
    }
}
