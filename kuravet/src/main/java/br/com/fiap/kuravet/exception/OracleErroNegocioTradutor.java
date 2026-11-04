package br.com.fiap.kuravet.exception;

import java.sql.SQLException;
import java.util.OptionalInt;

public final class OracleErroNegocioTradutor {

    static final int CODIGO_MINIMO = 20000;
    static final int CODIGO_MAXIMO = 20999;

    // PKG_KV_UTIL.c_err_inesperado
    static final int CODIGO_INESPERADO = 20099;
    static final String MENSAGEM_INESPERADO = "Erro inesperado no banco de dados. Tente novamente mais tarde.";

    private OracleErroNegocioTradutor() {
    }

    public static OptionalInt codigoErroNegocio(Throwable erro) {
        SQLException sql = primeiraSqlException(erro);
        if (sql == null || sql.getErrorCode() < CODIGO_MINIMO || sql.getErrorCode() > CODIGO_MAXIMO) {
            return OptionalInt.empty();
        }
        return OptionalInt.of(sql.getErrorCode());
    }

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
