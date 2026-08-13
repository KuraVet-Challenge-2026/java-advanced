package br.com.fiap.kuravet.exception;

/**
 * Lancada quando uma regra de negocio do fluxo funcional e violada
 * (ex.: transicao de status invalida, dado obrigatorio ausente).
 */
public class RegraDeNegocioException extends RuntimeException {

    public RegraDeNegocioException(String mensagem) {
        super(mensagem);
    }
}
