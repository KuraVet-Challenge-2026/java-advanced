package br.com.fiap.kuravet.controller.api;

import br.com.fiap.kuravet.config.SecurityConfig;
import br.com.fiap.kuravet.enums.Perfil;
import br.com.fiap.kuravet.ia.MetodoExtracao;
import br.com.fiap.kuravet.ia.Sintoma;
import br.com.fiap.kuravet.ia.Urgencia;
import br.com.fiap.kuravet.model.Pet;
import br.com.fiap.kuravet.model.Triagem;
import br.com.fiap.kuravet.model.Tutor;
import br.com.fiap.kuravet.model.Usuario;
import br.com.fiap.kuravet.security.UsuarioPrincipal;
import br.com.fiap.kuravet.service.TriagemService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TriagemController.class)
@Import(SecurityConfig.class)
class TriagemControllerSecurityTest {

    private static final String CORPO_VALIDO = """
            { "idPet": 2, "relato": "Minha gata não faz xixi desde ontem" }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TriagemService triagemService;

    private UsuarioPrincipal principalTutor() {
        Usuario usuario = Usuario.builder()
                .idUsuario(1L)
                .username("tutor")
                .senha("senha")
                .perfil(Perfil.TUTOR)
                .tutor(Tutor.builder().idTutor(1L).nome("Tutor 1").cpf("111.111.111-11").build())
                .build();
        return new UsuarioPrincipal(usuario);
    }

    private UsuarioPrincipal principalVeterinario() {
        Usuario usuario = Usuario.builder()
                .idUsuario(99L)
                .username("veterinario")
                .senha("senha")
                .perfil(Perfil.VETERINARIO)
                .build();
        return new UsuarioPrincipal(usuario);
    }

    private Triagem triagemEmergencia() {
        Pet pet = Pet.builder()
                .idPet(2L)
                .nome("Luna")
                .especie("Gato")
                .dataNascimento(LocalDate.of(2021, 7, 2))
                .sexo('F')
                .tutor(Tutor.builder().idTutor(1L).nome("Tutor 1").build())
                .build();
        return Triagem.builder()
                .idTriagem(7L)
                .pet(pet)
                .relato("Minha gata não faz xixi desde ontem")
                .sintomas(EnumSet.of(Sintoma.NAO_URINA))
                .urgencia(Urgencia.EMERGENCIA)
                .recomendacao("Procure atendimento presencial imediato em um pronto-socorro veterinário.")
                .regrasAplicadas(List.of("R2 - gato que não urina (risco de obstrução urinária)"))
                .metodoExtracao(MetodoExtracao.PALAVRAS_CHAVE)
                .dataTriagem(LocalDateTime.now())
                .build();
    }

    @Test
    void deveRetornar201ComLocationAoTutorRealizarTriagem() throws Exception {
        when(triagemService.realizar(any(UsuarioPrincipal.class), any())).thenReturn(triagemEmergencia());

        mockMvc.perform(post("/api/triagens")
                        .with(user(principalTutor()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CORPO_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/triagens/7"))
                .andExpect(jsonPath("$.urgencia").value("EMERGENCIA"))
                .andExpect(jsonPath("$.nomePet").value("Luna"))
                .andExpect(jsonPath("$.sintomas[0]").value("Não urina"))
                .andExpect(jsonPath("$.sugereTeleconsultaPrioritaria").value(true));
    }

    @Test
    void deveRetornar403AoVeterinarioTentarRealizarTriagem() throws Exception {
        mockMvc.perform(post("/api/triagens")
                        .with(user(principalVeterinario()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CORPO_VALIDO))
                .andExpect(status().isForbidden());

        verify(triagemService, never()).realizar(any(), any());
    }

    @Test
    void deveRetornar400QuandoRelatoForCurto() throws Exception {
        mockMvc.perform(post("/api/triagens")
                        .with(user(principalTutor()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "idPet": 2, "relato": "tosse" }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.relato").exists());

        verify(triagemService, never()).realizar(any(), any());
    }

    @Test
    void deveRetornar401SemAutenticacao() throws Exception {
        mockMvc.perform(post("/api/triagens")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CORPO_VALIDO))
                .andExpect(status().isUnauthorized());
    }
}
