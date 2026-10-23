package br.com.fiap.kuravet.controller.web;

import br.com.fiap.kuravet.config.SecurityConfig;
import br.com.fiap.kuravet.enums.Perfil;
import br.com.fiap.kuravet.enums.StatusConsulta;
import br.com.fiap.kuravet.exception.RegraDeNegocioException;
import br.com.fiap.kuravet.model.Consulta;
import br.com.fiap.kuravet.model.Pet;
import br.com.fiap.kuravet.model.Tutor;
import br.com.fiap.kuravet.model.Usuario;
import br.com.fiap.kuravet.model.Veterinario;
import br.com.fiap.kuravet.security.UsuarioPrincipal;
import br.com.fiap.kuravet.service.ConsultaService;
import br.com.fiap.kuravet.service.TriagemService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest({TutorWebController.class, PortalWebController.class})
@Import(SecurityConfig.class)
class TutorWebControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ConsultaService consultaService;

    // Dependencia do PortalWebController (painel); aqui so precisa existir.
    @MockitoBean
    private TriagemService triagemService;

    private UsuarioPrincipal tutor() {
        return new UsuarioPrincipal(Usuario.builder()
                .idUsuario(1L).username("tutor").senha("senha").perfil(Perfil.TUTOR)
                .tutor(Tutor.builder().idTutor(1L).nome("Ana Paula").build())
                .build());
    }

    private UsuarioPrincipal veterinario() {
        return new UsuarioPrincipal(Usuario.builder()
                .idUsuario(99L).username("veterinario").senha("senha").perfil(Perfil.VETERINARIO)
                .build());
    }

    private Consulta consulta(Long id, StatusConsulta status) {
        return Consulta.builder()
                .idConsulta(id)
                .pet(Pet.builder().idPet(1L).nome("Thor").especie("Cachorro")
                        .tutor(Tutor.builder().idTutor(1L).nome("Ana Paula").build()).build())
                .veterinario(Veterinario.builder().idVeterinario(2L).nome("Dr. Rafael Andrade").build())
                .dataConsulta(LocalDate.now().plusDays(10))
                .tipoConsulta("Teleconsulta - Dermatologia")
                .status(status)
                .build();
    }

    @Test
    void tutorDeveVerSuasConsultasComBotaoCancelarSomenteNasCancelaveis() throws Exception {
        Consulta agendada = consulta(1L, StatusConsulta.AGENDADA);
        Consulta realizada = consulta(2L, StatusConsulta.REALIZADA);
        realizada.setDiagnostico("Dermatite alérgica leve, tratar com pomada.");
        when(consultaService.listar(any())).thenReturn(List.of(agendada, realizada));
        when(consultaService.podeCancelar(any(), eq(agendada))).thenReturn(true);

        mockMvc.perform(get("/tutor/consultas").with(user(tutor())))
                .andExpect(status().isOk())
                .andExpect(view().name("tutor-consultas"))
                .andExpect(model().attributeExists("consultas", "cancelaveis"))
                .andExpect(model().attribute("cancelaveis", Set.of(1L)))
                .andExpect(content().string(containsString("Dermatite alérgica leve")))
                .andExpect(content().string(containsString("/tutor/consultas/1/cancelar")))
                .andExpect(content().string(not(containsString("/tutor/consultas/2/cancelar"))))
                .andExpect(content().string(containsString("Minhas consultas")))
                .andExpect(content().string(not(containsString("/portal/painel"))));
    }

    @Test
    void listaVaziaDeveExibirEstadoVazio() throws Exception {
        mockMvc.perform(get("/tutor/consultas").with(user(tutor())))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Você ainda não tem consultas.")));
    }

    @Test
    void veterinarioEmAreaDoTutorDeveSerEncaminhadoParaPaginaAmigavel() throws Exception {
        mockMvc.perform(get("/tutor/consultas").with(user(veterinario())))
                .andExpect(status().isForbidden())
                .andExpect(forwardedUrl("/acesso-negado"));
    }

    @Test
    void tutorEmAreaDoVeterinarioDeveSerEncaminhadoParaPaginaAmigavel() throws Exception {
        mockMvc.perform(get("/portal/painel").with(user(tutor())))
                .andExpect(status().isForbidden())
                .andExpect(forwardedUrl("/acesso-negado"));
    }

    @Test
    void paginaDeAcessoNegadoDeveOferecerVoltarParaAAreaDoPerfil() throws Exception {
        mockMvc.perform(get("/acesso-negado").with(user(veterinario())))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Acesso não permitido para o seu perfil")))
                .andExpect(content().string(containsString("Voltar ao painel")));

        mockMvc.perform(get("/acesso-negado").with(user(tutor())))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Ir para minhas consultas")));
    }

    @Test
    void cancelarDeveRedirecionarComMensagemDeSucesso() throws Exception {
        when(consultaService.cancelar(any(), eq(1L))).thenReturn(consulta(1L, StatusConsulta.CANCELADA));

        mockMvc.perform(post("/tutor/consultas/1/cancelar").with(user(tutor())).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tutor/consultas"))
                .andExpect(flash().attribute("sucesso", "Consulta de Thor cancelada."));

        verify(consultaService).cancelar(any(), eq(1L));
    }

    @Test
    void cancelarSemCsrfDeveSerNegado() throws Exception {
        mockMvc.perform(post("/tutor/consultas/1/cancelar").with(user(tutor())))
                .andExpect(status().isForbidden());

        verify(consultaService, never()).cancelar(any(), any());
    }

    @Test
    void cancelamentoNaoPermitidoDeveExibirMensagemNaPaginaDeErro() throws Exception {
        when(consultaService.cancelar(any(), eq(5L)))
                .thenThrow(new RegraDeNegocioException("Transição de status inválida: REALIZADA -> CANCELADA."));

        mockMvc.perform(post("/tutor/consultas/5/cancelar").with(user(tutor())).with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(view().name("erro"))
                .andExpect(content().string(containsString("Transição de status inválida")));
    }

    @Test
    void raizDeveRedirecionarCadaPerfilParaASuaArea() throws Exception {
        mockMvc.perform(get("/").with(user(tutor())))
                .andExpect(redirectedUrl("/tutor/consultas"));
        mockMvc.perform(get("/").with(user(veterinario())))
                .andExpect(redirectedUrl("/portal/painel"));
    }
}
