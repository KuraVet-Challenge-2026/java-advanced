package br.com.fiap.kuravet.dto.financeiro;

import java.math.BigDecimal;

/**
 * Linha do cursor de PKG_KV_RELATORIO.PRC_FATURAMENTO_CLINICA_TIPO.
 * NIVEL vem de GROUPING_ID(CLINICA, TIPO_PAGAMENTO): 0 = detalhe, 1 = subtotal da clinica, 3 = total geral.
 */
public record LinhaFaturamentoDTO(
        String clinica,
        String tipoPagamento,
        long qtdPagamentos,
        BigDecimal valorTotal,
        int nivel
) {

    public static final int NIVEL_DETALHE = 0;
    public static final int NIVEL_SUBTOTAL = 1;
    public static final int NIVEL_TOTAL_GERAL = 3;

    public boolean isDetalhe() {
        return nivel == NIVEL_DETALHE;
    }

    public boolean isSubtotal() {
        return nivel == NIVEL_SUBTOTAL;
    }

    public boolean isTotalGeral() {
        return nivel == NIVEL_TOTAL_GERAL;
    }
}
