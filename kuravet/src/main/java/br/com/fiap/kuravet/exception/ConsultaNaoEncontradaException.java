package br.com.fiap.kuravet.exception;

/**
 * Lancada quando uma operacao referencia um ID_CONSULTA inexistente.
 */
public class ConsultaNaoEncontradaException extends RuntimeException {

    public ConsultaNaoEncontradaException(Long idConsulta) {
        super("Consulta nao encontrada para o ID: " + idConsulta);
    }
}
