package br.com.fiap.kuravet.ia;

import br.com.fiap.kuravet.ia.llm.FalhaLlmException;
import br.com.fiap.kuravet.ia.llm.GeminiProperties;
import br.com.fiap.kuravet.ia.llm.GeradorOrientacaoGemini;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Tenta escrever a orientacao com a LLM e, em qualquer falha, usa o modelo fixo.
 * O aviso final e acrescentado aqui, pelo codigo, e nunca pedido a LLM.
 */
@Component
public class GeradorOrientacaoComFallback {

    public static final String AVISO = "Esta orientação não substitui a avaliação do veterinário.";

    private static final Logger log = LoggerFactory.getLogger(GeradorOrientacaoComFallback.class);

    private final GeminiProperties properties;
    private final GeradorOrientacaoGemini geradorGemini;
    private final GeradorOrientacaoModeloFixo geradorModeloFixo;

    public GeradorOrientacaoComFallback(GeminiProperties properties,
                                        GeradorOrientacaoGemini geradorGemini,
                                        GeradorOrientacaoModeloFixo geradorModeloFixo) {
        this.properties = properties;
        this.geradorGemini = geradorGemini;
        this.geradorModeloFixo = geradorModeloFixo;
    }

    public OrientacaoGerada gerar(ContextoOrientacao contexto) {
        if (properties.enabled() && properties.temChave()) {
            try {
                return comAviso(geradorGemini.gerar(contexto), OrigemOrientacao.IA_GENERATIVA);
            } catch (FalhaLlmException e) {
                log.warn("Orientação com LLM falhou, usando modelo fixo: {}", e.getMessage());
            } catch (RuntimeException e) {
                // So o tipo: a mensagem pode carregar trechos do relato ou da resposta.
                log.warn("Orientação com LLM falhou, usando modelo fixo: {}", e.getClass().getSimpleName());
            }
        }
        return comAviso(geradorModeloFixo.gerar(contexto), OrigemOrientacao.MODELO_FIXO);
    }

    private static OrientacaoGerada comAviso(String texto, OrigemOrientacao origem) {
        return new OrientacaoGerada(texto.strip() + " " + AVISO, origem);
    }
}
