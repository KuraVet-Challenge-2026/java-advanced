package br.com.fiap.kuravet.controller.web;

import br.com.fiap.kuravet.config.SecurityConfig;
import br.com.fiap.kuravet.enums.Perfil;
import br.com.fiap.kuravet.enums.StatusConsulta;
import br.com.fiap.kuravet.exception.ConsultaNaoEncontradaException;
import br.com.fiap.kuravet.exception.RegraDeNegocioException;
import br.com.fiap.kuravet.model.Usuario;
import br.com.fiap.kuravet.security.UsuarioPrincipal;
import br.com.fiap.kuravet.service.ConsultaService;
import br.com.fiap.kuravet.service.PetService;
import br.com.fiap.kuravet.service.TriagemService;
import br.com.fiap.kuravet.service.TutorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest({PortalWebController.class, PetWebController.class})
@Import(SecurityConfig.class)
class PortalWebControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ConsultaService consultaService;

    @MockitoBean
    private PetService petService;

    @MockitoBean
    private TutorService tutorService;

    @MockitoBean
    private TriagemService triagemService;

    private UsuarioPrincipal veterinario() {
        return new UsuarioPrincipal(Usuario.builder()
                .idUsuario(99L)
                .username("veterinario")
                .senha("senha")
                .perfil(Perfil.VETERINARIO)
                .build());
    }

    @Test
    void painelDeveExibirTituloCentralizado() throws Exception {
        mockMvc.perform(get("/portal/painel").with(user(veterinario())))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Painel da Clínica KuraVet")))
                .andExpect(content().string(containsString("Aguardando aprovação")));
    }

    @Test
    void painelDeveExibirCardDeTriagensUrgentesComLinkParaAFilaFiltrada() throws Exception {
        when(triagemService.contarUrgentesDesde(any())).thenReturn(3L);

        mockMvc.perform(get("/portal/painel").with(user(veterinario())))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Triagens urgentes (7 dias)")))
                .andExpect(content().string(containsString("/portal/triagens?urgentes=true")));
    }

    @Test
    void solicitacoesVaziasDevemOrientarProximoPasso() throws Exception {
        mockMvc.perform(get("/portal/solicitacoes").with(user(veterinario())))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Nenhuma solicitação aguardando aprovação.")))
                .andExpect(content().string(containsString("Ver consultas agendadas")));
    }

    @Test
    void consultasVaziasSemFiltroDevemApontarParaSolicitacoes() throws Exception {
        mockMvc.perform(get("/portal/consultas").with(user(veterinario())))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Nenhuma consulta registrada ainda.")))
                .andExpect(content().string(not(containsString("Nenhuma consulta com status"))));
    }

    @Test
    void consultasVaziasComFiltroDevemOferecerLimparFiltro() throws Exception {
        mockMvc.perform(get("/portal/consultas").param("status", "RECUSADA").with(user(veterinario())))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Nenhuma consulta com status")))
                .andExpect(content().string(containsString("Ver todas as consultas")))
                .andExpect(content().string(not(containsString("Nenhuma consulta registrada ainda."))));
    }

    @Test
    void petsVaziosDevemOferecerCadastro() throws Exception {
        mockMvc.perform(get("/portal/pets").with(user(veterinario())))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Nenhum pet cadastrado ainda.")))
                .andExpect(content().string(containsString("Cadastrar o primeiro pet")));
    }

    @Test
    void formularioDePetDeveRenderizarComAcentos() throws Exception {
        mockMvc.perform(get("/portal/pets/novo").with(user(veterinario())))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Tutor responsável *")))
                .andExpect(content().string(containsString("Fêmea")));
    }

    @Test
    void consultaInexistenteDeveExibirPagina404Amigavel() throws Exception {
        when(consultaService.buscarPorId(any(), eq(404L))).thenThrow(new ConsultaNaoEncontradaException(404L));

        mockMvc.perform(get("/portal/consultas/404").with(user(veterinario())))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error/404"))
                .andExpect(content().string(containsString("Consulta não encontrada para o ID: 404")))
                .andExpect(content().string(containsString("Voltar ao painel")));
    }

    @Test
    void idNaoNumericoDeveExibirPagina404() throws Exception {
        mockMvc.perform(get("/portal/consultas/abc").with(user(veterinario())))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error/404"));
    }

    @Test
    void regraDeNegocioDeveExibirMensagemNaPaginaDeErro() throws Exception {
        when(consultaService.aprovarSolicitacao(1L))
                .thenThrow(new RegraDeNegocioException("Transição de status inválida: REALIZADA -> AGENDADA."));

        mockMvc.perform(post("/portal/solicitacoes/1/aprovar").with(user(veterinario())).with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(view().name("erro"))
                .andExpect(content().string(containsString("Operação não permitida")))
                .andExpect(content().string(containsString("Transição de status inválida")));
    }

    @Test
    void erroInesperadoDeveExibirPagina500SemDetalhesTecnicos() throws Exception {
        when(consultaService.contarPorStatus(StatusConsulta.SOLICITADA))
                .thenThrow(new IllegalStateException("detalhe interno: conexao recusada em 10.0.0.1"));

        mockMvc.perform(get("/portal/painel").with(user(veterinario())))
                .andExpect(status().isInternalServerError())
                .andExpect(view().name("error/500"))
                .andExpect(content().string(containsString("Algo deu errado")))
                .andExpect(content().string(not(containsString("detalhe interno"))))
                .andExpect(content().string(not(containsString("IllegalStateException"))));
    }
}
