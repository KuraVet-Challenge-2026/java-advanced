package br.com.fiap.kuravet.exception;

import java.sql.SQLException;
import java.util.OptionalInt;

/**
 * Converte erros de negocio lancados pelos packages PL/SQL (RAISE_APPLICATION_ERROR,
 * ORA-20000..ORA-20999) em {@link RegraDeNegocioException} com a mensagem do banco.
 * Qualquer outro erro sobe sem traducao.
 */
public final class OracleErroNegocioTradutor {

    static final int CODIGO_MINIMO = 20000;
    static final int CODIGO_MAXIMO = 20999;

    // PKG_KV_UTIL.c_err_inesperado: a mensagem carrega o ORA original, que nao vai para a tela.
    static final int CODIGO_INESPERADO = 20099;
    static final String MENSAGEM_INESPERADO = "Erro inesperado no banco de dados. Tente novamente mais tarde.";

    private OracleErroNegocioTradutor() {
    }

    /** Codigo positivo do erro de negocio (ex.: 20011), se houver um na cadeia de causas. */
    public static OptionalInt codigoErroNegocio(Throwable erro) {
        SQLException sql = primeiraSqlException(erro);
        if (sql == null || sql.getErrorCode() < CODIGO_MINIMO || sql.getErrorCode() > CODIGO_MAXIMO) {
            return OptionalInt.empty();
        }
        return OptionalInt.of(sql.getErrorCode());
    }

    /** Devolve a excecao a lancar: {@link RegraDeNegocioException} para ORA-200xx, ou o proprio erro. */
    public static RuntimeException traduzir(RuntimeException erro) {
        OptionalInt codigo = codigoErroNegocio(erro);
        if (codigo.isEmpty()) {
            return erro;
        }
        if (codigo.getAsInt() == CODIGO_INESPERADO) {
            return new RegraDeNegocioException(MENSAGEM_INESPERADO);
        }
        return new RegraDeNegocioException(mensagemLimpa(primeiraSqlException(erro).getMessage()));
    }

    // "ORA-20011: Pagamento so e aceito...\nORA-06512: at ..." -> "Pagamento so e aceito..."
    static String mensagemLimpa(String mensagem) {
        if (mensagem == null) {
            return "";
        }
        String primeiraLinha = mensagem.lines().findFirst().orElse("");
        return primeiraLinha.replaceFirst("^ORA-\\d{5}:\\s*", "").strip();
    }

    private static SQLException primeiraSqlException(Throwable erro) {
        for (Throwable atual = erro; atual != null; atual = atual.getCause()) {
            if (atual instanceof SQLException sql) {
                return sql;
            }
            if (atual.getCause() == atual) {
                break;
            }
        }
        return null;
    }
}
