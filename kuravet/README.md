# 🐾 KuraVet

Plataforma de saúde preventiva contínua para pets. Backend em **Spring Boot** que expõe:

- um **portal web** (Thymeleaf + Spring Security), em `/portal/**`, restrito ao perfil VETERINARIO;
- uma **API REST** (`/api/**`) consumida por um app mobile (React Native), autenticada via HTTP Basic
  contra a tabela `USUARIO` — um TUTOR só enxerga e altera os próprios pets e consultas.

Projeto acadêmico (FIAP) que utiliza banco de dados **Oracle** em nuvem, com controle de schema via **Flyway**.

---

## Sumário

- [Stack utilizada](#stack-utilizada)
- [Pré-requisitos](#pré-requisitos)
- [Configuração](#configuração)
- [Instalação e execução](#instalação-e-execução)
- [Migrações do banco (Flyway)](#migrações-do-banco-flyway)
- [Acesso à aplicação](#acesso-à-aplicação)
  - [Portal web](#portal-web-thymeleaf)
  - [API REST](#api-rest)
- [Tratamento de erros da API](#tratamento-de-erros-da-api)
- [Estrutura do projeto](#estrutura-do-projeto)
- [Problemas comuns](#problemas-comuns)

---

## Stack utilizada

| Camada          | Tecnologia                                   |
|-----------------|-----------------------------------------------|
| Linguagem       | Java 21                                       |
| Framework       | Spring Boot 4.1.0                             |
| Persistência    | Spring Data JPA + Hibernate                   |
| Banco de dados  | Oracle Database (driver `ojdbc11`)            |
| Migrações       | Flyway (`flyway-database-oracle`)             |
| Views           | Thymeleaf                                     |
| Segurança       | Spring Security (form login + API stateless)  |
| Build           | Maven (via `mvnw`)                            |
| Utilitários     | Lombok, Bean Validation (Jakarta Validation)  |

---

## Pré-requisitos

- **JDK 21** ou superior instalado e configurado no `PATH` (`java -version`).
- Não é necessário instalar o Maven — o projeto já inclui o Maven Wrapper (`mvnw` / `mvnw.cmd`).
- Acesso de rede ao banco Oracle da instituição (`oracle.fiap.com.br:1521`), incluindo estar na rede/VPN da FIAP quando exigido.
- Credenciais de um schema Oracle válido (usuário/senha fornecidos pela FIAP).
- IDE recomendada: IntelliJ IDEA (o projeto já foi testado nela), mas qualquer IDE com suporte a Maven funciona.

---

## Configuração

As configurações da aplicação ficam em:

```
src/main/resources/application.properties
```

Principais propriedades:

```properties
spring.application.name=kuravet

# Datasource Oracle FIAP
spring.datasource.url=jdbc:oracle:thin:@oracle.fiap.com.br:1521:ORCL
spring.datasource.username=SEU_USUARIO
spring.datasource.password=SUA_SENHA
spring.datasource.driver-class-name=oracle.jdbc.OracleDriver

# Flyway
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
spring.flyway.baseline-on-migrate=true
spring.flyway.baseline-version=0

# Thymeleaf
spring.thymeleaf.cache=false

# Schema é propriedade do Flyway; Hibernate só valida contra ele
spring.jpa.hibernate.ddl-auto=validate
```

> ⚠️ **Antes de rodar**, substitua `spring.datasource.username` e `spring.datasource.password` pelas suas credenciais do banco Oracle da FIAP. Evite commitar credenciais reais em repositórios públicos — prefira variáveis de ambiente ou um `application-local.properties` fora do controle de versão quando possível.
>
> ⚠️ O arquivo deve estar salvo em **UTF-8**. Se o editor salvar em outra codificação (ex.: ISO-8859-1), o Maven falha ao copiar o arquivo para `target/classes` durante o build (`MalformedInputException`), e a aplicação sobe com as propriedades vazias.

Se preferir sobrescrever a URL/usuário/senha sem editar o arquivo, é possível passar via variáveis de ambiente ou argumentos de linha de comando, por exemplo:

```bash
./mvnw spring-boot:run -Dspring-boot.run.arguments="--spring.datasource.username=SEU_USUARIO --spring.datasource.password=SUA_SENHA"
```

---

## Instalação e execução

Clone o repositório e entre na pasta do módulo Spring Boot:

```bash
git clone <url-do-repositorio>
cd java-advanced/kuravet
```

### Opção 1 — Maven Wrapper (linha de comando)

**Windows (PowerShell / cmd):**
```powershell
.\mvnw.cmd clean install
.\mvnw.cmd spring-boot:run
```

**Linux / macOS / Git Bash:**
```bash
./mvnw clean install
./mvnw spring-boot:run
```

### Opção 2 — Pela IDE (IntelliJ IDEA)

1. Abra a pasta `kuravet` como projeto Maven (o IntelliJ importa o `pom.xml` automaticamente).
2. Configure as credenciais do banco em `application.properties` (veja [Configuração](#configuração)).
3. Rode a classe principal `br.com.fiap.kuravet.KuravetApplication` (botão ▶️ ao lado do `main`).

Ao subir com sucesso, o log deve mostrar algo como:

```
Tomcat initialized with port 8080 (http)
...
Started KuravetApplication in X seconds
```

A aplicação fica disponível em **http://localhost:8080**.

> O projeto usa **Spring Boot DevTools**, então alterações no código-fonte reiniciam a aplicação automaticamente durante o desenvolvimento.

---

## Migrações do banco (Flyway)

O schema é criado e versionado automaticamente pelo Flyway ao iniciar a aplicação, a partir dos scripts em:

```
src/main/resources/db/migration/
├── V1__schema.sql                        # DDL: tutor, veterinario, pet, consulta, usuario
├── V2__seed_perfis_e_veterinario.sql      # veterinários + usuário de perfil VETERINARIO
└── V3__seed_tutor_inicial.sql             # tutor inicial + usuário de perfil TUTOR
```

Não é necessário rodar nenhum script manualmente — basta iniciar a aplicação com um schema Oracle válido e vazio (ou já controlado pelo Flyway) que as tabelas e os dados iniciais são criados na primeira execução. O schema é propriedade do Flyway: `spring.jpa.hibernate.ddl-auto=validate` faz o Hibernate só conferir se as entidades batem com o que o Flyway criou, nunca alterar o schema.

> ⚠️ Se o seu schema Oracle já tinha as migrations antigas (`V1__Criar_Tabelas_Core.sql` e demais) aplicadas, o histórico do Flyway (`flyway_schema_history`) não bate mais com este novo conjunto V1–V3. Antes de rodar a aplicação, resete o schema — veja [Problemas comuns](#problemas-comuns).

O arquivo `script_bd.sql`, na raiz do módulo, é um script SQL independente (procedures, function, trigger de auditoria, tabela de pagamentos) para execução manual em um cliente Oracle (ex. SQL Developer); não faz parte do schema gerenciado pelo Flyway nem é usado pela aplicação Spring.

---

## Acesso à aplicação

### Portal web (Thymeleaf)

| Rota             | Descrição                | Acesso                      |
|------------------|---------------------------|-------------------------------|
| `/login`         | Tela de login              | Público                       |
| `/portal/painel` | Painel autenticado          | Requer login, perfil VETERINARIO |

Usuário de teste (persistido via migration `V2__seed_perfis_e_veterinario.sql`):

| Usuário       | Senha       | Perfil        |
|---------------|-------------|----------------|
| `veterinario` | `vet123`    | `VETERINARIO`  |

### API REST

Base URL: `http://localhost:8080/api`

As rotas `/api/**` exigem autenticação **HTTP Basic** contra a tabela `USUARIO`, exceto `/api/ping`.
Um usuário TUTOR só enxerga e altera os próprios pets e consultas; um VETERINARIO tem acesso irrestrito
e é o único que pode emitir diagnóstico. Cadastro/edição/exclusão de pet é exclusivo de TUTOR — o dono
é sempre o usuário autenticado, nunca um `idTutor` enviado no corpo da requisição.

Usuário de teste (persistido via migration `V3__seed_tutor_inicial.sql`):

| Usuário  | Senha       | Perfil    |
|----------|-------------|------------|
| `tutor`  | `tutor123`  | `TUTOR`    |

| Método   | Rota                                | Acesso                    | Descrição                              |
|----------|--------------------------------------|-----------------------------|------------------------------------------|
| `GET`    | `/api/ping`                          | Público                    | Health-check simples (retorna `"pong"`)   |
| `GET`    | `/api/pets`                          | Autenticado                | Lista pets (TUTOR: só os próprios)        |
| `GET`    | `/api/pets/{id}`                     | Autenticado                | Busca um pet pelo ID                       |
| `POST`   | `/api/pets`                          | TUTOR                      | Cadastra um pet para o tutor autenticado   |
| `PUT`    | `/api/pets/{id}`                     | TUTOR                      | Atualiza um pet próprio                    |
| `DELETE` | `/api/pets/{id}`                     | TUTOR                      | Exclui um pet próprio                      |
| `GET`    | `/api/tutores`, `/api/tutores/{id}`  | Autenticado                | Lista/busca tutores                        |
| `POST`, `PUT`, `DELETE` | `/api/tutores/**`      | Autenticado                | CRUD de tutores                            |
| `GET`    | `/api/consultas`                     | Autenticado                | Lista consultas (TUTOR: só as próprias)    |
| `GET`    | `/api/consultas/{id}`                | Autenticado                | Busca uma consulta pelo ID                 |
| `POST`   | `/api/consultas`                     | Autenticado                | Agenda uma consulta (status inicial `AGENDADA`) |
| `PUT`    | `/api/consultas/{id}`                | Autenticado                | Atualiza pet/veterinário/data/tipo         |
| `DELETE` | `/api/consultas/{id}`                | Autenticado                | Exclui uma consulta                        |
| `PATCH`  | `/api/consultas/{id}/diagnostico`    | VETERINARIO                 | Encerra a consulta emitindo o diagnóstico  |

#### Exemplo — cadastrar pet

```http
POST /api/pets
Authorization: Basic dHV0b3I6dHV0b3IxMjM=
Content-Type: application/json

{
  "nome": "Thor",
  "especie": "Cachorro",
  "raca": "Labrador",
  "dataNascimento": "2022-03-15",
  "sexo": "M"
}
```

Resposta (`201 Created`):

```json
{
  "idPet": 11,
  "nome": "Thor",
  "especie": "Cachorro",
  "raca": "Labrador",
  "dataNascimento": "2022-03-15",
  "sexo": "M",
  "idTutor": 1,
  "nomeTutor": "Ana Beatriz Souza"
}
```

#### Exemplo — emitir diagnóstico de uma consulta

```http
PATCH /api/consultas/1/diagnostico
Content-Type: application/json

{
  "diagnostico": "Animal saudável, vacinação em dia"
}
```

Resposta (`200 OK`):

```json
{
  "idConsulta": 1,
  "idPet": 1,
  "nomePet": "Thor",
  "idVeterinario": 1,
  "nomeVeterinario": "Dra. Patricia Gomes",
  "dataConsulta": "2025-01-10",
  "tipoConsulta": "Checkup",
  "diagnostico": "Animal saudável, vacinação em dia",
  "status": "REALIZADA"
}
```

---

## Tratamento de erros da API

Erros de negócio, recursos não encontrados e falhas de validação retornam JSON padronizado em vez do erro 500 genérico do Spring:

```json
{
  "timestamp": "2026-08-13T21:10:00",
  "status": 404,
  "erro": "Not Found",
  "mensagem": "Pet com ID 999 não encontrado."
}
```

Erros de validação (`400 Bad Request`) incluem também o detalhe por campo:

```json
{
  "timestamp": "2026-08-13T21:10:00",
  "status": 400,
  "erro": "Bad Request",
  "mensagem": "Erro de validação nos campos informados.",
  "campos": {
    "nome": "O nome do pet é obrigatório."
  }
}
```

---

## Estrutura do projeto

```
kuravet/
├── mvnw, mvnw.cmd              # Maven Wrapper
├── pom.xml                     # Dependências e build
├── script_bd.sql                # Script SQL manual (equivalente às migrations)
└── src/main/
    ├── java/br/com/fiap/kuravet/
    │   ├── KuravetApplication.java
    │   ├── config/                # SecurityConfig, CorsConfig
    │   ├── controller/            # controller/api (REST) e controller/web (Thymeleaf)
    │   ├── dto/                   # Records de entrada/saída da API, por domínio (dto/pet, dto/tutor, dto/consulta)
    │   ├── exception/              # Exceções de negócio + handler global (exception/handler)
    │   ├── model/                  # Entidades JPA (Tutor, Pet, Veterinario, Consulta, Usuario...)
    │   ├── repository/             # Spring Data JPA repositories
    │   ├── security/                # UsuarioPrincipal, UsuarioDetailsService (autenticação via USUARIO)
    │   └── service/                 # Regras de negócio (ex.: ConsultaService)
    └── resources/
        ├── application.properties
        ├── db/migration/         # Scripts versionados do Flyway
        └── templates/            # login.html, painel.html
```

---

## Problemas comuns

- **`Failed to determine a suitable driver class`**: as credenciais/URL do banco em `application.properties` não foram carregadas corretamente — confira se o arquivo está salvo em UTF-8 e se `spring.datasource.*` está preenchido.
- **`Found non-empty schema(s) ... but no schema history table`**: o schema Oracle já tem tabelas criadas fora do Flyway (ex.: via `script_bd.sql` manual). Garanta que `spring.flyway.baseline-on-migrate=true` esteja habilitado, ou limpe o schema antes de rodar a aplicação pela primeira vez.
- **Timeout/erro de conexão com `oracle.fiap.com.br`**: verifique se você está conectado à rede/VPN da FIAP e se as credenciais do schema estão corretas.
- **`Validate failed: Migrations have failed validation` / `Migration checksum mismatch`**: seu schema já tinha o conjunto antigo de migrations (`V1__Criar_Tabelas_Core.sql` e demais) aplicado antes da reorganização para `V1__schema.sql`/`V2__seed_perfis_e_veterinario.sql`/`V3__seed_tutor_inicial.sql`. Resete o schema antes de rodar a aplicação de novo — conecte no seu usuário Oracle (ex. via SQL Developer) e rode:

  ```sql
  BEGIN EXECUTE IMMEDIATE 'DROP TABLE CONSULTA CASCADE CONSTRAINTS PURGE'; EXCEPTION WHEN OTHERS THEN NULL; END;
  /
  BEGIN EXECUTE IMMEDIATE 'DROP TABLE USUARIO CASCADE CONSTRAINTS PURGE'; EXCEPTION WHEN OTHERS THEN NULL; END;
  /
  BEGIN EXECUTE IMMEDIATE 'DROP TABLE PET CASCADE CONSTRAINTS PURGE'; EXCEPTION WHEN OTHERS THEN NULL; END;
  /
  BEGIN EXECUTE IMMEDIATE 'DROP TABLE VETERINARIO CASCADE CONSTRAINTS PURGE'; EXCEPTION WHEN OTHERS THEN NULL; END;
  /
  BEGIN EXECUTE IMMEDIATE 'DROP TABLE TUTOR CASCADE CONSTRAINTS PURGE'; EXCEPTION WHEN OTHERS THEN NULL; END;
  /
  BEGIN EXECUTE IMMEDIATE 'DROP TABLE FATO_PAGAMENTO CASCADE CONSTRAINTS PURGE'; EXCEPTION WHEN OTHERS THEN NULL; END;
  /
  BEGIN EXECUTE IMMEDIATE 'DROP TABLE AUDITORIA_LOG CASCADE CONSTRAINTS PURGE'; EXCEPTION WHEN OTHERS THEN NULL; END;
  /
  BEGIN EXECUTE IMMEDIATE 'DROP TRIGGER TRG_AUDITORIA_CONSULTA'; EXCEPTION WHEN OTHERS THEN NULL; END;
  /
  BEGIN EXECUTE IMMEDIATE 'DROP SEQUENCE SEQ_TUTOR'; EXCEPTION WHEN OTHERS THEN NULL; END;
  /
  BEGIN EXECUTE IMMEDIATE 'DROP SEQUENCE SEQ_VETERINARIO'; EXCEPTION WHEN OTHERS THEN NULL; END;
  /
  BEGIN EXECUTE IMMEDIATE 'DROP SEQUENCE SEQ_PET'; EXCEPTION WHEN OTHERS THEN NULL; END;
  /
  BEGIN EXECUTE IMMEDIATE 'DROP SEQUENCE SEQ_CONSULTA'; EXCEPTION WHEN OTHERS THEN NULL; END;
  /
  BEGIN EXECUTE IMMEDIATE 'DROP TABLE FLYWAY_SCHEMA_HISTORY CASCADE CONSTRAINTS PURGE'; EXCEPTION WHEN OTHERS THEN NULL; END;
  /
  ```

  Depois disso, o próximo `spring-boot:run` recria tudo do zero a partir de `V1__schema.sql`.