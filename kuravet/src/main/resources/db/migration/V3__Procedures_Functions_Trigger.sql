--------------------------------------------------------------------------------
-- V3 - PL/SQL: FUNCOES, PROCEDIMENTOS E TRIGGER DE AUDITORIA
-- Cada bloco PL/SQL e finalizado com "/" em linha propria, delimitador
-- reconhecido pelo parser de SQL do Flyway para Oracle.
--------------------------------------------------------------------------------

--------------------------------------------------------------------------------
-- 1. FUNCAO 1 - CONVERSOR JSON MANUAL (SEM FUNCOES NATIVAS DE JSON)
--------------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION FN_MONTA_JSON (
    p_id_consulta   IN NUMBER,
    p_pet           IN VARCHAR2,
    p_tutor         IN VARCHAR2,
    p_veterinario   IN VARCHAR2,
    p_data_consulta IN VARCHAR2,
    p_tipo_consulta IN VARCHAR2
) RETURN VARCHAR2
IS
    v_json          VARCHAR2(4000);
BEGIN
    IF p_id_consulta IS NULL THEN
        RAISE VALUE_ERROR;
    END IF;

    v_json := '{' ||
              '"id_consulta":' || TO_CHAR(p_id_consulta) || ',' ||
              '"pet":"'         || NVL(p_pet,'N/A')         || '",' ||
              '"tutor":"'       || NVL(p_tutor,'N/A')       || '",' ||
              '"veterinario":"' || NVL(p_veterinario,'N/A')   || '",' ||
              '"data_consulta":"' || NVL(p_data_consulta,'N/A') || '",' ||
              '"tipo_consulta":"' || NVL(p_tipo_consulta,'N/A') || '"' ||
              '}';

    RETURN v_json;
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        RETURN '{"erro":"Nenhum dado encontrado para montagem do JSON"}';
    WHEN TOO_MANY_ROWS THEN
        RETURN '{"erro":"Mais de um registro retornado para montagem do JSON"}';
    WHEN VALUE_ERROR THEN
        RETURN '{"erro":"Parametro obrigatorio invalido ou nulo"}';
    WHEN OTHERS THEN
        RETURN '{"erro":"Falha inesperada: ' || SQLERRM || '"}';
END FN_MONTA_JSON;
/

--------------------------------------------------------------------------------
-- 2. PROCEDIMENTO 1 - JOIN ENTRE TABELAS + USO DA FUNCAO JSON
--------------------------------------------------------------------------------
CREATE OR REPLACE PROCEDURE PROC_LISTAR_CONSULTAS_JSON
IS
    CURSOR c_consultas IS
        SELECT c.id_consulta,
               p.nome  AS nome_pet,
               t.nome  AS nome_tutor,
               v.nome  AS nome_vet,
               TO_CHAR(c.data_consulta,'DD/MM/YYYY') AS data_fmt,
               c.tipo_consulta
        FROM   CONSULTA c
        JOIN   PET p          ON p.id_pet = c.id_pet
        JOIN   TUTOR t        ON t.id_tutor = p.id_tutor
        JOIN   VETERINARIO v  ON v.id_veterinario = c.id_veterinario
        ORDER BY c.id_consulta;

    v_json          VARCHAR2(4000);
    v_total         NUMBER := 0;
BEGIN
    SELECT COUNT(*) INTO v_total FROM CONSULTA;

    IF v_total = 0 THEN
        RAISE NO_DATA_FOUND;
    END IF;

    DBMS_OUTPUT.PUT_LINE('=== LISTAGEM DE CONSULTAS (JSON MANUAL) ===');

    FOR r_reg IN c_consultas LOOP
        v_json := FN_MONTA_JSON(
                     p_id_consulta   => r_reg.id_consulta,
                     p_pet           => r_reg.nome_pet,
                     p_tutor         => r_reg.nome_tutor,
                     p_veterinario   => r_reg.nome_vet,
                     p_data_consulta => r_reg.data_fmt,
                     p_tipo_consulta => r_reg.tipo_consulta
                  );
        DBMS_OUTPUT.PUT_LINE(v_json);
    END LOOP;

EXCEPTION
    WHEN NO_DATA_FOUND THEN
        DBMS_OUTPUT.PUT_LINE('Aviso: Nenhuma consulta cadastrada no sistema.');
    WHEN TOO_MANY_ROWS THEN
        DBMS_OUTPUT.PUT_LINE('Erro: Retorno de dados excedeu o esperado.');
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('Erro inesperado em PROC_LISTAR_CONSULTAS_JSON: ' || SQLERRM);
END PROC_LISTAR_CONSULTAS_JSON;
/

