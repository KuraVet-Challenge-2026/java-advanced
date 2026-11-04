package br.com.fiap.kuravet.ia;

public interface GeradorOrientacao {

    String gerar(ContextoOrientacao contexto);

    OrigemOrientacao origem();
}
