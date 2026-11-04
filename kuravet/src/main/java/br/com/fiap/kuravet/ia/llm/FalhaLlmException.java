package br.com.fiap.kuravet.ia.llm;

public class FalhaLlmException extends RuntimeException {

    public FalhaLlmException(String motivo) {
        super(motivo);
    }
}
