package br.com.fiap.kuravet.ia;

public enum Sintoma {
    VOMITO("Vômito"),
    DIARREIA("Diarreia"),
    FALTA_DE_APETITE("Falta de apetite"),
    APATIA("Apatia"),
    FEBRE("Febre"),
    TOSSE("Tosse"),
    ESPIRROS("Espirros"),
    COCEIRA("Coceira"),
    FERIDA("Ferida"),
    MANCANDO("Mancando"),
    DIFICULDADE_RESPIRAR("Dificuldade para respirar"),
    CONVULSAO("Convulsão"),
    SANGRAMENTO("Sangramento"),
    INGESTAO_TOXICA("Ingestão de substância tóxica"),
    NAO_URINA("Não urina"),
    DESMAIO("Desmaio");

    private final String descricao;

    Sintoma(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
