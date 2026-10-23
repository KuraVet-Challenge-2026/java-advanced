package br.com.fiap.kuravet.config;

import br.com.fiap.kuravet.enums.Perfil;
import br.com.fiap.kuravet.security.UsuarioPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;

/**
 * Depois do login, cada perfil cai na sua propria area do portal:
 * VETERINARIO no painel da clinica e TUTOR na lista das proprias consultas.
 */
public class RedirecionamentoPorPerfilHandler implements AuthenticationSuccessHandler {

    private final RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    public static String destino(Perfil perfil) {
        return perfil == Perfil.TUTOR ? "/tutor/consultas" : "/portal/painel";
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        UsuarioPrincipal principal = (UsuarioPrincipal) authentication.getPrincipal();
        redirectStrategy.sendRedirect(request, response, destino(principal.getPerfil()));
    }
}
