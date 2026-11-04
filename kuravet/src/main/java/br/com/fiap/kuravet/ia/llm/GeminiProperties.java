package br.com.fiap.kuravet.ia.llm;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("kuravet.ia.llm")
public record GeminiProperties(boolean enabled, String apiKey, String baseUrl, String model, int timeoutMs,
                               String thinkingLevel) {

    public boolean temChave() {
        return apiKey != null && !apiKey.isBlank();
    }

    public boolean temThinkingLevel() {
        return thinkingLevel != null && !thinkingLevel.isBlank();
    }
}
