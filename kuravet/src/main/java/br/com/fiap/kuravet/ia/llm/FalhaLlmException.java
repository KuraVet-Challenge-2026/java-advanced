package br.com.fiap.kuravet.ia.llm;

/**
 * Falha ao chamar a LLM ou ao validar a resposta. A mensagem descreve apenas o motivo
 * (status HTTP, tipo de erro) e nunca contem o relato, a chave ou o corpo da resposta.
 */
public class FalhaLlmException extends RuntimeException {

    public FalhaLlmException(String motivo) {
        super(motivo);
    }
}
