package br.com.fiap.kuravet.exception;

public class ConsultaNaoEncontradaException extends RuntimeException {

    public ConsultaNaoEncontradaException(Long idConsulta) {
        super("Consulta não encontrada para o ID: " + idConsulta);
    }
}
