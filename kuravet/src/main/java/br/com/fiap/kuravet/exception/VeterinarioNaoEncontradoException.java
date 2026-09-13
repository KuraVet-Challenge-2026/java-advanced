package br.com.fiap.kuravet.exception;

public class VeterinarioNaoEncontradoException extends RuntimeException {

    public VeterinarioNaoEncontradoException(Long idVeterinario) {
        super("Veterinario nao encontrado para o ID: " + idVeterinario);
    }
}
