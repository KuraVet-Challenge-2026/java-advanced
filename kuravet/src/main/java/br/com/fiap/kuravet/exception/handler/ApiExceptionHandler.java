package br.com.fiap.kuravet.exception.handler;

import br.com.fiap.kuravet.exception.ConsultaNaoEncontradaException;
import br.com.fiap.kuravet.exception.PetNaoEncontradoException;
import br.com.fiap.kuravet.exception.RegraDeNegocioException;
import br.com.fiap.kuravet.exception.TutorNaoEncontradoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Tratamento global de excecoes da API REST ({@code /api/**}) consumida pelo
 * app mobile. Garante que erros de negocio e de validacao virem JSON
 * previsivel com o status HTTP correto, em vez do 500 padrao do Spring.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler({
            ConsultaNaoEncontradaException.class,
            PetNaoEncontradoException.class,
            TutorNaoEncontradoException.class
    })
    public ResponseEntity<Map<String, Object>> handleNaoEncontrado(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(corpoErro(HttpStatus.NOT_FOUND, ex.getMessage()));
    }

    @ExceptionHandler(RegraDeNegocioException.class)
    public ResponseEntity<Map<String, Object>> handleRegraDeNegocio(RegraDeNegocioException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(corpoErro(HttpStatus.BAD_REQUEST, ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidacao(MethodArgumentNotValidException ex) {
        Map<String, String> camposInvalidos = new LinkedHashMap<>();
        for (FieldError erro : ex.getBindingResult().getFieldErrors()) {
            camposInvalidos.put(erro.getField(), erro.getDefaultMessage());
        }

        Map<String, Object> corpo = corpoErro(HttpStatus.BAD_REQUEST, "Erro de validacao nos campos informados.");
        corpo.put("campos", camposInvalidos);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(corpo);
    }

    private Map<String, Object> corpoErro(HttpStatus status, String mensagem) {
        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("timestamp", LocalDateTime.now());
        corpo.put("status", status.value());
        corpo.put("erro", status.getReasonPhrase());
        corpo.put("mensagem", mensagem);
        return corpo;
    }
}
