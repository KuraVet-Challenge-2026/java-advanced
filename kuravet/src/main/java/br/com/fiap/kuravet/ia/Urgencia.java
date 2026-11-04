package br.com.fiap.kuravet.ia;

// A ordem das constantes importa: maior() usa o ordinal
public enum Urgencia {
    BAIXA,
    MODERADA,
    ALTA,
    EMERGENCIA;

    public Urgencia maior(Urgencia outra) {
        if (outra == null) {
            return this;
        }
        return compareTo(outra) >= 0 ? this : outra;
    }

    public boolean sugereTeleconsultaPrioritaria() {
        return this == ALTA || this == EMERGENCIA;
    }
}
