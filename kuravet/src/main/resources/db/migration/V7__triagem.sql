--------------------------------------------------------------------------------
-- V7 - TRIAGEM POR IA
-- Registra cada triagem feita pelo tutor no app: o relato livre, os sintomas
-- extraidos, a urgencia decidida pelo motor de regras deterministico e as
-- regras que justificam a decisao (auditoria). A LLM nunca decide a urgencia.
-- Colunas de texto livre usam semantica CHAR para aceitar acentos sem estourar
-- o limite em bytes.
--------------------------------------------------------------------------------

CREATE TABLE TRIAGEM (
    ID_TRIAGEM        NUMBER(10)           NOT NULL,
    ID_PET            NUMBER(6)            NOT NULL,
    RELATO            VARCHAR2(1000 CHAR)  NOT NULL,
    SINTOMAS          VARCHAR2(500),
    URGENCIA          VARCHAR2(20)         NOT NULL,
    RECOMENDACAO      VARCHAR2(400 CHAR)   NOT NULL,
    REGRAS_APLICADAS  VARCHAR2(1000 CHAR)  NOT NULL,
    METODO_EXTRACAO   VARCHAR2(20)         NOT NULL,
    DATA_TRIAGEM      TIMESTAMP            DEFAULT SYSTIMESTAMP NOT NULL,
    CONSTRAINT KV_PK_TRIAGEM PRIMARY KEY (ID_TRIAGEM),
    CONSTRAINT KV_CK_TRI_URGENCIA CHECK (URGENCIA IN ('BAIXA','MODERADA','ALTA','EMERGENCIA')),
    CONSTRAINT KV_CK_TRI_METODO CHECK (METODO_EXTRACAO IN ('PALAVRAS_CHAVE','LLM')),
    CONSTRAINT KV_FK_TRI_PET FOREIGN KEY (ID_PET) REFERENCES PET(ID_PET)
);

CREATE INDEX KV_IX_TRI_PET ON TRIAGEM (ID_PET);

CREATE SEQUENCE SEQ_TRIAGEM START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
