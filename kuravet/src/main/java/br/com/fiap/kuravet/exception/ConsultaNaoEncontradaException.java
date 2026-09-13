package br.com.fiap.kuravet.exception;

public class ConsultaNaoEncontradaException extends RuntimeException {

    public ConsultaNaoEncontradaException(Long idConsulta) {
        super("Consulta nao encontrada para o ID: " + idConsulta);
    }
}
