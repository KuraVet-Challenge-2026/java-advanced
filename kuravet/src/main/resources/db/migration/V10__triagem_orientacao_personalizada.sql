--------------------------------------------------------------------------------
-- V10 - ORIENTACAO PERSONALIZADA DA TRIAGEM
-- Depois que o motor de regras decide a urgencia, o backend escreve uma
-- orientacao curta ao tutor (Gemini, ou modelo fixo quando a LLM esta
-- desligada ou falha) e grava de onde ela veio. A orientacao nunca altera a
-- urgencia nem a recomendacao.
-- Colunas anulaveis: triagens anteriores a esta versao ficam sem orientacao.
-- ORIENTACAO usa semantica CHAR para aceitar acentos sem estourar o limite em
-- bytes.
--------------------------------------------------------------------------------

ALTER TABLE TRIAGEM ADD (
    ORIENTACAO         VARCHAR2(1000 CHAR),
    ORIGEM_ORIENTACAO  VARCHAR2(20)
);

ALTER TABLE TRIAGEM ADD CONSTRAINT KV_CK_TRI_ORIGEM
    CHECK (ORIGEM_ORIENTACAO IS NULL OR ORIGEM_ORIENTACAO IN ('IA_GENERATIVA','MODELO_FIXO'));
