--------------------------------------------------------------------------------
-- V4 - FLUXO DE APROVACAO DE TELECONSULTA
-- Amplia a maquina de estados da CONSULTA para cobrir a solicitacao feita pelo
-- tutor no app e a decisao do veterinario no portal (aprovar / recusar).
--------------------------------------------------------------------------------

ALTER TABLE CONSULTA ADD (
    DATA_SOLICITACAO  DATE,
    MOTIVO_RECUSA     VARCHAR2(300)
);

-- Consultas anteriores nasciam direto como AGENDADA, sem passar por solicitacao.
UPDATE CONSULTA SET DATA_SOLICITACAO = DATA_CONSULTA WHERE DATA_SOLICITACAO IS NULL;

ALTER TABLE CONSULTA MODIFY (DATA_SOLICITACAO DATE DEFAULT SYSDATE NOT NULL);

ALTER TABLE CONSULTA DROP CONSTRAINT KV_CK_CONS_STATUS;

ALTER TABLE CONSULTA ADD CONSTRAINT KV_CK_CONS_STATUS
    CHECK (STATUS IN ('SOLICITADA','AGENDADA','REALIZADA','CANCELADA','RECUSADA'));

-- O motivo existe se, e somente se, a solicitacao foi recusada.
ALTER TABLE CONSULTA ADD CONSTRAINT KV_CK_CONS_MOTIVO
    CHECK ( (STATUS =  'RECUSADA' AND MOTIVO_RECUSA IS NOT NULL)
         OR (STATUS <> 'RECUSADA' AND MOTIVO_RECUSA IS NULL) );

-- O diagnostico existe se, e somente se, a consulta foi realizada.
ALTER TABLE CONSULTA ADD CONSTRAINT KV_CK_CONS_DIAGNOSTICO
    CHECK ( (STATUS =  'REALIZADA' AND DIAGNOSTICO IS NOT NULL)
         OR (STATUS <> 'REALIZADA' AND DIAGNOSTICO IS NULL) );