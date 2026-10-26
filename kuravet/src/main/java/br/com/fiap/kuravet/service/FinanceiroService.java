package br.com.fiap.kuravet.service;

import br.com.fiap.kuravet.dto.financeiro.LinhaFaturamentoDTO;
import br.com.fiap.kuravet.dto.financeiro.PagamentoForm;
import br.com.fiap.kuravet.exception.OracleErroNegocioTradutor;
import br.com.fiap.kuravet.exception.RegraDeNegocioException;
import br.com.fiap.kuravet.repository.procedure.FinanceiroProcedureRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.OptionalInt;

// As regras de pagamento vivem no PL/SQL; aqui so se abre a transacao (as procedures nao fazem COMMIT)
// e se traduzem os erros ORA-200xx.
@Service
public class FinanceiroService {

    private static final Logger log = LoggerFactory.getLogger(FinanceiroService.class);

    // PKG_KV_UTIL.c_err_sem_dados: o relatorio sem pagamentos no periodo e um estado vazio, nao um erro.
    static final int CODIGO_SEM_DADOS = 20013;

    private final FinanceiroProcedureRepository repository;

    public FinanceiroService(FinanceiroProcedureRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public long registrarPagamento(PagamentoForm form) {
        long idPagamento;
        try {
            idPagamento = repository.registrarPagamento(form.getIdConsulta(), form.getClinica(),
                    form.getTipoPagamento(), form.getValor(), form.getDataPagamento());
        } catch (RuntimeException e) {
            throw OracleErroNegocioTradutor.traduzir(e);
        }

        log.info("Pagamento registrado: idPagamento={}, idConsulta={}", idPagamento, form.getIdConsulta());
        return idPagamento;
    }

    @Transactional
    public List<LinhaFaturamentoDTO> faturamento(LocalDate inicio, LocalDate fim) {
        if (inicio == null || fim == null) {
            throw new RegraDeNegocioException("Informe a data inicial e a data final do período.");
        }
        if (inicio.isAfter(fim)) {
            throw new RegraDeNegocioException("A data inicial não pode ser posterior à data final.");
        }

        try {
            return repository.faturamento(inicio, fim);
        } catch (RuntimeException e) {
            OptionalInt codigo = OracleErroNegocioTradutor.codigoErroNegocio(e);
            if (codigo.isPresent() && codigo.getAsInt() == CODIGO_SEM_DADOS) {
                return List.of();
            }
            throw OracleErroNegocioTradutor.traduzir(e);
        }
    }
}
