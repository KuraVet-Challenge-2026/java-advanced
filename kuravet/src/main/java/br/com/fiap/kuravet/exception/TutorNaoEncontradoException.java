package br.com.fiap.kuravet.exception;

/**
 * Lancada quando um PET e cadastrado/atualizado referenciando um
 * ID_TUTOR inexistente.
 */
public class TutorNaoEncontradoException extends RuntimeException {

    public TutorNaoEncontradoException(Long idTutor) {
        super("Tutor nao encontrado para o ID: " + idTutor);
    }
}
