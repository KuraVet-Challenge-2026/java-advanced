package br.com.fiap.kuravet.controller.web;

import br.com.fiap.kuravet.config.SecurityConfig;
import br.com.fiap.kuravet.enums.Perfil;
import br.com.fiap.kuravet.exception.PetNaoEncontradoException;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(TriagemWebController.class)
@Import(SecurityConfig.class)
class TriagemWebControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TriagemService triagemService;

    @MockitoBean
    private PetService petService;

    private UsuarioPrincipal veterinario() {
        return new UsuarioPrincipal(Usuario.builder()
                .idUsuario(99L)
                .username("veterinario")
                .senha("senha")
                .perfil(Perfil.VETERINARIO)
                .build());
    }

    private UsuarioPrincipal tutor() {
        return new UsuarioPrincipal(Usuario.builder()
                .idUsuario(1L)
                .username("tutor")
                .senha("senha")
                .perfil(Perfil.TUTOR)
                .tutor(Tutor.builder().idTutor(1L).nome("Ana Paula").build())
                .build());
    }

    private Pet pet() {
        return Pet.builder()
                .idPet(2L)
                .nome("Luna")
                .especie("Gato")
                .dataNascimento(LocalDate.now().minusYears(3))
                .sexo('F')
                .tutor(Tutor.builder().idTutor(1L).nome("Ana Paula").build())
                .build();
    }

    private Triagem triagem() {
        return Triagem.builder()
                .idTriagem(7L)
                .pet(pet())
                .relato("Minha gata não faz xixi desde ontem")
                .sintomas(EnumSet.of(Sintoma.NAO_URINA, Sintoma.APATIA))
                .urgencia(Urgencia.EMERGENCIA)
                .recomendacao("Procure atendimento presencial imediato.")
                .regrasAplicadas(List.of("R2 - gato que não urina (risco de obstrução urinária)"))
                .metodoExtracao(MetodoExtracao.PALAVRAS_CHAVE)
                .dataTriagem(LocalDateTime.of(2026, 10, 15, 21, 40))
                .build();
    }

    @Test
    void filaDeveListarTriagensComUrgenciaEMetodo() throws Exception {
        when(triagemService.listar(any())).thenReturn(List.of(triagem()));

        mockMvc.perform(get("/portal/triagens").with(user(veterinario())))
                .andExpect(status().isOk())
                .andExpect(view().name("triagens"))
                .andExpect(content().string(containsString("Luna")))
                .andExpect(content().string(containsString("Ana Paula")))
                .andExpect(content().string(containsString("selo-EMERGENCIA")))
                .andExpect(content().string(containsString("Não urina")))
                .andExpect(content().string(containsString("Palavras-chave")))
                .andExpect(content().string(containsString("/portal/triagens/7")));
    }

    @Test
    void filaDeveRotularMetodoLlmComoIaGenerativa() throws Exception {
        Triagem viaLlm = triagem();
        viaLlm.setMetodoExtracao(MetodoExtracao.LLM);
        when(triagemService.listar(any())).thenReturn(List.of(viaLlm));

        mockMvc.perform(get("/portal/triagens").with(user(veterinario())))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("IA generativa")))
                .andExpect(content().string(not(containsString("Palavras-chave</td>"))));
    }

    @Test
    void filaVaziaDeveExibirEstadoVazioAmigavel() throws Exception {
        mockMvc.perform(get("/portal/triagens").with(user(veterinario())))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Nenhuma triagem registrada ainda.")))
                .andExpect(content().string(not(containsString("Nenhuma triagem encontrada com este filtro."))));
    }

    @Test
    void filaFiltradaPorUrgenciaDeveUsarOFiltroDoService() throws Exception {
        mockMvc.perform(get("/portal/triagens").param("urgencia", "ALTA").with(user(veterinario())))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Nenhuma triagem encontrada com este filtro.")))
                .andExpect(content().string(containsString("Ver todas as triagens")));

        verify(triagemService).listarPorUrgencia(any(), eq(Urgencia.ALTA));
    }

    @Test
    void filaDeUrgentesDeveUsarListagemDeAltaEEmergencia() throws Exception {
        when(triagemService.listarUrgentes(any())).thenReturn(List.of(triagem()));

        mockMvc.perform(get("/portal/triagens").param("urgentes", "true").with(user(veterinario())))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("apenas triagens de urgência ALTA e EMERGENCIA")))
                .andExpect(content().string(containsString("Luna")));
    }

    @Test
    void urgenciaInvalidaDeveExibirPagina404() throws Exception {
        mockMvc.perform(get("/portal/triagens").param("urgencia", "XYZ").with(user(veterinario())))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error/404"));
    }

    @Test
    void detalheDeveExplicarAClassificacaoComAsRegrasAplicadas() throws Exception {
        when(triagemService.buscarPorId(any(), eq(7L))).thenReturn(triagem());

        mockMvc.perform(get("/portal/triagens/7").with(user(veterinario())))
                .andExpect(status().isOk())
                .andExpect(view().name("triagem"))
                .andExpect(content().string(containsString("Minha gata não faz xixi desde ontem")))
                .andExpect(content().string(containsString("Por que esta classificação?")))
                .andExpect(content().string(containsString("R2 - gato que não urina")))
                .andExpect(content().string(containsString("não substitui a avaliação do veterinário")))
                .andExpect(content().string(containsString("Histórico do pet")))
                .andExpect(content().string(containsString("/portal/consultas")));
    }

    @Test
    void detalheDeveExibirAOrientacaoEnviadaAoTutorComOSeloDeOrigem() throws Exception {
        Triagem comOrientacao = triagem();
        comOrientacao.setOrientacao("Luna precisa de atendimento agora.");
        comOrientacao.setOrigemOrientacao(OrigemOrientacao.IA_GENERATIVA);
        when(triagemService.buscarPorId(any(), eq(7L))).thenReturn(comOrientacao);

        mockMvc.perform(get("/portal/triagens/7").with(user(veterinario())))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Orientação para o tutor")))
                .andExpect(content().string(containsString("Luna precisa de atendimento agora.")))
                .andExpect(content().string(containsString("Gerada por IA")));
    }

    @Test
    void detalheDeTriagemAntigaNaoDeveExibirOBlocoDeOrientacao() throws Exception {
        when(triagemService.buscarPorId(any(), eq(7L))).thenReturn(triagem());

        mockMvc.perform(get("/portal/triagens/7").with(user(veterinario())))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("Orientação para o tutor"))));
    }

    @Test
    void triagemInexistenteDeveExibirPaginaDeNaoEncontrado() throws Exception {
        when(triagemService.buscarPorId(any(), eq(404L))).thenThrow(new TriagemNaoEncontradaException(404L));

        mockMvc.perform(get("/portal/triagens/404").with(user(veterinario())))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error/404"))
                .andExpect(content().string(containsString("Triagem não encontrada para o ID: 404")));
    }

    @Test
    void historicoDoPetDeveListarTriagensDoPet() throws Exception {
        when(petService.buscarPorId(any(), eq(2L))).thenReturn(pet());
        when(triagemService.listarPorPet(any(), eq(2L))).thenReturn(List.of(triagem()));

        mockMvc.perform(get("/portal/pets/2/triagens").with(user(veterinario())))
                .andExpect(status().isOk())
                .andExpect(view().name("pet-triagens"))
                .andExpect(content().string(containsString("Histórico de triagens de Luna")))
                .andExpect(content().string(containsString("selo-EMERGENCIA")))
                .andExpect(content().string(containsString("15/10/2026 21:40")));
    }

    @Test
    void historicoSemTriagensDeveExibirEstadoVazio() throws Exception {
        when(petService.buscarPorId(any(), eq(2L))).thenReturn(pet());

        mockMvc.perform(get("/portal/pets/2/triagens").with(user(veterinario())))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Este pet ainda não tem triagens.")));
    }

    @Test
    void historicoDePetInexistenteDeveExibirPagina404() throws Exception {
        when(petService.buscarPorId(any(), eq(404L))).thenThrow(new PetNaoEncontradoException(404L));

        mockMvc.perform(get("/portal/pets/404/triagens").with(user(veterinario())))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error/404"));
    }

    @Test
    void tutorNaoDeveAcessarAsTelasDeTriagemDoPortal() throws Exception {
        mockMvc.perform(get("/portal/triagens").with(user(tutor())))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/portal/triagens/7").with(user(tutor())))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/portal/pets/2/triagens").with(user(tutor())))
                .andExpect(status().isForbidden());
    }
}
