package br.com.fiap.kuravet.ia.llm;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuracao da extracao de sintomas via Google Gemini.
 *
 * @param enabled    liga/desliga a chamada a LLM (desligada = so palavras-chave)
 * @param apiKey     chave da API; vazia = LLM indisponivel. Nunca versionar.
 * @param baseUrl    URL base da API (ex.: https://generativelanguage.googleapis.com/v1beta)
 * @param model      modelo usado em generateContent
 * @param timeoutMs  timeout de conexao e de leitura, em milissegundos
 */
@ConfigurationProperties("kuravet.ia.llm")
public record GeminiProperties(boolean enabled, String apiKey, String baseUrl, String model, int timeoutMs) {

    public boolean temChave() {
        return apiKey != null && !apiKey.isBlank();
    }
}
