package br.com.fiap.kuravet.controller.web;

import br.com.fiap.kuravet.config.SecurityConfig;
import br.com.fiap.kuravet.dto.financeiro.LinhaFaturamentoDTO;
import br.com.fiap.kuravet.dto.financeiro.PagamentoForm;
import br.com.fiap.kuravet.enums.Perfil;
import br.com.fiap.kuravet.enums.StatusConsulta;
import br.com.fiap.kuravet.exception.RegraDeNegocioException;
import br.com.fiap.kuravet.model.Consulta;
import br.com.fiap.kuravet.model.Pet;
import br.com.fiap.kuravet.model.Tutor;
import br.com.fiap.kuravet.model.Usuario;
import br.com.fiap.kuravet.security.UsuarioPrincipal;
import br.com.fiap.kuravet.service.ConsultaService;
import br.com.fiap.kuravet.service.FinanceiroService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
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

@WebMvcTest(FinanceiroWebController.class)
@Import(SecurityConfig.class)
class FinanceiroWebControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FinanceiroService financeiroService;

    @MockitoBean
    private ConsultaService consultaService;

    private UsuarioPrincipal veterinario() {
        return new UsuarioPrincipal(Usuario.builder()
                .idUsuario(99L).username("veterinario").senha("senha").perfil(Perfil.VETERINARIO)
                .build());
    }

    private UsuarioPrincipal tutor() {
        return new UsuarioPrincipal(Usuario.builder()
                .idUsuario(1L).username("tutor").senha("senha").perfil(Perfil.TUTOR)
                .tutor(Tutor.builder().idTutor(1L).nome("Ana Paula").build())
                .build());
    }

    private Consulta consultaRealizada() {
        return Consulta.builder()
                .idConsulta(50L)
                .pet(Pet.builder().idPet(2L).nome("Luna").especie("Gato").build())
                .dataConsulta(LocalDate.of(2026, 1, 10))
                .tipoConsulta("Teleconsulta")
                .status(StatusConsulta.REALIZADA)
                .build();
    }

    private static List<LinhaFaturamentoDTO> faturamento() {
        return List.of(
                new LinhaFaturamentoDTO("KuraVet Moema", "Cartao de Credito", 1, new BigDecimal("220.00"), 0),
                new LinhaFaturamentoDTO("KuraVet Moema", "Pix", 1, new BigDecimal("90.00"), 0),
                new LinhaFaturamentoDTO("KuraVet Moema", "SUBTOTAL", 2, new BigDecimal("310.00"), 1),
                new LinhaFaturamentoDTO("TOTAL GERAL", null, 2, new BigDecimal("1310.00"), 3));
    }

    @Test
    void veterinarioDeveVerFormularioEFaturamentoDoAnoCorrente() throws Exception {
        when(consultaService.listarPorStatus(any(), eq(StatusConsulta.REALIZADA))).thenReturn(List.of(consultaRealizada()));
        int ano = LocalDate.now().getYear();
        when(financeiroService.faturamento(LocalDate.of(ano, 1, 1), LocalDate.of(ano, 12, 31))).thenReturn(faturamento());

        mockMvc.perform(get("/portal/financeiro").with(user(veterinario())))
                .andExpect(status().isOk())
                .andExpect(view().name("financeiro"))
                .andExpect(model().attributeExists("pagamentoForm", "consultasRealizadas", "linhas"))
                .andExpect(content().string(containsString("#50 - Luna (10/01/2026)")))
                .andExpect(content().string(containsString("Cartão de Crédito")))
                .andExpect(content().string(containsString("value=\"Cartao de Credito\"")))
                .andExpect(content().string(containsString("linha-subtotal")))
                .andExpect(content().string(containsString("Subtotal da clínica")))
                .andExpect(content().string(containsString("linha-total")))
                .andExpect(content().string(containsString("Total geral")))
                .andExpect(content().string(containsString("R$ 1.310,00")))
                .andExpect(content().string(not(containsString("Nenhum pagamento no período."))));
    }

    @Test
    void periodoSemPagamentosDeveExibirEstadoVazio() throws Exception {
        when(financeiroService.faturamento(any(), any())).thenReturn(List.of());

        mockMvc.perform(get("/portal/financeiro").with(user(veterinario()))
                        .param("inicio", "2020-01-01").param("fim", "2020-12-31"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Nenhum pagamento no período.")));

        verify(financeiroService).faturamento(LocalDate.of(2020, 1, 1), LocalDate.of(2020, 12, 31));
    }

    @Test
    void periodoInvalidoDeveExibirMensagemSemPaginaDeErro() throws Exception {
        when(financeiroService.faturamento(any(), any()))
                .thenThrow(new RegraDeNegocioException("A data inicial não pode ser posterior à data final."));

        mockMvc.perform(get("/portal/financeiro").with(user(veterinario()))
                        .param("inicio", "2026-12-31").param("fim", "2026-01-01"))
                .andExpect(status().isOk())
                .andExpect(view().name("financeiro"))
                .andExpect(content().string(containsString("A data inicial não pode ser posterior à data final.")));
    }

    @Test
    void tutorNaoDeveAcessarOFinanceiro() throws Exception {
        mockMvc.perform(get("/portal/financeiro").with(user(tutor())))
                .andExpect(status().isForbidden())
                .andExpect(forwardedUrl("/acesso-negado"));
        mockMvc.perform(post("/portal/financeiro/pagamentos").with(user(tutor())).with(csrf())
                        .param("idConsulta", "50"))
                .andExpect(status().isForbidden());

        verify(financeiroService, never()).registrarPagamento(any());
    }

    @Test
    void pagamentoValidoDeveRedirecionarComMensagemDeSucesso() throws Exception {
        when(financeiroService.registrarPagamento(any())).thenReturn(101L);

        mockMvc.perform(post("/portal/financeiro/pagamentos").with(user(veterinario())).with(csrf())
                        .param("idConsulta", "50")
                        .param("clinica", "KuraVet Pinheiros")
                        .param("tipoPagamento", "Pix")
                        .param("valor", "150.00")
                        .param("dataPagamento", "2026-10-20"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/portal/financeiro"))
                .andExpect(flash().attribute("sucesso", "Pagamento nº 101 registrado."));

        ArgumentCaptor<PagamentoForm> captor = ArgumentCaptor.forClass(PagamentoForm.class);
        verify(financeiroService).registrarPagamento(captor.capture());
        assertThat(captor.getValue().getValor()).isEqualByComparingTo("150.00");
        assertThat(captor.getValue().getDataPagamento()).isEqualTo(LocalDate.of(2026, 10, 20));
    }

    @Test
    void regraDeNegocioDoBancoDeveVoltarAoFormularioComAMensagem() throws Exception {
        when(financeiroService.registrarPagamento(any())).thenThrow(new RegraDeNegocioException(
                "Pagamento so e aceito para consulta REALIZADA (consulta 3 esta AGENDADA)"));

        mockMvc.perform(post("/portal/financeiro/pagamentos").with(user(veterinario())).with(csrf())
                        .param("idConsulta", "3")
                        .param("clinica", "KuraVet Pinheiros")
                        .param("tipoPagamento", "Pix")
                        .param("valor", "150.00"))
                .andExpect(status().isOk())
                .andExpect(view().name("financeiro"))
                .andExpect(content().string(containsString("Pagamento so e aceito para consulta REALIZADA")))
                .andExpect(content().string(containsString("KuraVet Pinheiros")));
    }

    @Test
    void formularioSemConsultaNaoDeveChamarOBanco() throws Exception {
        mockMvc.perform(post("/portal/financeiro/pagamentos").with(user(veterinario())).with(csrf())
                        .param("clinica", "KuraVet Pinheiros"))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("pagamentoForm", "idConsulta"))
                .andExpect(content().string(containsString("Selecione a consulta.")));

        verify(financeiroService, never()).registrarPagamento(any());
    }

    @Test
    void pagamentoSemCsrfDeveSerNegado() throws Exception {
        mockMvc.perform(post("/portal/financeiro/pagamentos").with(user(veterinario()))
                        .param("idConsulta", "50"))
                .andExpect(status().isForbidden());

        verify(financeiroService, never()).registrarPagamento(any());
    }
}
