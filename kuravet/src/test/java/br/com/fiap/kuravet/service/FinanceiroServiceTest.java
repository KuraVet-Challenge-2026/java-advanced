package br.com.fiap.kuravet.service;

import br.com.fiap.kuravet.dto.financeiro.LinhaFaturamentoDTO;
import br.com.fiap.kuravet.dto.financeiro.PagamentoForm;
import br.com.fiap.kuravet.exception.RegraDeNegocioException;
import br.com.fiap.kuravet.repository.procedure.FinanceiroProcedureRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.UncategorizedSQLException;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FinanceiroServiceTest {

    private static final LocalDate INICIO = LocalDate.of(2026, 1, 1);
    private static final LocalDate FIM = LocalDate.of(2026, 12, 31);

    @Mock
    private FinanceiroProcedureRepository repository;

    @InjectMocks
    private FinanceiroService service;

    private static PagamentoForm form() {
        PagamentoForm form = new PagamentoForm();
        form.setIdConsulta(50L);
        form.setClinica("KuraVet Pinheiros");
        form.setTipoPagamento("Pix");
        form.setValor(new BigDecimal("150.00"));
        form.setDataPagamento(LocalDate.of(2026, 10, 20));
        return form;
    }

    private static UncategorizedSQLException erroDoBanco(int codigo, String mensagem) {
        return new UncategorizedSQLException("call", "{call ...}", new SQLException(mensagem, "72000", codigo));
    }

    // ---------- registrarPagamento ----------

    @Test
    void registrarPagamentoDeveRetornarOIdGeradoPelaProcedure() {
        when(repository.registrarPagamento(50L, "KuraVet Pinheiros", "Pix",
                new BigDecimal("150.00"), LocalDate.of(2026, 10, 20))).thenReturn(101L);

        assertThat(service.registrarPagamento(form())).isEqualTo(101L);
    }

    @Test
    void erroDeNegocioDaProcedureDeveVirarRegraDeNegocio() {
        when(repository.registrarPagamento(any(), any(), any(), any(), any()))
                .thenThrow(erroDoBanco(20008, "ORA-20008: Valor deve ser maior que zero (recebido: 0)\nORA-06512: em linha 1"));

        assertThatThrownBy(() -> service.registrarPagamento(form()))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessage("Valor deve ser maior que zero (recebido: 0)");
    }

    @Test
    void erroTecnicoDoBancoDeveSubirSemTraducao() {
        UncategorizedSQLException falha = erroDoBanco(17002, "IO Error: The Network Adapter could not establish the connection");
        when(repository.registrarPagamento(any(), any(), any(), any(), any())).thenThrow(falha);

        assertThatThrownBy(() -> service.registrarPagamento(form())).isSameAs(falha);
    }

    // ---------- faturamento ----------

    @Test
    void periodoInvertidoDeveSerRecusadoSemChamarOBanco() {
        assertThatThrownBy(() -> service.faturamento(FIM, INICIO))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessage("A data inicial não pode ser posterior à data final.");

        verifyNoInteractions(repository);
    }

    @Test
    void periodoIncompletoDeveSerRecusadoSemChamarOBanco() {
        assertThatThrownBy(() -> service.faturamento(INICIO, null))
                .isInstanceOf(RegraDeNegocioException.class);

        verifyNoInteractions(repository);
    }

    @Test
    void faturamentoDeveRetornarAsLinhasDoCursor() {
        List<LinhaFaturamentoDTO> linhas = List.of(
                new LinhaFaturamentoDTO("KuraVet Moema", "Pix", 1, new BigDecimal("90.00"), 0),
                new LinhaFaturamentoDTO("KuraVet Moema", "SUBTOTAL", 1, new BigDecimal("90.00"), 1),
                new LinhaFaturamentoDTO("TOTAL GERAL", null, 1, new BigDecimal("90.00"), 3));
        when(repository.faturamento(INICIO, FIM)).thenReturn(linhas);

        assertThat(service.faturamento(INICIO, FIM)).isEqualTo(linhas);
    }

    @Test
    void periodoSemPagamentosDeveVirarListaVazia() {
        when(repository.faturamento(INICIO, FIM))
                .thenThrow(erroDoBanco(20013, "ORA-20013: Nenhum pagamento entre 01/01/2026 e 31/12/2026"));

        assertThat(service.faturamento(INICIO, FIM)).isEmpty();
    }
}
