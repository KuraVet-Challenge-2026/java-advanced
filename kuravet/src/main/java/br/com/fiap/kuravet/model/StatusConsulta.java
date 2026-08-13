package br.com.fiap.kuravet.model;

/**
 * Espelha a CHECK CONSTRAINT KV_CK_CONS_STATUS da tabela CONSULTA
 * (STATUS IN ('AGENDADA','REALIZADA','CANCELADA')).
 */
public enum StatusConsulta {
    AGENDADA,
    REALIZADA,
    CANCELADA
}
