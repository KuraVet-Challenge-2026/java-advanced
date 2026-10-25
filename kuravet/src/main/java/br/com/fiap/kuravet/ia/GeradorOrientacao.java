package br.com.fiap.kuravet.ia;

/**
 * Escreve uma orientacao personalizada ao tutor a partir de uma triagem ja decidida.
 * Nao decide nem altera urgencia ou recomendacao: isso e responsabilidade do {@link MotorRegrasTriagem}.
 */
public interface GeradorOrientacao {

    String gerar(ContextoOrientacao contexto);

    OrigemOrientacao origem();
}
