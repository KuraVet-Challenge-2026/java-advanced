package br.com.fiap.kuravet.exception;

public class TriagemNaoEncontradaException extends RuntimeException {

    public TriagemNaoEncontradaException(Long idTriagem) {
        super("Triagem nao encontrada para o ID: " + idTriagem);
    }
}
