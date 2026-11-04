package br.com.fiap.kuravet.dto.financeiro;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

@Data
public class PagamentoForm {

    // Mesmos valores do KV_CK_PAG_TIPO (V8)
    public static final Map<String, String> TIPOS_PAGAMENTO;

    static {
        Map<String, String> tipos = new LinkedHashMap<>();
        tipos.put("Pix", "Pix");
        tipos.put("Cartao de Credito", "Cartão de Crédito");
        tipos.put("Cartao de Debito", "Cartão de Débito");
        tipos.put("Dinheiro", "Dinheiro");
        TIPOS_PAGAMENTO = Collections.unmodifiableMap(tipos);
    }

    @NotNull(message = "Selecione a consulta.")
    private Long idConsulta;

    @Size(max = 60, message = "A clínica deve ter no máximo 60 caracteres.")
    private String clinica;

    private String tipoPagamento;

    @Digits(integer = 8, fraction = 2, message = "Informe o valor com até 8 dígitos e 2 casas decimais.")
    private BigDecimal valor;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dataPagamento = LocalDate.now();
}
