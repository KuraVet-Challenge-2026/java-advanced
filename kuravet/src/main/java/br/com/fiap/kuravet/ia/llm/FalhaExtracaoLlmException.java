package br.com.fiap.kuravet.ia.llm;

/**
 * Falha ao extrair sintomas com a LLM. A mensagem descreve apenas o motivo
 * (status HTTP, tipo de erro) e nunca contem o relato, a chave ou o corpo da resposta.
 */
public class FalhaExtracaoLlmException extends RuntimeException {

    public FalhaExtracaoLlmException(String motivo) {
        super(motivo);
    }
}
