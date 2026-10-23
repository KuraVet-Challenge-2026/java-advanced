# 🐾 KuraVet

Plataforma de teleconsulta e triagem veterinária da **Clyvo Vet**: o tutor usa o aplicativo mobile, o veterinário usa o portal web, e os dois conversam com o mesmo backend Spring Boot, publicado no Azure por um pipeline de CI/CD.

[![Aplicação online](https://img.shields.io/badge/aplica%C3%A7%C3%A3o-online-0078D4)](https://kuravet-rm562396.azurewebsites.net/login)
[![Health](https://img.shields.io/badge/health-%2Factuator%2Fhealth-2EA44F)](https://kuravet-rm562396.azurewebsites.net/actuator/health)
[![Repositório](https://img.shields.io/badge/GitHub-java--advanced-181717)](https://github.com/KuraVet-Challenge-2026/java-advanced)

## Sumário

1. [Integrantes](#integrantes)
2. [Links da entrega](#links-da-entrega)
3. [O problema e a solução](#o-problema-e-a-solução)
4. [Decisões de design e justificativas](#decisões-de-design-e-justificativas)
5. [Stack](#stack)
6. [Arquitetura e fluxo CI/CD](#arquitetura-e-fluxo-cicd)
7. [Pipeline CI/CD](#pipeline-cicd)
8. [Infraestrutura](#infraestrutura)
9. [Como executar localmente](#como-executar-localmente)
10. [Banco de dados](#banco-de-dados)
11. [Segurança e perfis](#segurança-e-perfis)
12. [Portal web e API REST](#portal-web-e-api-rest)
13. [Triagem por IA](#triagem-por-ia)
14. [Integração multidisciplinar](#integração-multidisciplinar)
15. [Testes](#testes)
16. [Health check](#health-check)
17. [Problemas comuns](#problemas-comuns)

---

## Integrantes

| Nome completo | RM | Turma |
|---|---|---|
| Guilherme Macedo Martins | RM562396 | 2TDSPO |
| Pedro Henrique Luiz Alves Duarte | RM563405 | 2TDSPO |
| Henrique Martins Oliveira | RM563620 | 2TDSPO |

---

## Links da entrega

| Item | Link |
|---|---|
| Aplicação online (portal) | https://kuravet-rm562396.azurewebsites.net/login |
| Health check em produção | https://kuravet-rm562396.azurewebsites.net/actuator/health |
| Repositório Java Advanced / DevOps | https://github.com/KuraVet-Challenge-2026/java-advanced |
| Vídeo Java Advanced | https://www.youtube.com/watch?v=up5OcE8ulrA `<PREENCHER: confirmar se é o vídeo de Java>` |
| Vídeo DevOps | `<PREENCHER>` |
| Projeto no Azure DevOps | `<PREENCHER>` |
| Repositório de Banco de Dados | https://github.com/KuraVet-Challenge-2026/database |
| Repositório Mobile | `<PREENCHER>` |
| Repositório .NET | `<PREENCHER>` |
| Repositório / evidências de QA | `<PREENCHER>` |

---

## O problema e a solução

**O desafio.** A Clyvo Vet quer sair de um cuidado veterinário **reativo** (o tutor só procura a clínica quando o pet já está mal) para um cuidado **contínuo e preventivo**: acompanhar o pet, orientar o tutor cedo e reservar o tempo do veterinário para quem mais precisa dele.

**Quem usa.**

- **Tutor**, pelo aplicativo mobile: cadastra-se, registra seus pets, descreve sintomas em texto livre para receber uma triagem e solicita teleconsultas. Consome a API REST (`/api/**`).
- **Veterinário**, pelo portal web: vê a fila de solicitações, aprova ou recusa (com justificativa), emite diagnósticos e gerencia o cadastro de pets. Usa as telas Thymeleaf (`/portal/**`).

**O que é original: a triagem explicável.** O tutor escreve o relato do jeito que falaria com um amigo ("minha gata não faz xixi desde ontem e está apática"). Duas peças trabalham em sequência, com responsabilidades separadas:

1. A **LLM (Google Gemini) apenas interpreta o relato**: extrai a lista de sintomas e a duração em dias. Ela nunca decide urgência.
2. Um **motor de regras determinístico (R1–R10) decide a urgência** a partir dos sintomas, da espécie e da idade do pet, e devolve junto a lista das regras que justificam a decisão.

Se a LLM estiver desligada, sem chave ou falhar (timeout, erro de rede, resposta inválida), o sistema cai automaticamente para um extrator por palavras-chave e a triagem continua funcionando. O método realmente usado fica gravado em cada triagem. O resultado é uma classificação que a clínica consegue **auditar regra por regra**, o que uma resposta livre de LLM não permitiria.

---

## Decisões de design e justificativas

| Decisão | Justificativa |
|---|---|
| **Spring Boot + Thymeleaf no mesmo backend** | Portal e API chamam os mesmos *services*; nenhuma regra de negócio é duplicada entre os dois canais, e há um único artefato (jar) para implantar. |
| **Máquina de estados da consulta no `ConsultaService`** | O service é o único ponto autorizado a mudar o status (`SOLICITADA → AGENDADA → REALIZADA`, com `RECUSADA` e `CANCELADA`). Transição inválida responde `400`. O portal e a API não têm como contornar a regra. |
| **Flyway como dono do schema** (`ddl-auto=validate`, sem `repair`) | O Hibernate apenas confere se as entidades batem com o banco. A aplicação **não** repara o histórico do Flyway: migration com problema derruba a inicialização com erro explícito, em vez de esconder o estado do schema. `baseline-on-migrate=false` impede rodar a V1 sobre tabelas existentes. |
| **PL/SQL entregue por migrations** (V8 e V9) | Packages e trigger chegam ao schema da aplicação de forma versionada e repetível; a V9 ainda falha se algum objeto compilar `INVALID`. |
| **HTTP Basic + perfis** | API stateless para o app mobile (HTTP Basic) e form login com sessão e CSRF para o portal; autorização por perfil (`TUTOR`, `VETERINARIO`) e por dono nos services. |
| **Testes sem Oracle no CI** | O agente do pipeline não alcança o Oracle da FIAP; os testes usam JUnit/Mockito/MockMvc e rodam em qualquer lugar. |
| **Gemini com fallback** | A LLM melhora a interpretação do relato, mas a triagem não pode depender dela: sem chave, com timeout ou com resposta inválida, o extrator por palavras-chave assume. |
| **Deploy somente por pipeline** | Segredos ficam no Variable Group do Azure DevOps, não em scripts nem no repositório; todo deploy tem commit e execução rastreáveis. |

---

## Stack

| Camada | Tecnologia |
|---|---|
| Linguagem | Java 21 |
| Framework | Spring Boot 4.1.0 |
| Visualização | Thymeleaf + `thymeleaf-extras-springsecurity6` |
| Persistência | Spring Data JPA + Hibernate |
| Banco de dados | Oracle Database (driver `ojdbc11`) |
| Migrações | Flyway (`flyway-database-oracle`) |
| Segurança | Spring Security (form login no portal, HTTP Basic stateless na API) |
| Validação | Jakarta Bean Validation |
| Observabilidade | Spring Boot Actuator (apenas `/actuator/health`) |
| IA | Google Gemini (`gemini-2.5-flash` por padrão) com fallback por palavras-chave |
| Build | Maven (Maven Wrapper) |
| Utilitários | Lombok |
| Nuvem e CI/CD | Azure Pipelines, Azure Web App Linux (Java 21), Azure CLI |

---

## Arquitetura e fluxo CI/CD

![Arquitetura e fluxo CI/CD do KuraVet](docs/arquitetura-cicd.png)

**Fluxo:**

1. **Desenvolvedor** faz push ou abre Pull Request no **GitHub** (`KuraVet-Challenge-2026/java-advanced`).
2. O **Azure Pipelines** dispara o **CI**: compila, roda os testes (`./mvnw -B clean verify`), publica os resultados e gera o artefato `kuravet-jar`. Em Pull Request, o fluxo termina aqui.
3. Em push na `master`, o stage **CD** baixa o artefato `kuravet-jar`.
4. O CD aplica as *app settings* no Web App, lidas do **Variable Group** `kuravet-producao` (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `LLM_API_KEY`) mais `SERVER_PORT=80`.
5. O CD publica o `app.jar` no **Azure Web App** `kuravet-rm562396` (Linux, Java 21), usando a service connection `sc-kuravet-azure`.
6. O pipeline faz o **health check** em `/actuator/health` e só conclui com HTTP 200 e status `UP`.
7. O Web App acessa o **Oracle da FIAP** (externo ao Azure) e a **API do Gemini**.
8. O **Tutor** usa o **app mobile** (consome a API REST em `/api/**`) e o **Veterinário** usa o **portal web** (`/portal/**`). O **Professor/Avaliador** acessa a aplicação publicada, o pipeline e este repositório.

---

## Pipeline CI/CD

Definido em [`azure-pipelines.yml`](azure-pipelines.yml), na raiz do repositório.

**Gatilhos**

| Evento | O que roda |
|---|---|
| Push na `master` | CI + CD |
| Pull Request para a `master` | Somente CI (nunca faz deploy) |

O CD só executa se o CI passou, a branch é `refs/heads/master` e o motivo do build não é `PullRequest`.

**Stage `CI` — "Build e testes"** (agente `ubuntu-latest`)

1. Instala o JDK 21.
2. Restaura o cache do repositório Maven (chave baseada no `kuravet/pom.xml`).
3. `./mvnw -B clean verify` em `kuravet/` (compila e roda os testes, sem Oracle).
4. Publica os resultados JUnit do Surefire (`TEST-*.xml`) na aba **Tests**; teste com falha reprova o pipeline.
5. Confere que existe exatamente um jar executável em `kuravet/target` e o copia como `kuravet.jar`.
6. Publica o artefato de pipeline **`kuravet-jar`**.

**Stage `CD` — "Deploy no Azure Web App"** (job de deployment no ambiente `producao`, sem aprovação manual)

1. Baixa o artefato `kuravet-jar`.
2. Renomeia o jar para `app.jar`.
3. `AzureAppServiceSettings@1`: aplica as app settings no Web App.
4. `AzureWebApp@1`: publica o `app.jar` (`JAVA|21-java21`, startup `java -jar /home/site/wwwroot/app.jar`).
5. Verifica `/actuator/health` até 20 vezes, com 15 s de intervalo; falha o stage se não obtiver HTTP 200 com `"UP"`.

**Segredos e configuração**

| Item | Onde fica |
|---|---|
| `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `LLM_API_KEY` | Variable Group **`kuravet-producao`** (as credenciais e a chave da LLM como secret). Declarado só no stage CD: o CI e os builds de PR não têm acesso. |
| Service connection | `sc-kuravet-azure` (nome fixo no YAML, pois precisa ser conhecido em tempo de compilação) |
| Resource Group, nome do Web App, URL de health | Variáveis não sensíveis no próprio YAML |
| Histórico de deploys | Azure DevOps → Pipelines → Environments → `producao` |

---

## Infraestrutura

A aplicação roda em um **Azure Web App Linux com Java 21**. O banco é o **Oracle da FIAP**, externo ao Azure: nenhum banco é criado na nuvem. Os scripts PowerShell ficam em [`infra/`](infra/).

> ⚠️ **O deploy da aplicação é feito exclusivamente pelo pipeline.** Os scripts apenas criam e configuram os recursos; não existe deploy manual, e nenhum segredo fica nos scripts nem neste README.

**Pré-requisitos:** [Azure CLI](https://learn.microsoft.com/cli/azure/install-azure-cli) (`az version`), sessão com `az login` na assinatura correta e PowerShell com o diretório atual em `infra/`.

**Ordem de execução** (todos na **mesma sessão**, porque dependem das variáveis do `00`):

```powershell
cd infra
. .\00-Variaveis.ps1          # dot-source: carrega $RG, $LOCAL, $PLANO e $WEBAPP
.\01-ResourceGroup.ps1
.\02-AppServicePlan.ps1
.\03-WebApp.ps1
.\04-ConfigurarWebApp.ps1
```

Sem o `00` carregado, cada script para com a mensagem `Rode primeiro: . .\00-Variaveis.ps1`.

| Script | O que faz |
|---|---|
| `00-Variaveis.ps1` | Define `rg-kuravet-rm562396`, `plan-kuravet-rm562396`, `kuravet-rm562396` e a região `brazilsouth` (alternativa `mexicocentral` se a política da assinatura bloquear) |
| `01-ResourceGroup.ps1` | Cria o Resource Group |
| `02-AppServicePlan.ps1` | Cria o App Service Plan Linux, SKU B1 |
| `03-WebApp.ps1` | Cria o Web App com runtime `JAVA:21-java21` e ativa HTTPS only |
| `04-ConfigurarWebApp.ps1` | Liga o Always On e desabilita FTP/FTPS |
| `99-RemoverRecursos.ps1` | Apaga o Resource Group inteiro, após confirmar digitando o nome dele |

**Validação:**

```powershell
az webapp show -g $RG -n $WEBAPP --query state -o tsv                  # esperado: Running
az webapp show -g $RG -n $WEBAPP --query "{https:httpsOnly, host:defaultHostName}"
az webapp config show -g $RG -n $WEBAPP --query "{alwaysOn:alwaysOn, ftps:ftpsState, java:linuxFxVersion}"
```

Depois que o pipeline publicar a aplicação, `https://<defaultHostName>/api/ping` responde `pong` e `/actuator/health` responde `UP`.

---

## Como executar localmente

**Pré-requisitos**

- JDK 21 ou superior no `PATH` (`java -version`). O Maven não precisa estar instalado (Maven Wrapper).
- Acesso de rede ao Oracle da FIAP (`oracle.fiap.com.br:1521`), com VPN quando exigido, e um schema Oracle válido.

**Configuração.** As credenciais não são versionadas. O `application.properties` traz só placeholders e importa, se existir, o `application-local.properties` (ignorado pelo Git):

```properties
spring.datasource.url=${DB_URL:jdbc:oracle:thin:@oracle.fiap.com.br:1521:ORCL}
spring.datasource.username=${DB_USERNAME:}
spring.datasource.password=${DB_PASSWORD:}
```

Crie `kuravet/src/main/resources/application-local.properties`:

```properties
spring.datasource.username=<SEU_USUARIO_ORACLE>
spring.datasource.password=<SUA_SENHA>
# opcional: sem a chave, a triagem usa apenas palavras-chave
kuravet.ia.llm.api-key=<SUA_CHAVE_GEMINI>
```

Alternativa: variáveis de ambiente `DB_USERNAME`, `DB_PASSWORD` e `LLM_API_KEY`. O `application.properties` deve estar em **UTF-8**.

**Execução** (a partir da pasta `kuravet/`, a que contém o `pom.xml`):

```powershell
# Windows (PowerShell)
git clone https://github.com/KuraVet-Challenge-2026/java-advanced.git
cd java-advanced\kuravet
.\mvnw.cmd clean spring-boot:run
```

```bash
# Linux / macOS
git clone https://github.com/KuraVet-Challenge-2026/java-advanced.git
cd java-advanced/kuravet
./mvnw clean spring-boot:run
```

Pela IDE, abra a pasta `kuravet` como projeto Maven e execute `br.com.fiap.kuravet.KuravetApplication`. A aplicação sobe em **http://localhost:8080** e aplica as migrations do Flyway na inicialização.

---

## Banco de dados

O schema pertence ao Flyway (`kuravet/src/main/resources/db/migration/`). Nenhum script precisa ser executado à mão.

| Migration | Conteúdo |
|---|---|
| `V1__schema.sql` | DDL de TUTOR, VETERINARIO, PET, CONSULTA, USUARIO, sequences e constraints |
| `V2__seed_perfis_e_veterinario.sql` | 10 veterinários e o usuário de perfil VETERINARIO |
| `V3__seed_tutor_inicial.sql` | Tutor inicial e seu usuário de perfil TUTOR |
| `V4__consulta_teleconsulta.sql` | Status `SOLICITADA` e `RECUSADA`, colunas `DATA_SOLICITACAO` e `MOTIVO_RECUSA`, novas CHECK constraints |
| `V5__seed_pets_e_consultas.sql` | Pets e consultas cobrindo todos os estados do fluxo |
| `V6__pet_codigo_identificacao.sql` | Coluna opcional `CODIGO_IDENTIFICACAO` em PET, única quando preenchida |
| `V7__triagem.sql` | Tabela TRIAGEM (relato, sintomas, urgência, recomendação, regras aplicadas, método de extração), `SEQ_TRIAGEM` e índice por pet |
| `V8__banco_tabelas_apoio.sql` | FATO_PAGAMENTO, AUDITORIA_LOG, LOG_ERRO_CARGA, índices de FK/filtros, `SEQ_PAGAMENTO`, dados de demonstração (consultas 50–57 REALIZADAS e 8 pagamentos em 3 clínicas); avança `SEQ_CONSULTA` para ≥ 100 |
| `V9__banco_packages_e_trigger.sql` | Packages `PKG_KV_*`, trigger `TRG_AUDITORIA_CONSULTA` e verificação final que falha a migration se algum objeto ficar `INVALID` |

**Regras**

- **Migrations aplicadas são imutáveis.** O Flyway compara o checksum e se recusa a continuar se o arquivo mudou. Correção entra em nova versão (`V10__`...).
- **Sem `repair` automático.** Migration com problema derruba a inicialização com erro explícito.
- **Nunca rode `flyway repair`, `flyway clean` nem os scripts avulsos da disciplina de Banco no schema do backend**: eles começam apagando TUTOR, PET e CONSULTA.
- **Migrations com PL/SQL são testadas antes em um schema sandbox** (apontando `DB_USERNAME`/`DB_PASSWORD` para ele).
- **Se uma migration falhar:** pare, avise o time e não reinicie a aplicação em loop nem edite o histórico à mão.

**Objetos PL/SQL da V9.** Erros de negócio usam o catálogo `-20001..-20099` (`PKG_KV_UTIL`) e são gravados em `LOG_ERRO_CARGA` por transação autônoma. As rotinas de carga não fazem `COMMIT`: quem chama controla a transação.

| Package | Rotinas |
|---|---|
| `PKG_KV_UTIL` | `PRC_REGISTRAR_ERRO`, `PRC_LANCAR_ERRO`, `PRC_TRATAR_INESPERADO`, `FN_MONTA_JSON`, `FN_CALCULA_IDADE_PET`, `FN_CATEGORIA_ETARIA` |
| `PKG_KV_AUDITORIA` | `FN_SERIALIZAR_CONSULTA`, `PRC_REGISTRAR` |
| `PKG_KV_CARGA` | `PRC_CARGA_TUTOR`, `PRC_CARGA_VETERINARIO`, `PRC_CARGA_PET`, `PRC_CARGA_CONSULTA`, `PRC_REGISTRAR_DIAGNOSTICO`, `PRC_CARGA_PAGAMENTO` |
| `PKG_KV_RELATORIO` | `PRC_LISTAR_CONSULTAS_JSON`, `PRC_SUBTOTAIS_PAGAMENTO` (console), `PRC_CONSULTAS_TUTOR`, `PRC_FATURAMENTO_CLINICA_TIPO` (`SYS_REFCURSOR`) |
| `PKG_KV_EXPORT` | `FN_DOC_PET`, `FN_DOC_VETERINARIO`, `FN_EXPORTAR_PETS`, `FN_EXPORTAR_VETERINARIOS` (JSON para o MongoDB) |

**Trigger:** `TRG_AUDITORIA_CONSULTA` (`AFTER INSERT OR UPDATE OR DELETE ... FOR EACH ROW` em `CONSULTA`) apenas dispara; a regra fica em `PKG_KV_AUDITORIA`, que grava OLD/NEW em JSON em `AUDITORIA_LOG`. Se a auditoria falhar, a alteração na consulta é bloqueada. Como o Oracle não falha um `CREATE` com erro de compilação, o último bloco da V9 confere `USER_OBJECTS` e lança `ORA-20900` com os nomes dos objetos inválidos.

> A aplicação Java **ainda não chama** as procedures e functions dos packages; o que a aplicação exercita hoje é a trigger de auditoria, a cada alteração em `CONSULTA` (aprovar, recusar, diagnosticar, cancelar).

Os scripts avulsos da disciplina (DDL, carga, packages, evidências e exportação JSON) ficam no repositório [`database`](https://github.com/KuraVet-Challenge-2026/database), executados em um schema Oracle separado. Neste backend, os mesmos objetos chegam ao schema da aplicação **somente** pelas migrations V8 e V9.

**Regras espelhadas no schema** (CHECK/UNIQUE constraints que repetem as regras do Java):

| Constraint | Garante |
|---|---|
| `KV_CK_CONS_STATUS` | status ∈ {SOLICITADA, AGENDADA, REALIZADA, CANCELADA, RECUSADA} |
| `KV_CK_CONS_DIAGNOSTICO` | diagnóstico existe se e somente se o status é `REALIZADA` |
| `KV_CK_CONS_MOTIVO` | motivo de recusa existe se e somente se o status é `RECUSADA` |
| `KV_CK_USU_PERFIL` | perfil ∈ {TUTOR, VETERINARIO} |
| `KV_CK_USU_TUTOR` | `ID_TUTOR` obrigatório para TUTOR e proibido para VETERINARIO |
| `KV_CK_PET_SEXO` | sexo ∈ {M, F} |
| `KV_UQ_PET_CODIGO` | `CODIGO_IDENTIFICACAO` único quando preenchido |

---

## Segurança e perfis

Duas cadeias de filtros isoladas, em `SecurityConfig`:

| Cadeia | Rotas | Autenticação | Sessão | CSRF |
|---|---|---|---|---|
| API mobile | `/api/**` | HTTP Basic | Stateless | Desabilitado |
| Portal web | demais rotas | Form login | Sessão HTTP | Habilitado |

- A autenticação consulta a tabela `USUARIO` (`UsuarioDetailsService`), com senhas em **BCrypt**.
- Autorização por perfil nas rotas (`SecurityConfig`) e **por dono** nos services: pet, consulta ou tutor de outro dono responde `404`, não `403`, para não revelar a existência de registros de terceiros.
- Falha de autenticação em `/api/**` responde `401` puro, com `WWW-Authenticate`.
- `/portal/**` exige `VETERINARIO` e `/tutor/**` exige `TUTOR`. Depois do login, cada perfil cai na sua área (`VETERINARIO` → `/portal/painel`, `TUTOR` → `/tutor/consultas`); `GET /` redireciona pelo perfil. Quem abre a área do outro perfil vê a página amigável `/acesso-negado` (HTTP 403), não a página de erro padrão. Públicos: `/login`, `/error`, `/css/**`, `/api/ping`, `/actuator/health` e `POST /api/auth/cadastro`.
- `POST /api/auth/cadastro` sempre cria usuário `TUTOR`: o perfil nunca vem do corpo da requisição.

**Usuários de teste** (dados de demonstração criados pelas migrations V2 e V3):

| Usuário | Senha | Perfil | Acesso |
|---|---|---|---|
| `veterinario` | `vet123` | VETERINARIO | Portal web e API |
| `tutor` | `tutor123` | TUTOR | API (app mobile) e área do tutor no portal (`/tutor/**`) |

---

## Portal web e API REST

### Portal web (perfil `VETERINARIO`)

| Rota | Tela / ação |
|---|---|
| `GET /login` | Login (público) |
| `GET /portal/painel` | Indicadores por status e resumo do fluxo |
| `GET /portal/solicitacoes` | Fila de teleconsultas aguardando decisão |
| `POST /portal/solicitacoes/{id}/aprovar` | Aprova a solicitação |
| `GET/POST /portal/solicitacoes/{id}/recusar` | Formulário e gravação da recusa (justificativa de 10 a 300 caracteres) |
| `GET /portal/consultas` | Histórico, filtrável por status |
| `GET /portal/consultas/{id}` | Detalhe da consulta |
| `POST /portal/consultas/{id}/diagnostico` | Emite o diagnóstico (15 a 400 caracteres) e encerra |
| `GET /portal/pets` | Lista de pets |
| `GET/POST /portal/pets/novo` | Cadastro de pet |
| `GET/POST /portal/pets/{id}/editar` | Edição de pet |
| `POST /portal/pets/{id}/excluir` | Exclusão de pet |
| `GET /portal/triagens` | Fila de triagens, das mais urgentes para as menos (e, no mesmo nível, das mais recentes). Filtro opcional `?urgencia=` (`EMERGENCIA`, `ALTA`, `MODERADA`, `BAIXA`) ou `?urgentes=true` (ALTA + EMERGENCIA). Mostra data/hora, pet, tutor, urgência (selo colorido), sintomas, método (LLM ou palavras-chave) e link "Ver" |
| `GET /portal/triagens/{id}` | Detalhe: relato do tutor, sintomas, urgência, recomendação, "Por que esta classificação?" com as regras aplicadas, método de interpretação e aviso de que a triagem é orientativa; atalhos para o histórico do pet e para as consultas |
| `GET /portal/pets/{id}/triagens` | Histórico de triagens do pet, da mais recente para a mais antiga, com urgência, sintomas e data |

As telas de triagem são somente de visualização: a triagem é criada pela API (app do tutor) e o portal apenas exibe o que foi gravado, sem chamar a LLM. O painel traz o card "Triagens urgentes (7 dias)", com a quantidade de triagens ALTA e EMERGENCIA dos últimos 7 dias e link para a fila filtrada.

### Área do tutor no portal (perfil `TUTOR`)

| Rota | Tela / ação |
|---|---|
| `GET /tutor/consultas` | "Minhas consultas": pet, veterinário, data, tipo, status e diagnóstico ou motivo da recusa. O botão "Cancelar" só aparece quando a regra do `ConsultaService.podeCancelar` permite (`SOLICITADA` e `AGENDADA`) |
| `POST /tutor/consultas/{id}/cancelar` | Cancela a consulta (com confirmação) e volta à lista com mensagem de sucesso |
| `GET /acesso-negado` | Página amigável para quem acessa a área de outro perfil, com botão para a área do perfil logado |

Solicitar teleconsulta e fazer triagem continuam sendo feitos pelo app mobile (API REST).

### API REST (app mobile)

Todas as rotas exigem HTTP Basic, exceto `/api/ping` e `POST /api/auth/cadastro`.

| Método | Rota | Perfil | Descrição |
|---|---|---|---|
| `GET` | `/api/ping` | público | Retorna `pong` |
| `POST` | `/api/auth/cadastro` | público | Autocadastro: cria TUTOR + USUARIO |
| `GET` | `/api/auth/me` | autenticado | Perfil de quem está logado |
| `GET` | `/api/pets`, `/api/pets/{id}` | autenticado | Lista/busca pets (TUTOR: só os próprios) |
| `POST` / `PUT` / `DELETE` | `/api/pets`, `/api/pets/{id}` | TUTOR | Cadastra, atualiza e exclui pet próprio |
| `GET` | `/api/tutores`, `/api/tutores/{id}` | autenticado | Lista/busca (TUTOR: só o próprio; VETERINARIO: todos) |
| `POST` | `/api/tutores` | autenticado | Cadastro direto de tutor, sem login (uso administrativo) |
| `PUT` / `DELETE` | `/api/tutores/{id}` | autenticado | Atualiza/exclui (TUTOR: só o próprio) |
| `GET` | `/api/veterinarios`, `/api/veterinarios/{id}` | autenticado | Lista/busca veterinários (leitura) |
| `GET` | `/api/consultas?status=` | autenticado | Lista consultas, opcionalmente por status |
| `GET` | `/api/consultas/{id}` | autenticado | Busca consulta |
| `POST` | `/api/consultas/solicitacoes` | TUTOR | Solicita teleconsulta |
| `PATCH` | `/api/consultas/{id}/aprovacao` | VETERINARIO | Aprova |
| `PATCH` | `/api/consultas/{id}/recusa` | VETERINARIO | Recusa com justificativa |
| `PATCH` | `/api/consultas/{id}/diagnostico` | VETERINARIO | Emite diagnóstico e encerra |
| `PATCH` | `/api/consultas/{id}/cancelamento` | autenticado | TUTOR cancela a própria `SOLICITADA`/`AGENDADA`; VETERINARIO só `AGENDADA` |
| `PUT` | `/api/consultas/{id}` | autenticado | Atualiza pet, veterinário, data ou tipo |
| `DELETE` | `/api/consultas/{id}` | VETERINARIO | Exclui (bloqueado para `REALIZADA`) |
| `POST` | `/api/triagens` | TUTOR | Triagem de um pet próprio a partir do relato |
| `GET` | `/api/triagens`, `/api/triagens/{id}` | autenticado | Lista (mais urgentes primeiro) e busca triagens |

**Máquina de estados da consulta**

```
SOLICITADA ──aprovar───▶ AGENDADA ──diagnóstico──▶ REALIZADA
     │    ╲                  │
   recusar  ╲cancelar      cancelar
     ▼        ╲               ▼
  RECUSADA      ╲────────▶ CANCELADA
```

`REALIZADA`, `RECUSADA` e `CANCELADA` são terminais. Transição fora do mapa responde `400` com `Transicao de status invalida: X -> Y`. Na solicitação, o pet precisa ser do tutor autenticado, o veterinário precisa existir, a data precisa ser futura e o pet não pode ter outra consulta ativa na mesma data.

**Exemplo 1 — validar login e obter o perfil**

```http
GET /api/auth/me
Authorization: Basic <usuario:senha em base64>
```

```json
{ "idUsuario": 100, "username": "carlos_novo", "perfil": "TUTOR", "idTutor": 11, "nomeTutor": "Carlos Eduardo Lima" }
```

**Exemplo 2 — solicitar teleconsulta (TUTOR)**

```http
POST /api/consultas/solicitacoes
Authorization: Basic <usuario:senha em base64>
Content-Type: application/json

{ "idPet": 1, "idVeterinario": 2, "dataConsulta": "2026-12-01", "tipoConsulta": "Teleconsulta - Dermatologia" }
```

Resposta `201 Created` com `"status": "SOLICITADA"`.

**Exemplo 3 — recusar solicitação (VETERINARIO)**

```http
PATCH /api/consultas/12/recusa
Authorization: Basic <usuario:senha em base64>
Content-Type: application/json

{ "motivo": "Caso ortopedico exige exame presencial com raio-x." }
```

Erros da API seguem um JSON padronizado (`timestamp`, `status`, `erro`, `mensagem` e, em validação, `campos`); o portal exibe páginas HTML de erro (`ApiExceptionHandler` e `PortalExceptionHandler`, cada um restrito ao seu pacote).

---

## Triagem por IA

Código em `br.com.fiap.kuravet.ia` (`MotorRegrasTriagem`, `ExtratorComFallback`, `ExtratorPalavrasChave`) e `br.com.fiap.kuravet.ia.llm` (`ExtratorGemini`).

**Regras.** Urgências, da menor para a maior: `BAIXA` < `MODERADA` < `ALTA` < `EMERGENCIA`. Filhote = menos de 1 ano; idoso = 10 anos ou mais.

| Regra | Condição | Urgência |
|---|---|---|
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

A urgência final é a **maior** entre as regras R1–R8 que dispararam; depois aplica-se o escalonamento R9; sem nenhuma regra, vale a R10. `regrasAplicadas` lista todas as regras disparadas. Toda recomendação termina com *"Esta triagem é orientativa e não substitui a avaliação do veterinário."*

| Urgência | Recomendação |
|---|---|
| EMERGENCIA | Procure atendimento presencial imediato em um pronto-socorro veterinário. |
| ALTA | Solicite uma teleconsulta prioritária ainda hoje. |
| MODERADA | Agende uma teleconsulta nas próximas 48 horas e observe a evolução. |
| BAIXA | Observe por 48 horas; se surgirem novos sintomas, refaça a triagem. |
| BAIXA (R10) | Descreva melhor os sintomas ou agende uma teleconsulta de rotina. |

**LLM e fallback** (`ExtratorComFallback`)

1. Se `kuravet.ia.llm.enabled=false` ou a chave está vazia, usa palavras-chave, sem chamada HTTP.
2. Caso contrário, chama o Gemini; sucesso grava o método `LLM`.
3. Qualquer falha (timeout, rede, HTTP não 2xx, resposta bloqueada, sem `candidates`, JSON inválido) gera log `WARN` com o motivo e cai para `PALAVRAS_CHAVE`.

A coluna `METODO_EXTRACAO` da tabela TRIAGEM guarda o método que **realmente** produziu os sintomas. A requisição usa temperatura 0 e `responseSchema` com enum de sintomas; o relato vai delimitado por `<relato></relato>` e a instrução de sistema manda ignorar instruções dentro dele. A chave segue no header `x-goog-api-key`, nunca na URL, e os logs nunca contêm o relato, a chave nem o corpo da resposta.

| Propriedade | Padrão |
|---|---|
| `kuravet.ia.llm.enabled` | `${LLM_ENABLED:true}` |
| `kuravet.ia.llm.api-key` | `${LLM_API_KEY:}` (vazia = só palavras-chave) |
| `kuravet.ia.llm.base-url` | `https://generativelanguage.googleapis.com/v1beta` |
| `kuravet.ia.llm.model` | `${LLM_MODEL:gemini-2.5-flash}` |
| `kuravet.ia.llm.timeout-ms` | `8000` |

**Exemplo.** O `relato` é obrigatório (10 a 1000 caracteres); pet de outro tutor responde `404`.

```http
POST /api/triagens
Authorization: Basic <usuario:senha em base64>
Content-Type: application/json

{ "idPet": 2, "relato": "Minha gata não faz xixi desde ontem e está apática" }
```

Resposta `201 Created`:

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

---

## Integração multidisciplinar

| Disciplina | Como foi aplicada | Evidência |
|---|---|---|
| **Java Advanced** | Backend Spring Boot com portal Thymeleaf, API REST, segurança por perfil, máquina de estados e Flyway | [`kuravet/`](kuravet/) |
| **DevOps** | Pipeline de CI/CD no Azure Pipelines, deploy em Azure Web App via Variable Group e health check, infraestrutura por scripts | [`azure-pipelines.yml`](azure-pipelines.yml), [`infra/`](infra/) |
| **Banco de Dados** | Tabelas de apoio (V8) e packages PL/SQL com trigger de auditoria (V9) no schema do backend, entregues por migration. A trigger é exercitada pelas telas e endpoints que alteram consultas; a chamada direta das procedures pela aplicação ainda não existe | [`V8`](kuravet/src/main/resources/db/migration/V8__banco_tabelas_apoio.sql), [`V9`](kuravet/src/main/resources/db/migration/V9__banco_packages_e_trigger.sql), [repo `database`](https://github.com/KuraVet-Challenge-2026/database) |
| **Inteligência Artificial** | Triagem explicável: Gemini extrai sintomas, motor de regras R1–R10 decide a urgência, fallback por palavras-chave | [`kuravet/src/main/java/br/com/fiap/kuravet/ia/`](kuravet/src/main/java/br/com/fiap/kuravet/ia/), endpoint `POST /api/triagens` |
| **Mobile** | O app do tutor (React Native) consome a API `/api/**` com HTTP Basic: cadastro, login, pets, consultas e triagem | `kuravet/src/main/java/br/com/fiap/kuravet/controller/api/`; repositório do app: `<PREENCHER>` |
| **.NET** | `<PREENCHER: como a disciplina foi integrada>` | `<PREENCHER: link do repositório>` |
| **QA** | Testes automatizados executados no CI (ver [Testes](#testes)); gestão de testes e tarefas no Azure Boards: `<PREENCHER>` | [`kuravet/src/test/`](kuravet/src/test/); Azure Boards: `<PREENCHER>` |

---

## Testes

```powershell
cd kuravet
.\mvnw.cmd clean verify      # Windows
```

```bash
cd kuravet
./mvnw clean verify          # Linux / macOS
```

**Resultado atual: 164 testes, 0 falhas, 0 erros** (execução local de `./mvnw clean verify`). Nenhum teste precisa de Oracle nem de internet; o pipeline roda exatamente este comando.

| Área | Classes de teste |
|---|---|
| Motor de regras da triagem (R1–R10) | `MotorRegrasTriagemTest` |
| Extração por palavras-chave, Gemini e fallback | `ExtratorPalavrasChaveTest`, `ExtratorGeminiTest`, `ExtratorComFallbackTest` |
| Serviços (máquina de estados, ownership, triagem) | `ConsultaServiceTest`, `PetServiceTest`, `TriagemServiceTest` |
| Segurança e autorização da API | `ConsultaControllerSecurityTest`, `TriagemControllerSecurityTest` |
| Portal web (painel, consultas, pets, triagem e área do tutor) | `PortalWebControllerTest`, `TriagemWebControllerTest`, `TutorWebControllerTest` |
| Redirecionamento do login por perfil | `RedirecionamentoPorPerfilHandlerTest` |
| Health check público | `HealthEndpointSecurityTest` |

---

## Health check

`GET /actuator/health` é público e informa se a aplicação e o banco Oracle estão de pé. Em produção: https://kuravet-rm562396.azurewebsites.net/actuator/health

```json
{ "status": "UP", "components": { "db": { "status": "UP" }, "diskSpace": { "status": "UP" }, "ping": { "status": "UP" } } }
```

- **`UP`** (HTTP 200): a aplicação respondeu e o banco aceitou a conexão.
- **`DOWN`** (HTTP 503): algum componente falhou, em geral o banco (credencial, rede ou Oracle fora do ar).
- Sem detalhes (`show-details=never`): só o status de cada componente, sem URL, usuário ou mensagem de erro.
- É o único endpoint do Actuator exposto (`management.endpoints.web.exposure.include=health`).
- O stage CD chama essa URL após o deploy e só passa com HTTP 200 e `"UP"` no corpo.

---

## Problemas comuns

**`ORA-12541: não há listener em host localhost port 1521`** — a URL do datasource não foi carregada e caiu no padrão. Confirme que o `application-local.properties` existe e que o `application.properties` está em UTF-8.

**`ORA-01017: invalid username/password`** — credenciais incorretas no `application-local.properties` ou nas variáveis de ambiente.

**`Migration checksum mismatch`** — uma migration já aplicada foi editada. Reverta o arquivo ou crie uma nova versão.

**`Detected failed migration to version N`** — uma migration falhou pela metade. Não há reparo automático: pare, avise o time e siga as [regras do banco](#banco-de-dados) (correção em nova versão, sem `repair`/`clean`).

**`Found non-empty schema(s) but no schema history table`** — o schema tem tabelas mas não tem `flyway_schema_history`. Aponte para o schema correto ou avise o time; não habilite baseline por conta própria.

**Timeout ao conectar em `oracle.fiap.com.br`** — verifique a conexão com a rede/VPN da FIAP.

**A IDE não reconhece o projeto / botão de execução ausente** — abra a pasta `kuravet` (a que contém o `pom.xml`), não a raiz do repositório.

**O deploy passou, mas o health check falha** — confira no Variable Group `kuravet-producao` os valores de `DB_URL`, `DB_USERNAME` e `DB_PASSWORD`, e se o Oracle da FIAP está acessível a partir do Azure.
