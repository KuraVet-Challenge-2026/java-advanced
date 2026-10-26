package br.com.fiap.kuravet.exception;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.UncategorizedSQLException;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

class OracleErroNegocioTradutorTest {

    private static UncategorizedSQLException erroDoBanco(int codigo, String mensagem) {
        return new UncategorizedSQLException("PRC_CARGA_PAGAMENTO", "{call PKG_KV_CARGA.PRC_CARGA_PAGAMENTO(?, ?, ?, ?, ?, ?)}",
                new SQLException(mensagem, "72000", codigo));
    }

    @Test
    void erroDeNegocioDeveVirarRegraDeNegocioComAMensagemLimpa() {
        UncategorizedSQLException erro = erroDoBanco(20011,
                "ORA-20011: Pagamento so e aceito para consulta REALIZADA (consulta 3 esta AGENDADA)\n"
                        + "ORA-06512: em \"KURAVET.PKG_KV_UTIL\", line 52\n"
                        + "ORA-06512: em \"KURAVET.PKG_KV_CARGA\", line 280");

        RuntimeException traduzida = OracleErroNegocioTradutor.traduzir(erro);

        assertThat(traduzida)
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessage("Pagamento so e aceito para consulta REALIZADA (consulta 3 esta AGENDADA)");
    }

    @Test
    void erroQueNaoEDeNegocioDeveSubirSemTraducao() {
        DuplicateKeyException erro = new DuplicateKeyException("violacao",
                new SQLException("ORA-00001: restrição exclusiva violada", "23000", 1));

        assertThat(OracleErroNegocioTradutor.traduzir(erro)).isSameAs(erro);
        assertThat(OracleErroNegocioTradutor.codigoErroNegocio(erro)).isEmpty();
    }

    @Test
    void erroSemSqlExceptionNaCadeiaDeveSubirSemTraducao() {
        IllegalStateException erro = new IllegalStateException("sem conexão");

        assertThat(OracleErroNegocioTradutor.traduzir(erro)).isSameAs(erro);
    }

    @Test
    void deveInformarOCodigoDoErroDeNegocio() {
        assertThat(OracleErroNegocioTradutor.codigoErroNegocio(erroDoBanco(20013, "ORA-20013: Nenhum pagamento")))
                .hasValue(20013);
    }

    @Test
    void erroInesperadoDoPackageNaoDeveExporOOraOriginal() {
        UncategorizedSQLException erro = erroDoBanco(20099,
                "ORA-20099: Erro inesperado em PKG_KV_CARGA.PRC_CARGA_PAGAMENTO: ORA-12899: valor muito grande");

        RuntimeException traduzida = OracleErroNegocioTradutor.traduzir(erro);

        assertThat(traduzida).isInstanceOf(RegraDeNegocioException.class);
        assertThat(traduzida.getMessage()).doesNotContain("ORA-").isEqualTo(OracleErroNegocioTradutor.MENSAGEM_INESPERADO);
    }
}
