package br.com.fiap.kuravet.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controlador do portal web (Thymeleaf). {@code /login} e publico;
 * {@code /portal/**} exige perfil VETERINARIO (ver {@code SecurityConfig}).
 */
@Controller
public class PortalWebController {

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/portal/painel")
    public String painel(Model model) {
        model.addAttribute("mensagem", "Bem-vindo(a) ao painel do KuraVet!");
        return "painel";
    }
}