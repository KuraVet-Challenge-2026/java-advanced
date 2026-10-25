package br.com.fiap.kuravet.ia.llm;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

import java.time.Duration;

/**
 * Chamada comum ao generateContent do Google Gemini: monta o envelope da requisicao,
 * envia com a chave no header e devolve o texto do primeiro candidato.
 * Qualquer falha lanca {@link FalhaLlmException} com apenas o motivo.
 */
@Component
@EnableConfigurationProperties(GeminiProperties.class)
public class ClienteGemini {

    private final ObjectMapper mapper = JsonMapper.builder().build();
    private final GeminiProperties properties;
    private final RestClient restClient;

    @Autowired
    public ClienteGemini(GeminiProperties properties) {
        this(properties, criarRestClient(properties));
    }

    public ClienteGemini(GeminiProperties properties, RestClient restClient) {
        this.properties = properties;
        this.restClient = restClient;
    }

    private static RestClient criarRestClient(GeminiProperties properties) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofMillis(properties.timeoutMs()));
        factory.setReadTimeout(Duration.ofMillis(properties.timeoutMs()));
        return RestClient.builder().requestFactory(factory).build();
    }

    /** Envelope com a instrucao de sistema e uma mensagem do usuario; o chamador completa o generationConfig. */
    public ObjectNode novaRequisicao(String instrucaoSistema, String textoUsuario) {
        ObjectNode raiz = mapper.createObjectNode();
        raiz.putObject("systemInstruction").putArray("parts").addObject().put("text", instrucaoSistema);
        ObjectNode conteudo = raiz.putArray("contents").addObject();
        conteudo.put("role", "user");
        conteudo.putArray("parts").addObject().put("text", textoUsuario);
        return raiz;
    }

    /** Relato entre {@code <relato></relato>}; o delimitador nao pode ser fechado de dentro do relato. */
    public static String relatoDelimitado(String relato) {
        String relatoSeguro = relato == null ? "" : relato.replace("</relato>", "").replace("<relato>", "");
        return "<relato>" + relatoSeguro + "</relato>";
    }

    public String gerarTexto(ObjectNode requisicao) {
        String resposta;
        try {
            resposta = restClient.post()
                    .uri(properties.baseUrl() + "/models/{model}:generateContent", properties.model())
                    .header("x-goog-api-key", properties.apiKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(mapper.writeValueAsString(requisicao))
                    .retrieve()
                    .body(String.class);
        } catch (RestClientResponseException e) {
            // A mensagem da excecao original inclui o corpo da resposta: nao propagar.
            throw new FalhaLlmException("HTTP " + e.getStatusCode().value());
        } catch (RestClientException e) {
            throw new FalhaLlmException("erro de comunicação (" + e.getClass().getSimpleName() + ")");
        }

        return textoDoCandidato(resposta);
    }

    private String textoDoCandidato(String resposta) {
        if (resposta == null || resposta.isBlank()) {
            throw new FalhaLlmException("resposta vazia");
        }

        JsonNode envelope;
        try {
            envelope = mapper.readTree(resposta);
        } catch (RuntimeException e) {
            throw new FalhaLlmException("resposta não é JSON válido");
        }

        if (envelope.path("promptFeedback").hasNonNull("blockReason")) {
            throw new FalhaLlmException("requisição bloqueada pela LLM");
        }

        JsonNode candidato = envelope.path("candidates").path(0);
        if (candidato.isMissingNode() || candidato.isNull()) {
            throw new FalhaLlmException("resposta sem candidates");
        }
        String finishReason = candidato.path("finishReason").asString("");
        if ("SAFETY".equals(finishReason) || "BLOCKLIST".equals(finishReason)
                || "PROHIBITED_CONTENT".equals(finishReason)) {
            throw new FalhaLlmException("resposta bloqueada por segurança");
        }

        JsonNode texto = candidato.path("content").path("parts").path(0).path("text");
        if (!texto.isString()) {
            throw new FalhaLlmException("resposta sem texto");
        }
        return texto.asString();
    }
}
