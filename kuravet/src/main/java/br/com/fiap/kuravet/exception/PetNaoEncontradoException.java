package br.com.fiap.kuravet.exception;

/**
 * Lancada quando uma operacao referencia um ID_PET inexistente.
 */
public class PetNaoEncontradoException extends RuntimeException {

    public PetNaoEncontradoException(Long idPet) {
        super("Pet nao encontrado para o ID: " + idPet);
    }
}
