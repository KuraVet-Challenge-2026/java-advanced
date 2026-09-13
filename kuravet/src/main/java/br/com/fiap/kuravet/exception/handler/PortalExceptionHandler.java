package br.com.fiap.kuravet.exception.handler;

import br.com.fiap.kuravet.exception.ConsultaNaoEncontradaException;
import br.com.fiap.kuravet.exception.PetNaoEncontradoException;
import br.com.fiap.kuravet.exception.RegraDeNegocioException;
import br.com.fiap.kuravet.exception.TutorNaoEncontradoException;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice(basePackages = "br.com.fiap.kuravet.controller.web")
public class PortalExceptionHandler {

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({
            ConsultaNaoEncontradaException.class,
            PetNaoEncontradoException.class,
            TutorNaoEncontradoException.class
    })
    public String naoEncontrado(RuntimeException ex, Model model) {
        model.addAttribute("titulo", "Registro nao encontrado");
        model.addAttribute("mensagem", ex.getMessage());
        return "erro";
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(RegraDeNegocioException.class)
    public String regraDeNegocio(RegraDeNegocioException ex, Model model) {
        model.addAttribute("titulo", "Operacao nao permitida");
        model.addAttribute("mensagem", ex.getMessage());
        return "erro";
    }
}