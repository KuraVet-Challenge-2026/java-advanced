package br.com.fiap.kuravet.exception;

public class PetNaoEncontradoException extends RuntimeException {

    public PetNaoEncontradoException(Long idPet) {
        super("Pet não encontrado para o ID: " + idPet);
    }
}
