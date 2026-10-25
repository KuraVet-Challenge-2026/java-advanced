package br.com.fiap.kuravet.controller.web;

import br.com.fiap.kuravet.config.SecurityConfig;
import br.com.fiap.kuravet.dto.triagem.TriagemRequestDTO;
import br.com.fiap.kuravet.enums.Perfil;
import br.com.fiap.kuravet.exception.TriagemNaoEncontradaException;
import br.com.fiap.kuravet.ia.MetodoExtracao;
import br.com.fiap.kuravet.ia.OrigemOrientacao;
import br.com.fiap.kuravet.ia.Sintoma;
import br.com.fiap.kuravet.ia.Urgencia;
import br.com.fiap.kuravet.model.Pet;
import br.com.fiap.kuravet.model.Triagem;
import br.com.fiap.kuravet.model.Tutor;
import br.com.fiap.kuravet.model.Usuario;
import br.com.fiap.kuravet.security.UsuarioPrincipal;
import br.com.fiap.kuravet.service.PetService;
import br.com.fiap.kuravet.service.TriagemService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;

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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(TutorTriagemWebController.class)
@Import(SecurityConfig.class)
class TutorTriagemWebControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TriagemService triagemService;

    @MockitoBean
    private PetService petService;

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

    private Pet pet() {
        return Pet.builder().idPet(2L).nome("Luna").especie("Gato")
                .tutor(Tutor.builder().idTutor(1L).nome("Ana Paula").build()).build();
    }

    private Triagem triagem(Urgencia urgencia, MetodoExtracao metodo) {
        return Triagem.builder()
                .idTriagem(7L)
                .pet(pet())
                .relato("Minha gata não faz xixi desde ontem")
                .sintomas(EnumSet.of(Sintoma.NAO_URINA))
                .urgencia(urgencia)
                .recomendacao("Procure atendimento presencial imediato.")
                .regrasAplicadas(List.of("R2 - gato que não urina (risco de obstrução urinária)"))
                .metodoExtracao(metodo)
                .dataTriagem(LocalDateTime.of(2026, 10, 15, 21, 40))
                .build();
    }

    @Test
    void formularioDeTriagemDeveListarOsPetsDoTutor() throws Exception {
        when(petService.listar(any())).thenReturn(List.of(pet()));

        mockMvc.perform(get("/tutor/triagens/nova").with(user(tutor())))
                .andExpect(status().isOk())
                .andExpect(view().name("tutor-triagem-nova"))
                .andExpect(model().attributeExists("pets", "triagemForm"))
                .andExpect(content().string(containsString("Luna (Gato)")))
                .andExpect(content().string(containsString("Descreva o que o seu pet está sentindo")));
    }

    @Test
    void veterinarioNaoDeveAcessarAsTelasDeTriagemDoTutor() throws Exception {
        mockMvc.perform(get("/tutor/triagens/nova").with(user(veterinario())))
                .andExpect(status().isForbidden())
                .andExpect(forwardedUrl("/acesso-negado"));
        mockMvc.perform(get("/tutor/triagens").with(user(veterinario())))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/tutor/triagens/7").with(user(veterinario())))
                .andExpect(status().isForbidden());
    }

    @Test
    void triagemValidaDeveRealizarEIrParaOResultado() throws Exception {
        when(triagemService.realizar(any(), any())).thenReturn(triagem(Urgencia.EMERGENCIA, MetodoExtracao.LLM));

        mockMvc.perform(post("/tutor/triagens").with(user(tutor())).with(csrf())
                        .param("idPet", "2")
                        .param("relato", "Minha gata não faz xixi desde ontem"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tutor/triagens/7"));

        verify(triagemService).realizar(any(), eq(new TriagemRequestDTO(2L, "Minha gata não faz xixi desde ontem")));
    }

    @Test
    void relatoCurtoDeveVoltarAoFormularioComErro() throws Exception {
        when(petService.listar(any())).thenReturn(List.of(pet()));

        mockMvc.perform(post("/tutor/triagens").with(user(tutor())).with(csrf())
                        .param("idPet", "2")
                        .param("relato", "dói"))
                .andExpect(status().isOk())
                .andExpect(view().name("tutor-triagem-nova"))
                .andExpect(model().attributeHasFieldErrors("triagemForm", "relato"))
                .andExpect(content().string(containsString("O relato deve ter entre 10 e 1000 caracteres.")));

        verify(triagemService, never()).realizar(any(), any());
    }

    @Test
    void triagemSemCsrfDeveSerNegada() throws Exception {
        mockMvc.perform(post("/tutor/triagens").with(user(tutor()))
                        .param("idPet", "2")
                        .param("relato", "Minha gata não faz xixi desde ontem"))
                .andExpect(status().isForbidden());

        verify(triagemService, never()).realizar(any(), any());
    }

    @Test
    void resultadoUrgenteDeveOferecerSolicitarTeleconsultaComOPetSelecionado() throws Exception {
        when(triagemService.buscarPorId(any(), eq(7L))).thenReturn(triagem(Urgencia.EMERGENCIA, MetodoExtracao.LLM));

        mockMvc.perform(get("/tutor/triagens/7").with(user(tutor())))
                .andExpect(status().isOk())
                .andExpect(view().name("tutor-triagem-resultado"))
                .andExpect(model().attributeExists("triagem"))
                .andExpect(content().string(containsString("selo-EMERGENCIA")))
                .andExpect(content().string(containsString("Por que esta classificação?")))
                .andExpect(content().string(containsString("R2 - gato que não urina")))
                .andExpect(content().string(containsString("Interpretado por IA generativa")))
                .andExpect(content().string(containsString("não substitui a avaliação do veterinário")))
                .andExpect(content().string(containsString("Solicitar teleconsulta agora")))
                .andExpect(content().string(containsString("/tutor/consultas/nova?idPet=2")));
    }

    @Test
    void resultadoNaoUrgenteNaoDeveOferecerTeleconsultaAgora() throws Exception {
        when(triagemService.buscarPorId(any(), eq(7L))).thenReturn(triagem(Urgencia.BAIXA, MetodoExtracao.PALAVRAS_CHAVE));

        mockMvc.perform(get("/tutor/triagens/7").with(user(tutor())))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Interpretado por palavras-chave")))
                .andExpect(content().string(not(containsString("Solicitar teleconsulta agora"))));
    }

    @Test
    void resultadoDeveDestacarAOrientacaoGeradaPorIa() throws Exception {
        Triagem comOrientacao = triagem(Urgencia.EMERGENCIA, MetodoExtracao.LLM);
        comOrientacao.setOrientacao("Luna precisa de atendimento agora. "
                + "Esta orientação não substitui a avaliação do veterinário.");
        comOrientacao.setOrigemOrientacao(OrigemOrientacao.IA_GENERATIVA);
        when(triagemService.buscarPorId(any(), eq(7L))).thenReturn(comOrientacao);

        mockMvc.perform(get("/tutor/triagens/7").with(user(tutor())))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Orientação para você")))
                .andExpect(content().string(containsString("Luna precisa de atendimento agora.")))
                .andExpect(content().string(containsString("Gerada por IA")));
    }

    @Test
    void resultadoComOrientacaoPadraoDeveExibirOSeloDoModeloFixo() throws Exception {
        Triagem comOrientacao = triagem(Urgencia.BAIXA, MetodoExtracao.PALAVRAS_CHAVE);
        comOrientacao.setOrientacao("Sintomas identificados em Luna: coceira.");
        comOrientacao.setOrigemOrientacao(OrigemOrientacao.MODELO_FIXO);
        when(triagemService.buscarPorId(any(), eq(7L))).thenReturn(comOrientacao);

        mockMvc.perform(get("/tutor/triagens/7").with(user(tutor())))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Orientação padrão")))
                .andExpect(content().string(not(containsString("Gerada por IA"))));
    }

    @Test
    void triagemAntigaSemOrientacaoNaoDeveExibirOBloco() throws Exception {
        when(triagemService.buscarPorId(any(), eq(7L))).thenReturn(triagem(Urgencia.ALTA, MetodoExtracao.LLM));

        mockMvc.perform(get("/tutor/triagens/7").with(user(tutor())))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("Orientação para você"))));
    }

    @Test
    void triagemDeOutroTutorOuInexistenteDeveExibirPaginaDeNaoEncontrado() throws Exception {
        when(triagemService.buscarPorId(any(), eq(404L))).thenThrow(new TriagemNaoEncontradaException(404L));

        mockMvc.perform(get("/tutor/triagens/404").with(user(tutor())))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error/404"));
    }

    @Test
    void historicoDeveListarTriagensComLinkParaOResultado() throws Exception {
        when(triagemService.listar(any())).thenReturn(List.of(triagem(Urgencia.ALTA, MetodoExtracao.LLM)));

        mockMvc.perform(get("/tutor/triagens").with(user(tutor())))
                .andExpect(status().isOk())
                .andExpect(view().name("tutor-triagens"))
                .andExpect(content().string(containsString("Luna")))
                .andExpect(content().string(containsString("/tutor/triagens/7")));
    }

    @Test
    void historicoVazioDeveExibirEstadoVazio() throws Exception {
        mockMvc.perform(get("/tutor/triagens").with(user(tutor())))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Você ainda não fez nenhuma triagem.")));
    }
}
