package br.com.fiap.kuravet.ia;

/**
 * Transforma o relato livre do tutor em sintomas estruturados.
 * Nao decide urgencia: isso e responsabilidade do {@link MotorRegrasTriagem}.
 */
public interface ExtratorSintomas {

    SintomasExtraidos extrair(String relato);

    MetodoExtracao metodo();
}
