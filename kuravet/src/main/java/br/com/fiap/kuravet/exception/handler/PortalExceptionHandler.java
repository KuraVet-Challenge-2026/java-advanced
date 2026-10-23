package br.com.fiap.kuravet.exception.handler;

import br.com.fiap.kuravet.exception.ConsultaNaoEncontradaException;
import br.com.fiap.kuravet.exception.PetNaoEncontradoException;
import br.com.fiap.kuravet.exception.RegraDeNegocioException;
import br.com.fiap.kuravet.exception.TriagemNaoEncontradaException;
import br.com.fiap.kuravet.exception.TutorNaoEncontradoException;
import br.com.fiap.kuravet.exception.VeterinarioNaoEncontradoException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice(basePackages = "br.com.fiap.kuravet.controller.web")
public class PortalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(PortalExceptionHandler.class);

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({
            ConsultaNaoEncontradaException.class,
            PetNaoEncontradoException.class,
            TriagemNaoEncontradaException.class,
            TutorNaoEncontradoException.class,
            VeterinarioNaoEncontradoException.class
    })
    public String naoEncontrado(RuntimeException ex, Model model) {
        model.addAttribute("titulo", "Registro não encontrado");
        model.addAttribute("mensagem", ex.getMessage());
        return "error/404";
    }

    // ID nao numerico na URL (ex.: /portal/consultas/abc): para o usuario, e um registro inexistente.
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public String enderecoInvalido(Model model) {
        model.addAttribute("titulo", "Registro não encontrado");
        model.addAttribute("mensagem", "O endereço acessado não corresponde a nenhum registro.");
        return "error/404";
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(RegraDeNegocioException.class)
    public String regraDeNegocio(RegraDeNegocioException ex, Model model) {
        model.addAttribute("titulo", "Operação não permitida");
        model.addAttribute("mensagem", ex.getMessage());
        return "erro";
    }

    /**
     * Qualquer erro nao previsto vira uma pagina amigavel com mensagem generica.
     * O detalhe tecnico (stacktrace) vai apenas para o log, nunca para a tela.
     */
    @ExceptionHandler(Exception.class)
    public ModelAndView erroInesperado(Exception ex, HttpServletRequest request) throws Exception {
        // Acesso negado continua com o fluxo do Spring Security (403/login).
        if (ex instanceof AccessDeniedException) {
            throw ex;
        }

        // Excecoes do Spring que ja carregam um status (ex.: 405) mantem o status original.
        HttpStatus status = (ex instanceof ErrorResponse erroSpring)
                ? HttpStatus.valueOf(erroSpring.getStatusCode().value())
                : HttpStatus.INTERNAL_SERVER_ERROR;

        if (status.is5xxServerError()) {
            log.error("Erro inesperado no portal: {} {}", request.getMethod(), request.getRequestURI(), ex);
        } else {
            log.warn("Requisicao invalida no portal: {} {} ({})",
                    request.getMethod(), request.getRequestURI(), ex.getClass().getSimpleName());
        }

        ModelAndView pagina;
        if (status == HttpStatus.NOT_FOUND) {
            pagina = new ModelAndView("error/404");
        } else if (status.is4xxClientError()) {
            pagina = new ModelAndView("erro");
            pagina.addObject("titulo", "Requisição inválida");
            pagina.addObject("mensagem", "Não foi possível processar a requisição. Volte e tente novamente.");
        } else {
            pagina = new ModelAndView("error/500");
        }
        pagina.setStatus(status);
        return pagina;
    }
}
