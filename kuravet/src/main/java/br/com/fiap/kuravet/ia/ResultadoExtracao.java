package br.com.fiap.kuravet.ia;

/**
 * Sintomas extraidos junto com o metodo que realmente os produziu.
 */
public record ResultadoExtracao(SintomasExtraidos sintomas, MetodoExtracao metodo) {
}