--------------------------------------------------------------------------------
-- 3. PROCEDIMENTO 2 - SUBTOTAIS MANUAIS (COM FORMATACAO TABULAR)
--------------------------------------------------------------------------------
CREATE OR REPLACE PROCEDURE PROC_SUBTOTAIS_PAGAMENTO
IS
    CURSOR c_pag IS
        SELECT clinica, tipo_pagamento, id_pagamento, valor
        FROM   FATO_PAGAMENTO
        ORDER BY clinica, tipo_pagamento, id_pagamento;

    r_pag               c_pag%ROWTYPE;
    v_clinica_ant       VARCHAR2(60) := NULL;
    v_tipo_ant          VARCHAR2(30) := NULL;
    v_subtotal_tipo     NUMBER(10,2) := 0;
    v_subtotal_clinica  NUMBER(10,2) := 0;
    v_total_geral       NUMBER(10,2) := 0;
    v_qtd_linhas        NUMBER       := 0;
BEGIN
    OPEN c_pag;

    -- Cabecalho da Tabela
    DBMS_OUTPUT.PUT_LINE(RPAD('CLINICA', 25) || ' | ' || RPAD('TIPO PAGAMENTO', 20) || ' | ' || 'VALOR (R$)');
    DBMS_OUTPUT.PUT_LINE(RPAD('-', 25, '-') || '-|-' || RPAD('-', 20, '-') || '-|-' || RPAD('-', 15, '-'));

    LOOP
        FETCH c_pag INTO r_pag;
        EXIT WHEN c_pag%NOTFOUND;

        v_qtd_linhas := v_qtd_linhas + 1;

        -- QUEBRA DE CLINICA
        IF v_clinica_ant IS NOT NULL AND r_pag.clinica <> v_clinica_ant THEN
            DBMS_OUTPUT.PUT_LINE(RPAD('Sub Total', 25) || ' | ' || RPAD(' ', 20) || ' | ' || TO_CHAR(v_subtotal_clinica, 'FM999G999D00'));
            DBMS_OUTPUT.PUT_LINE(RPAD('-', 66, '-'));
            v_subtotal_clinica := 0;
            v_subtotal_tipo    := 0;
            v_tipo_ant         := NULL;
        END IF;

        v_clinica_ant := r_pag.clinica;
        v_tipo_ant := r_pag.tipo_pagamento;

        -- Imprime a linha atual
        DBMS_OUTPUT.PUT_LINE(RPAD(r_pag.clinica, 25) || ' | ' || RPAD(r_pag.tipo_pagamento, 20) || ' | ' || TO_CHAR(r_pag.valor, 'FM999G999D00'));

        -- ACUMULO MANUAL DOS TOTAIS
        v_subtotal_tipo    := v_subtotal_tipo + r_pag.valor;
        v_subtotal_clinica := v_subtotal_clinica + r_pag.valor;
        v_total_geral      := v_total_geral + r_pag.valor;
    END LOOP;

    CLOSE c_pag;

    IF v_qtd_linhas = 0 THEN
        RAISE NO_DATA_FOUND;
    END IF;

    -- FECHA O ULTIMO GRUPO E TOTAL GERAL
    IF v_clinica_ant IS NOT NULL THEN
        DBMS_OUTPUT.PUT_LINE(RPAD('Sub Total', 25) || ' | ' || RPAD(' ', 20) || ' | ' || TO_CHAR(v_subtotal_clinica, 'FM999G999D00'));
    END IF;

    DBMS_OUTPUT.PUT_LINE(RPAD('=', 66, '='));
    DBMS_OUTPUT.PUT_LINE(RPAD('Total Geral', 25) || ' | ' || RPAD(' ', 20) || ' | ' || TO_CHAR(v_total_geral, 'FM999G999D00'));

EXCEPTION
    WHEN NO_DATA_FOUND THEN
        DBMS_OUTPUT.PUT_LINE('Aviso: Nao ha registros na tabela FATO_PAGAMENTO.');
    WHEN TOO_MANY_ROWS THEN
        DBMS_OUTPUT.PUT_LINE('Erro: Consulta retornou mais linhas do que o esperado.');
    WHEN ZERO_DIVIDE THEN
        DBMS_OUTPUT.PUT_LINE('Erro: Divisao por zero detectada.');
    WHEN OTHERS THEN
        IF c_pag%ISOPEN THEN
            CLOSE c_pag;
        END IF;
        DBMS_OUTPUT.PUT_LINE('Erro inesperado: ' || SQLERRM);
END PROC_SUBTOTAIS_PAGAMENTO;
/

--------------------------------------------------------------------------------
-- 4. FUNCAO 2 - REGRA DE NEGOCIO: CALCULO DA IDADE EXATA DO PET
--------------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION FN_CALCULA_IDADE_PET (
    p_id_pet IN NUMBER
) RETURN VARCHAR2
IS
    v_data_nasc     PET.DATA_NASCIMENTO%TYPE;
    v_meses_total   NUMBER;
    v_anos          NUMBER;
    v_meses         NUMBER;
    v_resultado     VARCHAR2(200);
