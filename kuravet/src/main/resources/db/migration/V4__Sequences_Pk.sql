--------------------------------------------------------------------------------
-- V4 - SEQUENCES PARA GERACAO DE PK DAS TABELAS CORE
-- A carga inicial (V2) usa os IDs 1..10 manualmente; as sequences comecam
-- em 11 para nao colidir com os registros ja existentes.
--------------------------------------------------------------------------------

CREATE SEQUENCE SEQ_TUTOR       START WITH 11 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE SEQ_VETERINARIO START WITH 11 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE SEQ_PET         START WITH 11 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE SEQ_CONSULTA    START WITH 11 INCREMENT BY 1 NOCACHE NOCYCLE;
