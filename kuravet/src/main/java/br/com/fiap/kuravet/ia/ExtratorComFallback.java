package br.com.fiap.kuravet.ia;

import br.com.fiap.kuravet.ia.llm.ExtratorGemini;
import br.com.fiap.kuravet.ia.llm.FalhaExtracaoLlmException;
import br.com.fiap.kuravet.ia.llm.GeminiProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Tenta extrair os sintomas com a LLM e, em qualquer falha, usa o extrator de palavras-chave.
 * Informa o metodo que realmente produziu o resultado.
 */
@Component
public class ExtratorComFallback {

    private static final Logger log = LoggerFactory.getLogger(ExtratorComFallback.class);

    private final GeminiProperties properties;
    private final ExtratorGemini extratorGemini;
    private final ExtratorPalavrasChave extratorPalavrasChave;

    public ExtratorComFallback(GeminiProperties properties,
                               ExtratorGemini extratorGemini,
                               ExtratorPalavrasChave extratorPalavrasChave) {
        this.properties = properties;
        this.extratorGemini = extratorGemini;
        this.extratorPalavrasChave = extratorPalavrasChave;
    }

    public ResultadoExtracao extrair(String relato) {
        if (properties.enabled() && properties.temChave()) {
            try {
                return new ResultadoExtracao(extratorGemini.extrair(relato), MetodoExtracao.LLM);
            } catch (FalhaExtracaoLlmException e) {
                log.warn("Extração com LLM falhou, usando palavras-chave: {}", e.getMessage());
            } catch (RuntimeException e) {
                // So o tipo: a mensagem pode carregar trechos do relato ou da resposta.
                log.warn("Extração com LLM falhou, usando palavras-chave: {}", e.getClass().getSimpleName());
            }
        }
        return new ResultadoExtracao(extratorPalavrasChave.extrair(relato), MetodoExtracao.PALAVRAS_CHAVE);
    }
}