BEGIN
    IF p_id_pet IS NULL THEN
        RAISE VALUE_ERROR;
    END IF;

    SELECT data_nascimento
    INTO   v_data_nasc
    FROM   PET
    WHERE  id_pet = p_id_pet;

    v_meses_total := TRUNC(MONTHS_BETWEEN(SYSDATE, v_data_nasc));

    IF v_meses_total < 0 THEN
        RAISE VALUE_ERROR;
    END IF;

    v_anos  := TRUNC(v_meses_total / 12);
    v_meses := MOD(v_meses_total, 12);

    IF v_meses_total < 1 THEN
        v_resultado := 'Filhote recem-nascido (menos de 1 mes)';
    ELSE
        v_resultado := v_anos || ' ano(s) e ' || v_meses || ' mes(es)';
    END IF;

    RETURN v_resultado;

EXCEPTION
    WHEN NO_DATA_FOUND THEN
        RETURN 'Erro: Pet ID ' || p_id_pet || ' nao encontrado.';
    WHEN TOO_MANY_ROWS THEN
        RETURN 'Erro: Mais de um pet encontrado para o mesmo ID.';
    WHEN VALUE_ERROR THEN
        RETURN 'Erro: Parametro invalido ou data de nascimento inconsistente.';
    WHEN OTHERS THEN
        RETURN 'Erro inesperado: ' || SQLERRM;
END FN_CALCULA_IDADE_PET;
/

--------------------------------------------------------------------------------
-- 5. TRIGGER DE AUDITORIA (AFTER INSERT OR UPDATE OR DELETE) NA TABELA CONSULTA
--------------------------------------------------------------------------------
CREATE OR REPLACE TRIGGER TRG_AUDITORIA_CONSULTA
AFTER INSERT OR UPDATE OR DELETE ON CONSULTA
FOR EACH ROW
DECLARE
    v_operacao  VARCHAR2(10);
    v_old_vals  VARCHAR2(4000);
    v_new_vals  VARCHAR2(4000);
BEGIN
    IF INSERTING THEN
        v_operacao := 'INSERT';
        v_old_vals := NULL;
        v_new_vals := 'ID_CONSULTA=' || :NEW.id_consulta ||
                      ';ID_PET=' || :NEW.id_pet ||
                      ';ID_VETERINARIO=' || :NEW.id_veterinario ||
                      ';DATA_CONSULTA=' || TO_CHAR(:NEW.data_consulta,'DD/MM/YYYY') ||
                      ';TIPO_CONSULTA=' || :NEW.tipo_consulta ||
                      ';STATUS=' || :NEW.status;

    ELSIF UPDATING THEN
        v_operacao := 'UPDATE';
        v_old_vals := 'ID_CONSULTA=' || :OLD.id_consulta ||
                      ';ID_PET=' || :OLD.id_pet ||
                      ';ID_VETERINARIO=' || :OLD.id_veterinario ||
                      ';DATA_CONSULTA=' || TO_CHAR(:OLD.data_consulta,'DD/MM/YYYY') ||
                      ';TIPO_CONSULTA=' || :OLD.tipo_consulta ||
                      ';STATUS=' || :OLD.status;
        v_new_vals := 'ID_CONSULTA=' || :NEW.id_consulta ||
                      ';ID_PET=' || :NEW.id_pet ||
                      ';ID_VETERINARIO=' || :NEW.id_veterinario ||
                      ';DATA_CONSULTA=' || TO_CHAR(:NEW.data_consulta,'DD/MM/YYYY') ||
                      ';TIPO_CONSULTA=' || :NEW.tipo_consulta ||
                      ';STATUS=' || :NEW.status;

    ELSIF DELETING THEN
        v_operacao := 'DELETE';
        v_old_vals := 'ID_CONSULTA=' || :OLD.id_consulta ||
                      ';ID_PET=' || :OLD.id_pet ||
                      ';ID_VETERINARIO=' || :OLD.id_veterinario ||
                      ';DATA_CONSULTA=' || TO_CHAR(:OLD.data_consulta,'DD/MM/YYYY') ||
                      ';TIPO_CONSULTA=' || :OLD.tipo_consulta ||
                      ';STATUS=' || :OLD.status;
        v_new_vals := NULL;
    END IF;

    INSERT INTO AUDITORIA_LOG (USUARIO, OPERACAO, DATA_HORA, TABELA_AFETADA, VALORES_OLD, VALORES_NEW)
    VALUES (USER, v_operacao, SYSDATE, 'CONSULTA', v_old_vals, v_new_vals);
END TRG_AUDITORIA_CONSULTA;
/