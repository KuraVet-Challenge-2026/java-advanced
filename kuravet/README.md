# 🐾 KuraVet

Backend da plataforma de teleconsulta veterinária **KuraVet**, desenvolvido em **Spring Boot** para o Challenge FIAP.

A aplicação atende dois públicos a partir do mesmo núcleo de regras de negócio:

- **Portal web da clínica** (`/portal/**`) — camada de visualização em **Thymeleaf**, restrita ao perfil `VETERINARIO`. É onde o veterinário aprova ou recusa solicitações de teleconsulta, emite diagnósticos e gerencia o cadastro de pets.
- **API REST** (`/api/**`) — consumida pelo aplicativo mobile do tutor (React Native), autenticada via HTTP Basic. Um `TUTOR` só enxerga e altera os próprios pets, consultas e o próprio cadastro de tutor.
  Nenhuma regra de negócio é duplicada entre os dois canais: ambos chamam os mesmos *services*.

---

## Sumário

- [Stack utilizada](#stack-utilizada)
- [Fluxos de negócio](#fluxos-de-negócio)
- [Pré-requisitos](#pré-requisitos)
- [Configuração](#configuração)
- [Instalação e execução](#instalação-e-execução)
- [Migrações do banco (Flyway)](#migrações-do-banco-flyway)
- [Segurança e perfis de acesso](#segurança-e-perfis-de-acesso)
- [Portal web](#portal-web)
- [API REST](#api-rest)
- [Tratamento de erros](#tratamento-de-erros)
- [Triagem por IA — regras](#triagem-por-ia--regras)
- [Estrutura do projeto](#estrutura-do-projeto)
- [Pipeline CI](#pipeline-ci)
- [Problemas comuns](#problemas-comuns)
---

## Stack utilizada

| Camada         | Tecnologia                                        |
|----------------|---------------------------------------------------|
| Linguagem      | Java 21                                           |
| Framework      | Spring Boot 4.1.0                                 |
| Visualização   | Thymeleaf + thymeleaf-extras-springsecurity6      |
| Persistência   | Spring Data JPA + Hibernate                       |
| Banco de dados | Oracle Database (driver `ojdbc11`)                |
| Migrações      | Flyway (`flyway-database-oracle`)                 |
| Segurança      | Spring Security (form login + HTTP Basic stateless) |
| Validação      | Jakarta Bean Validation                           |
| Build          | Maven (via Maven Wrapper)                         |
| Utilitários    | Lombok                                            |
 
---

## Fluxos de negócio

A aplicação implementa dois fluxos funcionais completos, além do CRUD de apoio. Ambos vivem em `ConsultaService`, que é o **único ponto do sistema autorizado a alterar o status de uma consulta**.

### Máquina de estados da consulta

```
SOLICITADA ──aprovar───▶ AGENDADA ──diagnóstico──▶ REALIZADA
     │    ╲                  │
   recusar  ╲cancelar      cancelar
     ▼        ╲               ▼
  RECUSADA      ╲────────▶ CANCELADA
```

`REALIZADA`, `RECUSADA` e `CANCELADA` são estados terminais. Qualquer transição fora deste mapa é rejeitada com `400 Bad Request` e a mensagem `Transicao de status invalida: X -> Y`.

**Cancelamento tem regra própria por perfil** (`ConsultaService.podeCancelar`, único ponto de decisão — reaproveitado pelo portal para exibir ou não o botão):
- **TUTOR** cancela `SOLICITADA` ou `AGENDADA`, e só as **próprias** consultas (ownership de `buscarPorId`; consulta de outro tutor responde `404`).
- **VETERINARIO** cancela apenas `AGENDADA`. Uma solicitação ainda pendente (`SOLICITADA`) deve ser **recusada com motivo**, nunca cancelada — a tentativa responde `400` com `Solicitacoes pendentes devem ser recusadas com um motivo.`.

### Fluxo 1 — Aprovação de teleconsulta

1. O tutor solicita a teleconsulta pelo app (`POST /api/consultas/solicitacoes`). A consulta nasce como `SOLICITADA`.
2. Regras aplicadas na solicitação: o pet precisa pertencer ao tutor autenticado, o veterinário precisa existir, a data precisa ser futura, e o pet não pode ter outra consulta ativa na mesma data.
3. O veterinário vê o pedido na fila do portal (`/portal/solicitacoes`) e decide:
    - **Aprovar** → `AGENDADA`;
    - **Recusar** → `RECUSADA`, com justificativa obrigatória entre 10 e 300 caracteres, visível ao tutor.
### Fluxo 2 — Realização e emissão de diagnóstico

O veterinário abre uma consulta `AGENDADA` no portal e registra o diagnóstico (entre 15 e 400 caracteres). A consulta passa a `REALIZADA` e não pode mais ser alterada. Uma consulta ainda `SOLICITADA` ou já `RECUSADA` nunca vira atendimento.

### Exclusão física de consultas

`DELETE /api/consultas/{id}` exige perfil `VETERINARIO` (um `TUTOR` recebe `403`) e nunca apaga uma consulta `REALIZADA` — ela faz parte do histórico clínico e a tentativa responde `400` com `Consultas realizadas fazem parte do historico clinico e nao podem ser excluidas.`. Para os demais estados, a exclusão é física e definitiva; quem precisa encerrar o fluxo sem apagar histórico deve usar o cancelamento.

### Regras espelhadas no schema

As regras não vivem apenas no Java. O banco garante as mesmas invariantes por *CHECK constraints*, de modo que nem um `INSERT` manual pelo SQL Developer consegue violá-las:

| Constraint | Garante |
|---|---|
| `KV_CK_CONS_STATUS` | status ∈ {SOLICITADA, AGENDADA, REALIZADA, CANCELADA, RECUSADA} |
| `KV_CK_CONS_DIAGNOSTICO` | diagnóstico existe **se e somente se** o status é `REALIZADA` |
| `KV_CK_CONS_MOTIVO` | motivo de recusa existe **se e somente se** o status é `RECUSADA` |
| `KV_CK_USU_PERFIL` | perfil ∈ {TUTOR, VETERINARIO} |
| `KV_CK_USU_TUTOR` | `ID_TUTOR` é obrigatório quando o perfil é TUTOR e proibido quando é VETERINARIO |
| `KV_CK_PET_SEXO` | sexo ∈ {M, F} |
| `KV_UQ_PET_CODIGO` | `CODIGO_IDENTIFICACAO` é único quando preenchido (campo opcional — aceita múltiplos `NULL`) |
 
---

## Pré-requisitos

- **JDK 21** ou superior no `PATH` (`java -version`).
- Maven **não** precisa ser instalado — o projeto inclui o Maven Wrapper (`mvnw` / `mvnw.cmd`).
- Acesso de rede ao Oracle da instituição (`oracle.fiap.com.br:1521`), incluindo VPN quando exigido.
- Credenciais de um schema Oracle válido.
---

## Configuração

As credenciais **não são versionadas**. O arquivo `src/main/resources/application.properties` traz apenas placeholders e importa, se existir, um arquivo local fora do controle de versão:

```properties
spring.config.import=optional:classpath:application-local.properties
 
spring.datasource.url=${DB_URL:jdbc:oracle:thin:@oracle.fiap.com.br:1521:ORCL}
spring.datasource.username=${DB_USERNAME:}
spring.datasource.password=${DB_PASSWORD:}
```

Crie `src/main/resources/application-local.properties` com as suas credenciais:

```properties
spring.datasource.username=SEU_RM
spring.datasource.password=SUA_SENHA
```

Esse arquivo está no `.gitignore` e nunca vai para o repositório.

Alternativamente, defina as variáveis de ambiente `DB_USERNAME` e `DB_PASSWORD`, ou passe por linha de comando:

```bash
./mvnw spring-boot:run -Dspring-boot.run.arguments="--spring.datasource.username=SEU_RM --spring.datasource.password=SUA_SENHA"
```

> ⚠️ O `application.properties` deve estar salvo em **UTF-8**. Em outra codificação, o Maven falha ao copiar o arquivo para `target/classes` (`MalformedInputException`) e a aplicação sobe com as propriedades vazias.
 
---

## Instalação e execução

```bash
git clone https://github.com/KuraVet-Challenge-2026/java-advanced.git
cd java-advanced/kuravet
```

### Linha de comando

**Windows (PowerShell):**
```powershell
.\mvnw.cmd clean spring-boot:run
```

**Linux / macOS:**
```bash
./mvnw clean spring-boot:run
```

### Pela IDE

Abra a pasta **`kuravet`** (a que contém o `pom.xml`) como projeto Maven — não a pasta raiz do repositório. Configure as credenciais e execute `br.com.fiap.kuravet.KuravetApplication`.

Ao subir com sucesso:

```
Successfully applied N migrations
Tomcat initialized with port 8080 (http)
Started KuravetApplication in X seconds
```

A aplicação fica em **http://localhost:8080**.

### Para o time do app mobile

Para apontar o app para a API local, siga exatamente os passos de [Configuração](#configuração) e [Instalação e execução](#instalação-e-execução) acima (credenciais em `application-local.properties`, depois `./mvnw spring-boot:run` ou `.\mvnw.cmd spring-boot:run`). Nada de específico para mobile muda nisso — a API sobe do mesmo jeito para portal e app.

Os três pontos que o app precisa (ver [API REST](#api-rest) para os contratos completos):

- `POST /api/auth/cadastro` — autocadastro do tutor (público, sem HTTP Basic).
- `GET /api/auth/me` — validar login e obter `perfil`/`idTutor` depois de autenticar com HTTP Basic.
- `GET/POST/PUT/DELETE /api/tutores/**` — agora com ownership: um TUTOR só vê e altera o próprio registro (404 para qualquer outro ID).
- `GET /api/veterinarios` — lista para popular a escolha de profissional em `POST /api/consultas/solicitacoes`.
 
---

## Migrações do banco (Flyway)

O schema é criado e versionado automaticamente na inicialização, a partir de `src/main/resources/db/migration/`:

| Script | Conteúdo |
|---|---|
| `V1__schema.sql` | DDL das tabelas TUTOR, VETERINARIO, PET, CONSULTA, USUARIO, sequences e constraints |
| `V2__seed_perfis_e_veterinario.sql` | 10 veterinários e o usuário de perfil VETERINARIO |
| `V3__seed_tutor_inicial.sql` | Tutor inicial e seu usuário de perfil TUTOR |
| `V4__consulta_teleconsulta.sql` | Amplia a máquina de estados: status `SOLICITADA` e `RECUSADA`, colunas `DATA_SOLICITACAO` e `MOTIVO_RECUSA`, novas CHECK constraints |
| `V5__seed_pets_e_consultas.sql` | Pets e consultas cobrindo todos os estados do fluxo |
| `V6__pet_codigo_identificacao.sql` | Coluna opcional `CODIGO_IDENTIFICACAO` em PET (código da Petz, microchip etc.), única quando preenchida |
| `V7__triagem.sql` | Tabela TRIAGEM (relato, sintomas, urgência, recomendação, regras aplicadas, método de extração), `SEQ_TRIAGEM` e índice por pet |

Nenhum script precisa ser executado à mão: basta iniciar a aplicação apontando para um schema Oracle válido.

O Hibernate roda com `spring.jpa.hibernate.ddl-auto=validate` — o schema é propriedade do Flyway, e o Hibernate apenas confere se as entidades batem com o que foi criado.

### Regra de ouro

**Nunca edite uma migration já aplicada.** O Flyway compara o checksum do arquivo com o que foi executado e se recusa a continuar se houver diferença. Precisa mudar algo? Crie um `V6__`, `V7__` e assim por diante.

A aplicação registra um `FlywayMigrationStrategy` que executa `repair()` antes de `migrate()`, de modo que um registro de migration interrompida não impede a inicialização seguinte.

### Script SQL independente

O arquivo `script_bd.sql`, na raiz do módulo, contém procedures, functions e a trigger de auditoria da disciplina de banco de dados. Ele é executado manualmente em um cliente Oracle e **não** faz parte do schema gerenciado pelo Flyway.
 
---

## Segurança e perfis de acesso

Duas cadeias de filtros isoladas, declaradas em `SecurityConfig`:

| Cadeia | Rotas | Autenticação | Sessão | CSRF |
|---|---|---|---|---|
| API mobile | `/api/**` | HTTP Basic | Stateless | Desabilitado |
| Portal web | demais rotas | Form login | Sessão HTTP | Habilitado |

A autenticação consulta a tabela `USUARIO` através de `UsuarioDetailsService`, com senhas em **BCrypt**. A autorização por rota e método fica no `SecurityConfig`; a autorização **por dono** — um tutor só acessa os próprios registros — depende dos dados e por isso vive na camada de service.

Um pet, consulta ou **tutor** de outro dono retorna `404`, não `403`, para não revelar a existência de registros de terceiros (`PetService`, `ConsultaService` e `TutorService` seguem o mesmo padrão: o dono vem sempre do `UsuarioPrincipal` autenticado, nunca do `id` da URL).

Toda falha de autenticação em `/api/**` responde `401` puro (formato padrão do Spring Security, com header `WWW-Authenticate`) — a cadeia da API declara seu próprio `AuthenticationEntryPoint` e libera `/error` na cadeia do portal, para o forward interno do Tomcat em um `401`/`403` não cair no `formLogin()` e virar redirect para `/login`.

### Cadastro público (app mobile)

`POST /api/auth/cadastro` é a única rota de escrita liberada sem autenticação em `/api/**`. Ela sempre cria um usuário de perfil `TUTOR` — o papel nunca vem do corpo da requisição, é fixado no `AuthService`. Um tutor cadastrado por ali só enxerga e altera o próprio registro nas demais rotas de `/api/tutores/**`.

### Usuários de teste

| Usuário | Senha | Perfil | Acesso |
|---|---|---|---|
| `veterinario` | `vet123` | VETERINARIO | Portal web e API |
| `tutor` | `tutor123` | TUTOR | Apenas API (app mobile) |

Novos tutores se cadastram via `POST /api/auth/cadastro` (ver [API REST](#api-rest)) em vez de receber usuário/senha por seed.
 
---

## Portal web

Interface Thymeleaf da clínica. Todas as rotas sob `/portal/**` exigem perfil `VETERINARIO`.

| Rota | Descrição |
|---|---|
| `GET /login` | Tela de login (pública) |
| `GET /portal/painel` | Indicadores por status e resumo do fluxo |
| `GET /portal/solicitacoes` | Fila de teleconsultas aguardando decisão |
| `POST /portal/solicitacoes/{id}/aprovar` | Aprova a solicitação |
| `GET/POST /portal/solicitacoes/{id}/recusar` | Formulário e gravação da recusa |
| `GET /portal/consultas` | Histórico completo, filtrável por status |
| `GET /portal/consultas/{id}` | Detalhe da consulta |
| `POST /portal/consultas/{id}/diagnostico` | Emite o diagnóstico e encerra |
| `GET /portal/pets` | Lista de pets |
| `GET/POST /portal/pets/novo` | Cadastro de pet |
| `GET/POST /portal/pets/{id}/editar` | Edição de pet |
| `POST /portal/pets/{id}/excluir` | Exclusão de pet |

Os formulários usam `@Valid` com `BindingResult`: quando a validação falha, a página é reexibida com a mensagem sob o campo e o restante do formulário preenchido. O token CSRF é injetado automaticamente pelo Thymeleaf em todo `<form th:action>` com `method="post"`.
 
---

## API REST

Base: `http://localhost:8080/api`. Todas as rotas exigem HTTP Basic, exceto `/api/ping` e `POST /api/auth/cadastro`.

| Método | Rota | Perfil | Descrição |
|---|---|---|---|
| `GET` | `/api/ping` | público | Health-check (retorna `pong`) |
| `POST` | `/api/auth/cadastro` | público | Autocadastro: cria TUTOR + USUARIO (perfil sempre `TUTOR`) |
| `GET` | `/api/auth/me` | autenticado | Perfil de quem está logado (valida a senha digitada no app) |
| `GET` | `/api/pets` | autenticado | Lista pets (TUTOR: só os próprios) |
| `GET` | `/api/pets/{id}` | autenticado | Busca pet por ID |
| `POST` | `/api/pets` | TUTOR | Cadastra pet para o tutor autenticado |
| `PUT` | `/api/pets/{id}` | TUTOR | Atualiza pet próprio |
| `DELETE` | `/api/pets/{id}` | TUTOR | Exclui pet próprio (bloqueado se houver consultas) |
| `GET` | `/api/tutores`, `/api/tutores/{id}` | autenticado | Lista e busca tutores (TUTOR: só o próprio; VETERINARIO: todos) |
| `POST` | `/api/tutores` | autenticado | Cadastro direto de tutor (sem login associado — uso administrativo; o app mobile usa `/api/auth/cadastro`) |
| `PUT`/`DELETE` | `/api/tutores/{id}` | autenticado | Atualiza/exclui tutor (TUTOR: só o próprio; VETERINARIO: qualquer um) |
| `GET` | `/api/veterinarios`, `/api/veterinarios/{id}` | autenticado | Lista e busca veterinários (somente leitura) |
| `GET` | `/api/consultas?status=` | autenticado | Lista consultas, opcionalmente por status |
| `GET` | `/api/consultas/{id}` | autenticado | Busca consulta por ID |
| `POST` | `/api/consultas/solicitacoes` | TUTOR | Solicita teleconsulta |
| `PATCH` | `/api/consultas/{id}/aprovacao` | VETERINARIO | Aprova a solicitação |
| `PATCH` | `/api/consultas/{id}/recusa` | VETERINARIO | Recusa com justificativa |
| `PATCH` | `/api/consultas/{id}/diagnostico` | VETERINARIO | Emite diagnóstico e encerra |
| `PATCH` | `/api/consultas/{id}/cancelamento` | autenticado | Cancela: TUTOR só a própria `SOLICITADA`/`AGENDADA`; VETERINARIO só `AGENDADA` |
| `PUT` | `/api/consultas/{id}` | autenticado | Atualiza pet, veterinário, data ou tipo |
| `DELETE` | `/api/consultas/{id}` | VETERINARIO | Exclui consulta (bloqueado para `REALIZADA`, que fica no histórico clínico) |
| `POST` | `/api/triagens` | TUTOR | Faz a triagem de um pet próprio a partir do relato livre e grava o resultado |
| `GET` | `/api/triagens` | autenticado | Lista triagens das mais urgentes para as menos (TUTOR: só as dos próprios pets; VETERINARIO: todas) |
| `GET` | `/api/triagens/{id}` | autenticado | Busca triagem por ID (TUTOR: só dos próprios pets) |

### Exemplo — autocadastro do tutor (app mobile)

```http
POST /api/auth/cadastro
Content-Type: application/json

{
  "nome": "Carlos Eduardo Lima",
  "cpf": "999.888.777-66",
  "telefone": "(11) 90000-0001",
  "email": "carlos.lima@email.com",
  "endereco": "Rua Nova, 50",
  "username": "carlos_novo",
  "senha": "senha123"
}
```

Resposta `201 Created` (senha nunca volta na resposta):

```json
{
  "idTutor": 11,
  "nome": "Carlos Eduardo Lima",
  "cpf": "999.888.777-66",
  "telefone": "(11) 90000-0001",
  "email": "carlos.lima@email.com",
  "endereco": "Rua Nova, 50",
  "dataCadastro": "2026-09-03",
  "username": "carlos_novo",
  "perfil": "TUTOR"
}
```

`400 Bad Request` se `username` ou `cpf` já existirem (`{"mensagem":"Nome de usuario ja em uso."}`) ou se os campos obrigatórios faltarem (formato `campos` padrão do `ApiExceptionHandler`).

### Exemplo — validar login / obter o perfil autenticado

```http
GET /api/auth/me
Authorization: Basic Y2FybG9zX25vdm86c2VuaGExMjM=
```

Resposta `200 OK`:

```json
{
  "idUsuario": 100,
  "username": "carlos_novo",
  "perfil": "TUTOR",
  "idTutor": 11,
  "nomeTutor": "Carlos Eduardo Lima"
}
```

`idTutor`/`nomeTutor` vêm `null` quando `perfil` é `VETERINARIO`. Credencial ausente ou inválida responde `401` puro — é esse status que o app usa para decidir se o login falhou.

### Exemplo — cadastrar pet (app mobile)

`codigoIdentificacao` é **opcional e aditivo**: requisições antigas, sem o campo, continuam funcionando normalmente.

```http
POST /api/pets
Authorization: Basic dHV0b3I6dHV0b3IxMjM=
Content-Type: application/json

{
  "nome": "Bidu",
  "especie": "Cachorro",
  "raca": "Vira-lata",
  "dataNascimento": "2022-01-01",
  "sexo": "M",
  "codigoIdentificacao": " pz-123 "
}
```

Resposta `201 Created` (o código é normalizado — espaços nas pontas removidos e letras maiúsculas):

```json
{
  "idPet": 12,
  "nome": "Bidu",
  "especie": "Cachorro",
  "raca": "Vira-lata",
  "dataNascimento": "2022-01-01",
  "sexo": "M",
  "idTutor": 1,
  "nomeTutor": "Ana Paula Ferreira",
  "codigoIdentificacao": "PZ-123"
}
```

Se `codigoIdentificacao` já estiver em uso por outro pet, a resposta é `400 Bad Request`:

```json
{
  "mensagem": "Ja existe um pet cadastrado com o codigo de identificacao PZ-123."
}
```

### Exemplo — solicitar teleconsulta

```http
POST /api/consultas/solicitacoes
Authorization: Basic dHV0b3I6dHV0b3IxMjM=
Content-Type: application/json
 
{
  "idPet": 1,
  "idVeterinario": 2,
  "dataConsulta": "2026-10-01",
  "tipoConsulta": "Teleconsulta - Dermatologia"
}
```

Resposta `201 Created`:

```json
{
  "idConsulta": 12,
  "idPet": 1,
  "nomePet": "Thor",
  "idVeterinario": 2,
  "nomeVeterinario": "Dr. Rafael Andrade",
  "dataSolicitacao": "2026-08-27",
  "dataConsulta": "2026-10-01",
  "tipoConsulta": "Teleconsulta - Dermatologia",
  "diagnostico": null,
  "motivoRecusa": null,
  "status": "SOLICITADA"
}
```

### Exemplo — recusar solicitação

```http
PATCH /api/consultas/12/recusa
Authorization: Basic dmV0ZXJpbmFyaW86dmV0MTIz
Content-Type: application/json
 
{
  "motivo": "Caso ortopedico exige exame presencial com raio-x."
}
```

### Exemplo — triagem do pet (app mobile)

O tutor descreve os sintomas em texto livre. A API extrai os sintomas, aplica o [motor de regras](#triagem-por-ia--regras) com a espécie e a idade do pet e grava o resultado. O `relato` é obrigatório e deve ter entre 10 e 1000 caracteres; um pet de outro tutor responde `404`.

```http
POST /api/triagens
Authorization: Basic dHV0b3I6dHV0b3IxMjM=
Content-Type: application/json
 
{
  "idPet": 2,
  "relato": "Minha gata não faz xixi desde ontem e está apática"
}
```

Resposta `201 Created` (header `Location: /api/triagens/1`):

```json
{
  "idTriagem": 1,
  "idPet": 2,
  "nomePet": "Luna",
  "urgencia": "EMERGENCIA",
  "recomendacao": "Procure atendimento presencial imediato em um pronto-socorro veterinário. Esta triagem é orientativa e não substitui a avaliação do veterinário.",
  "sintomas": ["Apatia", "Não urina"],
  "regrasAplicadas": [
    "R2 - gato que não urina (risco de obstrução urinária)",
    "R7 - sintoma que exige acompanhamento veterinário"
  ],
  "metodoExtracao": "PALAVRAS_CHAVE",
  "dataTriagem": "2026-10-15T21:40:12.345",
  "sugereTeleconsultaPrioritaria": true
}
```

`GET /api/triagens` devolve a mesma estrutura em lista, ordenada por urgência (`EMERGENCIA` → `BAIXA`) e, dentro do mesmo nível, da mais recente para a mais antiga. O relato do tutor é gravado, mas nunca aparece nos logs.
 
---

## Tratamento de erros

Há dois `ControllerAdvice` com escopos distintos, para que cada canal receba a resposta no formato certo:

- `ApiExceptionHandler` (`@RestControllerAdvice` limitado ao pacote `controller.api`) → JSON padronizado;
- `PortalExceptionHandler` (`@ControllerAdvice` limitado a `controller.web`) → página HTML de erro.
  Erro de negócio ou recurso inexistente na API:

```json
{
  "timestamp": "2026-08-27T21:10:00",
  "status": 404,
  "erro": "Not Found",
  "mensagem": "Pet com ID 999 não encontrado."
}
```

Erro de validação inclui o detalhe por campo:

```json
{
  "timestamp": "2026-08-27T21:10:00",
  "status": 400,
  "erro": "Bad Request",
  "mensagem": "Erro de validação nos campos informados.",
  "campos": {
    "dataConsulta": "A data desejada precisa ser futura."
  }
}
```
 
---

## Triagem por IA — regras

A triagem separa duas responsabilidades:

- **A LLM só extrai sintomas**: lê o relato livre do tutor e devolve um `SintomasExtraidos` (conjunto de `Sintoma` + duração em dias).
- **O `MotorRegrasTriagem` decide a urgência**: aplica regras determinísticas e auditáveis ao que foi extraído e ao perfil do pet (`PerfilPet`: espécie e idade). A LLM nunca classifica a urgência.

O código fica em `br.com.fiap.kuravet.ia`. O motor percorre uma lista de `RegraTriagem` (código, descrição, urgência e condição) e devolve um `ResultadoTriagem` com a urgência, a recomendação e as regras que justificam a decisão.

Urgências, da menor para a maior: `BAIXA` < `MODERADA` < `ALTA` < `EMERGENCIA`. Filhote = menos de 1 ano; idoso = 10 anos ou mais.

| Regra | Condição | Urgência |
|-------|----------|----------|
| R1 | Dificuldade para respirar, convulsão, sangramento, ingestão tóxica ou desmaio | EMERGENCIA |
| R2 | Gato que não urina (risco de obstrução urinária) | EMERGENCIA |
| R3 | Não urina, em outras espécies | ALTA |
| R4 | Vômito e diarreia juntos | ALTA |
| R5 | Filhote ou idoso com vômito ou diarreia | ALTA |
| R6 | Febre com apatia | ALTA |
| R7 | Vômito, diarreia, falta de apetite, apatia, febre, tosse, ferida ou mancando | MODERADA |
| R8 | Coceira ou espirros | BAIXA |
| R9 | *Escalonamento:* resultado MODERADA com sintomas há 3 dias ou mais | MODERADA → ALTA |
| R10 | Nenhuma regra disparou (nenhum sintoma reconhecido) | BAIXA |

**Como a decisão é tomada:**

1. Todas as regras R1–R8 são avaliadas; a urgência final é a **maior** entre as que dispararam.
2. Se a urgência final for `MODERADA` e a duração for de 3 dias ou mais, ela sobe para `ALTA` (R9).
3. Se nenhuma regra disparar, o resultado é `BAIXA` com a R10.
4. `regrasAplicadas` lista todas as regras disparadas no formato `"R4 - vômito e diarreia juntos"`.

**Recomendação por nível:**

| Urgência | Recomendação |
|----------|--------------|
| EMERGENCIA | Procure atendimento presencial imediato em um pronto-socorro veterinário. |
| ALTA | Solicite uma teleconsulta prioritária ainda hoje. |
| MODERADA | Agende uma teleconsulta nas próximas 48 horas e observe a evolução. |
| BAIXA | Observe por 48 horas; se surgirem novos sintomas, refaça a triagem. |
| BAIXA (R10) | Descreva melhor os sintomas ou agende uma teleconsulta de rotina. |

Toda recomendação termina com: *"Esta triagem é orientativa e não substitui a avaliação do veterinário."*

**Exemplos:**

| Sintomas | Pet | Duração | Resultado |
|----------|-----|---------|-----------|
| Tosse | cão, 4 anos | 1 dia | MODERADA (R7) |
| Tosse | cão, 4 anos | 5 dias | ALTA (R7 + R9) |
| Não urina | gato | — | EMERGENCIA (R2) |
| Não urina | cão | — | ALTA (R3) |
| Vômito | filhote | — | ALTA (R5 + R7) |
| Coceira + convulsão | cão | — | EMERGENCIA (R1 + R8) |
| (nenhum) | qualquer | — | BAIXA (R10) |

Os cenários estão cobertos em `MotorRegrasTriagemTest` (JUnit 5 puro, sem Spring e sem banco).
 
---

## Estrutura do projeto

```
kuravet/
├── pom.xml
├── script_bd.sql                    # Script SQL manual (disciplina de banco)
└── src/main/
    ├── java/br/com/fiap/kuravet/
    │   ├── KuravetApplication.java
    │   ├── config/                  # SecurityConfig, CorsConfig, FlywayConfig
    │   ├── controller/
    │   │   ├── api/                 # Endpoints REST do app mobile
    │   │   └── web/                 # Controllers do portal Thymeleaf
    │   │       └── form/            # Objetos de vínculo dos formulários
    │   ├── dto/                     # Records de entrada e saída da API
    │   ├── enums/                   # Perfil, StatusConsulta
    │   ├── ia/                      # Motor de regras da triagem
    │   ├── exception/               # Exceções de negócio
    │   │   └── handler/             # Tratamento global por canal
    │   ├── model/                   # Entidades JPA
    │   ├── repository/              # Spring Data JPA
    │   ├── security/                # UsuarioPrincipal, UsuarioDetailsService
    │   └── service/                 # Regras de negócio
    └── resources/
        ├── application.properties
        ├── db/migration/            # Scripts versionados do Flyway
        ├── static/css/              # Folha de estilo do portal
        └── templates/               # Views Thymeleaf
```
 
---

## Pipeline CI

O pipeline de integração contínua roda no **Azure DevOps** e está definido em [`azure-pipelines.yml`](../azure-pipelines.yml), na raiz do repositório.

- **O que dispara:** todo push na branch `master` e todo pull request direcionado a ela.
- **O que roda:** em um agente `ubuntu-latest` com JDK 21, executa `./mvnw -B clean verify` (compilação + testes, sem necessidade de Oracle), com cache do repositório Maven. Os relatórios JUnit do Surefire são publicados na aba **Tests** da execução, e testes com falha reprovam o pipeline.
- **Artefato:** o jar executável do Spring Boot é publicado como `kuravet.jar` no artefato de pipeline **`kuravet-jar`**.

O deploy (CD) não faz parte deste pipeline por enquanto.
 
---

## Problemas comuns

**`ORA-12541: não há listener em host localhost port 1521`**
A URL do datasource não foi carregada e caiu no padrão. Confirme que o `application-local.properties` existe e que o `application.properties` está em UTF-8.

**`ORA-01017: invalid username/password`**
Credenciais incorretas em `application-local.properties`.

**`Migration checksum mismatch`**
Uma migration já aplicada foi editada. Reverta o arquivo ou crie uma nova versão — nunca altere uma migration aplicada.

**`Detected failed migration to version N`**
Uma migration falhou pela metade e o registro ficou no histórico. O `FlywayConfig` resolve automaticamente na próxima inicialização; se persistir, remova a linha correspondente de `FLYWAY_SCHEMA_HISTORY` (atenção: as colunas dessa tabela são minúsculas e exigem aspas duplas no Oracle, ex. `WHERE "version" = 'N'`).

**`Found non-empty schema(s) but no schema history table`**
O schema tem tabelas criadas fora do Flyway. Mantenha `spring.flyway.baseline-on-migrate=true` ou limpe o schema antes da primeira execução.

**Timeout ao conectar em `oracle.fiap.com.br`**
Verifique a conexão com a rede/VPN da FIAP.

**A IDE não reconhece o projeto / botão de execução ausente**
Abra a pasta `kuravet` (a que contém o `pom.xml`), não a raiz do repositório.

LINK YOUTUBE: https://www.youtube.com/watch?v=up5OcE8ulrA