package br.com.fiap.kuravet.config;

import br.com.fiap.kuravet.enums.Perfil;
import br.com.fiap.kuravet.model.Tutor;
import br.com.fiap.kuravet.model.Usuario;
import br.com.fiap.kuravet.security.UsuarioPrincipal;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import static org.assertj.core.api.Assertions.assertThat;

class RedirecionamentoPorPerfilHandlerTest {

    private final RedirecionamentoPorPerfilHandler handler = new RedirecionamentoPorPerfilHandler();

    private String destinoDoLogin(Usuario usuario) throws Exception {
        UsuarioPrincipal principal = new UsuarioPrincipal(usuario);
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.onAuthenticationSuccess(new MockHttpServletRequest(), response,
                UsernamePasswordAuthenticationToken.authenticated(principal, null, principal.getAuthorities()));

        return response.getRedirectedUrl();
    }

    @Test
    void tutorDeveIrParaAsPropriasConsultas() throws Exception {
        Usuario tutor = Usuario.builder().idUsuario(1L).username("tutor").senha("x").perfil(Perfil.TUTOR)
                .tutor(Tutor.builder().idTutor(1L).nome("Tutor 1").build()).build();

        assertThat(destinoDoLogin(tutor)).isEqualTo("/tutor/consultas");
    }

    @Test
    void veterinarioDeveIrParaOPainelDaClinica() throws Exception {
        Usuario vet = Usuario.builder().idUsuario(99L).username("veterinario").senha("x")
                .perfil(Perfil.VETERINARIO).build();

        assertThat(destinoDoLogin(vet)).isEqualTo("/portal/painel");
    }
}
