package br.com.fiap.kuravet.ia;

/**
 * Orientacao final ao tutor (ja com o aviso) junto com a origem que realmente a produziu.
 */
public record OrientacaoGerada(String texto, OrigemOrientacao origem) {
}
