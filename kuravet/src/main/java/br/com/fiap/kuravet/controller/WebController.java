package br.com.fiap.kuravet.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.CrossOrigin;

/**
 * Controlador do portal web (Thymeleaf), protegido por autenticacao via
 * formLogin() conforme definido em {@code SecurityConfig}.
 */
@Controller
@CrossOrigin(origins = "*")
public class WebController {

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/painel")
    public String painel(Model model) {
        model.addAttribute("mensagem", "Bem-vindo(a) ao painel do KuraVet!");
        return "painel";
    }
}
