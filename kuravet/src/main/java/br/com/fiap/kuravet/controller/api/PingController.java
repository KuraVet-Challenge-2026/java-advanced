package br.com.fiap.kuravet.controller.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint de pre-warming: usado pelo app mobile para "acordar" a
 * aplicacao e validar conectividade antes das chamadas reais.
 */
@RestController
public class PingController {

    @GetMapping("/api/ping")
    public String ping() {
        return "pong";
    }
}
