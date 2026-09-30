package br.com.fiap.kuravet.controller.api;

import br.com.fiap.kuravet.config.SecurityConfig;
import br.com.fiap.kuravet.enums.Perfil;
import br.com.fiap.kuravet.enums.StatusConsulta;
import br.com.fiap.kuravet.model.Consulta;
import br.com.fiap.kuravet.model.Pet;
import br.com.fiap.kuravet.model.Tutor;
import br.com.fiap.kuravet.model.Usuario;
import br.com.fiap.kuravet.model.Veterinario;
import br.com.fiap.kuravet.security.UsuarioPrincipal;
import br.com.fiap.kuravet.service.ConsultaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ConsultaController.class)
@Import(SecurityConfig.class)
class ConsultaControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ConsultaService consultaService;

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

    private Consulta consultaCancelada() {
        Pet pet = Pet.builder()
                .idPet(10L)
                .nome("Rex")
                .especie("Cachorro")
                .dataNascimento(LocalDate.of(2020, 1, 1))
                .sexo('M')
                .tutor(Tutor.builder().idTutor(1L).nome("Tutor 1").build())
                .build();
        Veterinario veterinario = Veterinario.builder()
                .idVeterinario(20L)
                .nome("Dra. Ana")
                .crmv("CRMV-20")
                .build();
        return Consulta.builder()
                .idConsulta(1L)
                .pet(pet)
                .veterinario(veterinario)
                .dataConsulta(LocalDate.now().plusDays(5))
                .tipoConsulta("Rotina")
                .dataSolicitacao(LocalDate.now())
                .status(StatusConsulta.CANCELADA)
                .build();
    }

    @Test
    void deveRetornar403AoTutorTentarExcluirConsulta() throws Exception {
        mockMvc.perform(delete("/api/consultas/1").with(user(principalTutor())))
                .andExpect(status().isForbidden());
    }

    @Test
    void deveRetornar204AoVeterinarioExcluirConsulta() throws Exception {
        mockMvc.perform(delete("/api/consultas/1").with(user(principalVeterinario())))
                .andExpect(status().isNoContent());
    }

    @Test
    void deveRetornar200AoTutorCancelarConsulta() throws Exception {
        when(consultaService.cancelar(any(UsuarioPrincipal.class), eq(1L))).thenReturn(consultaCancelada());

        mockMvc.perform(patch("/api/consultas/1/cancelamento").with(user(principalTutor())))
                .andExpect(status().isOk());

        verify(consultaService).cancelar(any(UsuarioPrincipal.class), eq(1L));
    }
}
