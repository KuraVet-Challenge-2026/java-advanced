package br.com.fiap.kuravet.repository.procedure;

import br.com.fiap.kuravet.dto.financeiro.LinhaFaturamentoDTO;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Types;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Chamada das procedures financeiras dos packages PL/SQL (V9). Os parametros sao declarados
 * na mesma ordem da spec do package, sem ler metadados do banco. As procedures nao fazem
 * COMMIT: a transacao e controlada por quem chama (FinanceiroService).
 */
@Repository
public class FinanceiroProcedureRepository {

    static final String P_ID_CONSULTA = "P_ID_CONSULTA";
    static final String P_CLINICA = "P_CLINICA";
    static final String P_TIPO_PAGAMENTO = "P_TIPO_PAGAMENTO";
    static final String P_VALOR = "P_VALOR";
    static final String P_DATA_PAGAMENTO = "P_DATA_PAGAMENTO";
    static final String P_ID_PAGAMENTO = "P_ID_PAGAMENTO";
    static final String P_DATA_INICIO = "P_DATA_INICIO";
    static final String P_DATA_FIM = "P_DATA_FIM";
    static final String P_CURSOR = "P_CURSOR";

    static final RowMapper<LinhaFaturamentoDTO> LINHA_FATURAMENTO = (rs, numeroLinha) -> new LinhaFaturamentoDTO(
            rs.getString("CLINICA"),
            rs.getString("TIPO_PAGAMENTO"),
            rs.getLong("QTD_PAGAMENTOS"),
            rs.getBigDecimal("VALOR_TOTAL"),
            rs.getInt("NIVEL"));

    private final SimpleJdbcCall cargaPagamento;
    private final SimpleJdbcCall faturamentoClinicaTipo;

    public FinanceiroProcedureRepository(JdbcTemplate jdbcTemplate) {
        this.cargaPagamento = new SimpleJdbcCall(jdbcTemplate)
                .withCatalogName("PKG_KV_CARGA")
                .withProcedureName("PRC_CARGA_PAGAMENTO")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter(P_ID_CONSULTA, Types.NUMERIC),
                        new SqlParameter(P_CLINICA, Types.VARCHAR),
                        new SqlParameter(P_TIPO_PAGAMENTO, Types.VARCHAR),
                        new SqlParameter(P_VALOR, Types.NUMERIC),
                        new SqlParameter(P_DATA_PAGAMENTO, Types.DATE),
                        new SqlOutParameter(P_ID_PAGAMENTO, Types.NUMERIC));

        this.faturamentoClinicaTipo = new SimpleJdbcCall(jdbcTemplate)
                .withCatalogName("PKG_KV_RELATORIO")
                .withProcedureName("PRC_FATURAMENTO_CLINICA_TIPO")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter(P_DATA_INICIO, Types.DATE),
                        new SqlParameter(P_DATA_FIM, Types.DATE),
                        new SqlOutParameter(P_CURSOR, Types.REF_CURSOR, LINHA_FATURAMENTO));
    }

    /** PKG_KV_CARGA.PRC_CARGA_PAGAMENTO; devolve o ID_PAGAMENTO gerado por SEQ_PAGAMENTO. */
    public long registrarPagamento(Long idConsulta, String clinica, String tipoPagamento,
                                   BigDecimal valor, LocalDate dataPagamento) {
        MapSqlParameterSource parametros = new MapSqlParameterSource()
                .addValue(P_ID_CONSULTA, idConsulta)
                .addValue(P_CLINICA, clinica)
                .addValue(P_TIPO_PAGAMENTO, tipoPagamento)
                .addValue(P_VALOR, valor)
                .addValue(P_DATA_PAGAMENTO, dataPagamento == null ? null : Date.valueOf(dataPagamento));

        Map<String, Object> saida = cargaPagamento.execute(parametros);
        return ((Number) saida.get(P_ID_PAGAMENTO)).longValue();
    }

    /** PKG_KV_RELATORIO.PRC_FATURAMENTO_CLINICA_TIPO; linhas na ordem do cursor (detalhe, subtotal, total). */
    @SuppressWarnings("unchecked")
    public List<LinhaFaturamentoDTO> faturamento(LocalDate inicio, LocalDate fim) {
        MapSqlParameterSource parametros = new MapSqlParameterSource()
                .addValue(P_DATA_INICIO, Date.valueOf(inicio))
                .addValue(P_DATA_FIM, Date.valueOf(fim));

        Map<String, Object> saida = faturamentoClinicaTipo.execute(parametros);
        Object linhas = saida.get(P_CURSOR);
        return linhas == null ? List.of() : List.copyOf((List<LinhaFaturamentoDTO>) linhas);
    }
}
