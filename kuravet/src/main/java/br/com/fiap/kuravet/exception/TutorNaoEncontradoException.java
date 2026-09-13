package br.com.fiap.kuravet.exception;

public class TutorNaoEncontradoException extends RuntimeException {

    public TutorNaoEncontradoException(Long idTutor) {
        super("Tutor nao encontrado para o ID: " + idTutor);
    }
}
