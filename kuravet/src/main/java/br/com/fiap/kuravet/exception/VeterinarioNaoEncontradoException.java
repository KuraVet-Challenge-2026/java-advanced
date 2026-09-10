package br.com.fiap.kuravet.exception;

/**
 * Lancada quando um VETERINARIO e buscado ou referenciado por um
 * ID_VETERINARIO inexistente.
 */
public class VeterinarioNaoEncontradoException extends RuntimeException {

    public VeterinarioNaoEncontradoException(Long idVeterinario) {
        super("Veterinario nao encontrado para o ID: " + idVeterinario);
    }
}
