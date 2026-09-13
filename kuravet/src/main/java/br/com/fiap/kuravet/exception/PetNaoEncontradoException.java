package br.com.fiap.kuravet.exception;

public class PetNaoEncontradoException extends RuntimeException {

    public PetNaoEncontradoException(Long idPet) {
        super("Pet nao encontrado para o ID: " + idPet);
    }
}
