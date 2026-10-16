package br.com.fiap.kuravet.exception;

public class VeterinarioNaoEncontradoException extends RuntimeException {

    public VeterinarioNaoEncontradoException(Long idVeterinario) {
        super("Veterinário não encontrado para o ID: " + idVeterinario);
    }
}
