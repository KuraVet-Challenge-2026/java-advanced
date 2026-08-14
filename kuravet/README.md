# 🐾 KuraVet

Plataforma de saúde preventiva contínua para pets. Backend em **Spring Boot** que expõe:

- um **portal web** (Thymeleaf + Spring Security) para tutores e veterinários autenticados;
- uma **API REST** (`/api/**`) consumida por um app mobile (React Native), sem autenticação nesta sprint.

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
├── V1__Criar_Tabelas_Core.sql
├── V2__Carga_Dados_Iniciais.sql
├── V3__Procedures_Functions_Trigger.sql
└── V4__Sequences_Pk.sql
```

Não é necessário rodar nenhum script manualmente — basta iniciar a aplicação com um schema Oracle válido e vazio (ou já controlado pelo Flyway) que as tabelas, dados iniciais, procedures/functions/trigger e sequences são criados na primeira execução.

O arquivo `script_bd.sql`, na raiz do módulo, é um script auxiliar equivalente (para execução manual direta em um cliente Oracle, ex. SQL Developer), útil para inspecionar/recriar o schema fora do fluxo do Spring Boot.

---

## Acesso à aplicação

### Portal web (Thymeleaf)

| Rota       | Descrição                                   | Acesso         |
|------------|----------------------------------------------|----------------|
| `/login`   | Tela de login                                 | Público        |
| `/painel`  | Painel autenticado                            | Requer login   |

Usuários de teste (em memória, definidos em `SecurityConfig`):

| Usuário       | Senha       | Papel (role)   |
|---------------|-------------|----------------|
| `veterinario` | `vet123`    | `VETERINARIO`  |
| `tutor`       | `tutor123`  | `TUTOR`        |

> Esses usuários são fixos em memória apenas para fins de desenvolvimento e serão substituídos por uma origem persistente (banco) em sprints futuras.

### API REST

Base URL: `http://localhost:8080/api`

As rotas `/api/**` **não exigem autenticação** nesta sprint (pensadas para consumo pelo app mobile).

| Método   | Rota                                | Descrição                                              |
|----------|--------------------------------------|----------------------------------------------------------|
| `GET`    | `/api/ping`                          | Health-check simples (retorna `"pong"`)                  |
| `GET`    | `/api/pets`                          | Lista todos os pets                                       |
| `GET`    | `/api/pets/{id}`                     | Busca um pet pelo ID                                       |
| `POST`   | `/api/pets`                          | Cadastra um novo pet                                       |
| `PUT`    | `/api/pets/{id}`                     | Atualiza um pet existente                                  |
| `DELETE` | `/api/pets/{id}`                     | Exclui um pet                                              |
| `PATCH`  | `/api/consultas/{id}/diagnostico`    | Encerra a consulta emitindo o diagnóstico                  |

#### Exemplo — cadastrar pet

```http
POST /api/pets
Content-Type: application/json

{
  "nome": "Thor",
  "especie": "Cachorro",
  "raca": "Labrador",
  "dataNascimento": "2022-03-15",
  "sexo": "M",
  "idTutor": 1
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
    │   ├── config/              # SecurityConfig
    │   ├── controller/          # Web (Thymeleaf) e REST (/api/**)
    │   ├── dto/                 # Records de entrada/saída da API
    │   ├── exception/            # Exceções de negócio + handler global
    │   ├── model/                # Entidades JPA (Tutor, Pet, Veterinario, Consulta...)
    │   ├── repository/           # Spring Data JPA repositories
    │   └── service/              # Regras de negócio (ex.: ConsultaService)
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