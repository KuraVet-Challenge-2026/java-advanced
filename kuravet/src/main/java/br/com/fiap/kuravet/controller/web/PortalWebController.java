package br.com.fiap.kuravet.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.CrossOrigin;

/**
 * Controlador do portal web (Thymeleaf). {@code /login} e publico;
 * {@code /portal/**} exige perfil VETERINARIO (ver {@code SecurityConfig}).
 */
@Controller
<<<<<<< HEAD:kuravet/src/main/java/br/com/fiap/kuravet/controller/web/PortalWebController.java
public class PortalWebController {
=======
@CrossOrigin(origins = "*")
public class WebController {
>>>>>>> 6ffe238c465b5c471a6c37c73c1b2eba6b945448:kuravet/src/main/java/br/com/fiap/kuravet/controller/WebController.java

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
