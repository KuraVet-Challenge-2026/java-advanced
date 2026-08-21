--------------------------------------------------------------------------------
-- V3 - TUTOR INICIAL E USUARIO DE PERFIL TUTOR (ACESSO PELO APP MOBILE)
-- Equivalente ao INSERT que era feito manualmente no Oracle.
--------------------------------------------------------------------------------

INSERT INTO TUTOR (ID_TUTOR, NOME, CPF, TELEFONE, EMAIL, ENDERECO, DATA_CADASTRO)
VALUES (1, 'Ana Beatriz Souza', '111.222.333-44', '(11) 91234-5601', 'ana.souza@email.com', 'Rua das Flores, 120 - Sao Paulo/SP', DATE '2024-01-10');

-- Login do tutor inicial para o app mobile. Senha: tutor123 (hash BCrypt).
INSERT INTO USUARIO (ID_USUARIO, USERNAME, SENHA, PERFIL, ID_TUTOR)
VALUES (2, 'tutor', '$2a$10$3WVhO4RxXg2bIcY29vWEZuVxYB6YyCagYoUzk3ipAneHkKtyglM12', 'TUTOR', 1);

COMMIT;
