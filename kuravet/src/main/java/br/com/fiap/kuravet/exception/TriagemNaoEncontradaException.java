package br.com.fiap.kuravet.exception;

public class TriagemNaoEncontradaException extends RuntimeException {

    public TriagemNaoEncontradaException(Long idTriagem) {
        super("Triagem não encontrada para o ID: " + idTriagem);
    }
}
