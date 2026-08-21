package br.com.fiap.kuravet.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

/**
 * Endpoints REST consumidos pelo app mobile (React Native), mapeados sob
 * {@code /api/**} e totalmente desprotegidos pelo {@code SecurityConfig}.
 */
@RestController
@CrossOrigin(origins = "*")
public class ApiController {

    /**
     * Endpoint de pre-warming: usado pelo app mobile para "acordar" a
     * aplicacao e validar conectividade antes das chamadas reais.
     */
    @GetMapping("/api/ping")
    public String ping() {
        return "pong";
    }
}
