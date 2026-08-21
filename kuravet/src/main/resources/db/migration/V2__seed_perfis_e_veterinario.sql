--------------------------------------------------------------------------------
-- V2 - VETERINARIOS E USUARIO DE PERFIL VETERINARIO (ACESSO AO PORTAL WEB)
--------------------------------------------------------------------------------

INSERT INTO VETERINARIO (ID_VETERINARIO, NOME, CRMV, ESPECIALIDADE, TELEFONE, EMAIL) VALUES (1,'Dra. Patricia Gomes','CRMV-SP 12345','Clinica Geral','(11) 3222-1001','patricia.gomes@kuravet.com');
INSERT INTO VETERINARIO (ID_VETERINARIO, NOME, CRMV, ESPECIALIDADE, TELEFONE, EMAIL) VALUES (2,'Dr. Rafael Andrade','CRMV-SP 12346','Dermatologia','(11) 3222-1002','rafael.andrade@kuravet.com');
INSERT INTO VETERINARIO (ID_VETERINARIO, NOME, CRMV, ESPECIALIDADE, TELEFONE, EMAIL) VALUES (3,'Dra. Camila Duarte','CRMV-RJ 22345','Cardiologia','(21) 3222-1003','camila.duarte@kuravet.com');
INSERT INTO VETERINARIO (ID_VETERINARIO, NOME, CRMV, ESPECIALIDADE, TELEFONE, EMAIL) VALUES (4,'Dr. Bruno Cardoso','CRMV-MG 32345','Ortopedia','(31) 3222-1004','bruno.cardoso@kuravet.com');
INSERT INTO VETERINARIO (ID_VETERINARIO, NOME, CRMV, ESPECIALIDADE, TELEFONE, EMAIL) VALUES (5,'Dra. Larissa Ferreira','CRMV-PR 42345','Clinica Geral','(41) 3222-1005','larissa.ferreira@kuravet.com');
INSERT INTO VETERINARIO (ID_VETERINARIO, NOME, CRMV, ESPECIALIDADE, TELEFONE, EMAIL) VALUES (6,'Dr. Eduardo Santos','CRMV-RS 52345','Cirurgia','(51) 3222-1006','eduardo.santos@kuravet.com');
INSERT INTO VETERINARIO (ID_VETERINARIO, NOME, CRMV, ESPECIALIDADE, TELEFONE, EMAIL) VALUES (7,'Dra. Renata Barbosa','CRMV-PE 62345','Oncologia','(81) 3222-1007','renata.barbosa@kuravet.com');
INSERT INTO VETERINARIO (ID_VETERINARIO, NOME, CRMV, ESPECIALIDADE, TELEFONE, EMAIL) VALUES (8,'Dr. Felipe Ramos','CRMV-DF 72345','Clinica Geral','(61) 3222-1008','felipe.ramos@kuravet.com');
INSERT INTO VETERINARIO (ID_VETERINARIO, NOME, CRMV, ESPECIALIDADE, TELEFONE, EMAIL) VALUES (9,'Dra. Vanessa Lopes','CRMV-CE 82345','Nutricao','(85) 3222-1009','vanessa.lopes@kuravet.com');
INSERT INTO VETERINARIO (ID_VETERINARIO, NOME, CRMV, ESPECIALIDADE, TELEFONE, EMAIL) VALUES (10,'Dr. Thiago Moreira','CRMV-BA 92345','Clinica Geral','(71) 3222-1010','thiago.moreira@kuravet.com');

-- Login generico de veterinario para o portal web. Senha: vet123 (hash BCrypt).
INSERT INTO USUARIO (ID_USUARIO, USERNAME, SENHA, PERFIL, ID_TUTOR)
VALUES (1, 'veterinario', '$2a$10$n8nMAul6jCsVTU6fdERGpe5PzwtPRGBY24Fk6iEWbyuoTjLXGxTRC', 'VETERINARIO', NULL);

COMMIT;
