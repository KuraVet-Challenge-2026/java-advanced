package br.com.fiap.kuravet.ia;

public interface ExtratorSintomas {

    SintomasExtraidos extrair(String relato);

    MetodoExtracao metodo();
}
