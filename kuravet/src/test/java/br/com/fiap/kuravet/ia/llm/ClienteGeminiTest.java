package br.com.fiap.kuravet.ia.llm;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.node.ObjectNode;

import java.net.SocketTimeoutException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class ClienteGeminiTest {

    private static final String CHAVE_FALSA = "chave-de-teste-nao-e-real";
    private static final String BASE_URL = "https://gemini.teste/v1beta";
    private static final String URL = BASE_URL + "/models/modelo-teste:generateContent";
    private static final String RELATO = "meu gato vomitou sangue";
    private static final String RESPOSTA_OK =
            "{\"candidates\":[{\"content\":{\"parts\":[{\"text\":\"ok\"}]},\"finishReason\":\"STOP\"}]}";

    private MockRestServiceServer server;

    private ClienteGemini cliente(String thinkingLevel) {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        GeminiProperties propriedades =
                new GeminiProperties(true, CHAVE_FALSA, BASE_URL, "modelo-teste", 1000, thinkingLevel);
        return new ClienteGemini(propriedades, builder.build());
    }

    private static ObjectNode requisicao(ClienteGemini cliente) {
        ObjectNode requisicao = cliente.novaRequisicao("sistema", ClienteGemini.relatoDelimitado(RELATO));
        cliente.configuracaoGeracao(requisicao).put("temperature", 0);
        return requisicao;
    }

    @Test
    void deveEnviarSoThinkingLevelQuandoConfigurado() {
        ClienteGemini cliente = cliente("low");
        server.expect(requestTo(URL))
                .andExpect(jsonPath("$.generationConfig.thinkingConfig.thinkingLevel").value("low"))
                .andExpect(jsonPath("$.generationConfig.thinkingConfig.thinkingBudget").doesNotExist())
                .andExpect(jsonPath("$.generationConfig.temperature").value(0))
                .andRespond(withSuccess(RESPOSTA_OK, MediaType.APPLICATION_JSON));

        cliente.gerarTexto(requisicao(cliente));

        server.verify();
    }

    @ParameterizedTest
    @CsvSource(value = {"''", "'   '", "NULO"}, nullValues = "NULO")
    void naoDeveEnviarThinkingConfigQuandoVazio(String thinkingLevel) {
        ClienteGemini cliente = cliente(thinkingLevel);
        server.expect(requestTo(URL))
                .andExpect(jsonPath("$.generationConfig.thinkingConfig").doesNotExist())
                .andExpect(jsonPath("$.generationConfig.temperature").value(0))
                .andRespond(withSuccess(RESPOSTA_OK, MediaType.APPLICATION_JSON));

        cliente.gerarTexto(requisicao(cliente));

        server.verify();
    }

    @ParameterizedTest
    @CsvSource({
            "BAD_REQUEST, HTTP 400 (requisição rejeitada)",
            "TOO_MANY_REQUESTS, HTTP 429 (cota excedida)",
            "SERVICE_UNAVAILABLE, HTTP 503 (modelo indisponível)",
            "INTERNAL_SERVER_ERROR, HTTP 500"
    })
    void falhaHttpDeveInformarSoOStatus(HttpStatus status, String motivo) {
        ClienteGemini cliente = cliente("low");
        server.expect(requestTo(URL))
                .andRespond(withStatus(status)
                        .body("{\"error\":{\"message\":\"" + RELATO + " " + CHAVE_FALSA + "\"}}")
                        .contentType(MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> cliente.gerarTexto(requisicao(cliente)))
                .isInstanceOf(FalhaLlmException.class)
                .hasMessage(motivo);
    }

    @Test
    void timeoutDeveSerInformadoComoTimeout() {
        ClienteGemini cliente = cliente("low");
        server.expect(requestTo(URL))
                .andRespond(withException(new SocketTimeoutException("Read timed out")));

        assertThatThrownBy(() -> cliente.gerarTexto(requisicao(cliente)))
                .isInstanceOf(FalhaLlmException.class)
                .hasMessage("timeout após 1000 ms");
    }
}
