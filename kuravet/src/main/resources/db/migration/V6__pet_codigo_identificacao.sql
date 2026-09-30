--------------------------------------------------------------------------------
-- V6 - CODIGO DE IDENTIFICACAO OPCIONAL DO PET
-- Permite registrar um codigo de identificacao ja existente do pet (ex.: codigo
-- da Petz, numero de microchip). Campo opcional e unico quando preenchido; no
-- Oracle, uma UNIQUE constraint de coluna unica aceita varios registros NULL.
--------------------------------------------------------------------------------

ALTER TABLE PET ADD (CODIGO_IDENTIFICACAO VARCHAR2(40));

ALTER TABLE PET ADD CONSTRAINT KV_UQ_PET_CODIGO UNIQUE (CODIGO_IDENTIFICACAO);
