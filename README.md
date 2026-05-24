# 🐾 KuraVet - Ecossistema de Saúde Animal Contínua

## 📋 Sobre o Projeto

O **KuraVet** é uma solução tecnológica desenvolvida para o **Challenge 2026 da FIAP**, em parceria com a **Clyvo Vet**.

O objetivo do projeto é transformar a jornada de saúde animal, migrando de um modelo reativo e episódico para um ecossistema de cuidado contínuo, preventivo e inteligente.

A plataforma permite que tutores e clínicas veterinárias acompanhem o histórico clínico longitudinal do pet, gerenciem check-ins e monitorem indicadores de vitalidade, garantindo maior fidelização para as clínicas e melhor qualidade de vida para os animais.

---

# 📊 Diagrama de Classe UML

<a href="https://ibb.co/JF7zGjMN">
  <img src="https://i.ibb.co/6JP4T0f2/imagem-2.png" alt="imagem (2)" border="0">
</a>

---

# 👥 Equipe (Squad)

- Pedro Henrique Luiz Alves Duarte
- Guilherme Macedo Martins
- Henrique Martins

**Turma:** 2TDSPO

---

# 🚀 Tecnologias Utilizadas

- Java 17
- Spring Boot 4.0.6
- Spring Data JPA (Persistência de Dados)
- Oracle SQL / H2 Database (Banco de Dados Relacional)
- Docker & Docker Compose (Containerização da Infraestrutura)
- Bean Validation (Validação de Dados)
- Spring Cache (Otimização de Performance)
- Springdoc OpenAPI (Swagger) (Documentação da API)
- Maven (Gerenciador de Dependências)

---

# 🏛️ Arquitetura, Desacoplamento e Padrões de Projeto

Visando atender aos critérios de coesão, desacoplamento e padrões de projeto estabelecidos na rubrica, a API foi estruturada seguindo rigorosamente a arquitetura em camadas independentes:

## Camada de Apresentação (Controllers)

Responsável estritamente pela exposição dos endpoints RESTful, manipulação dos códigos de status HTTP e integração com a documentação do Swagger.

## Camada de Negócio (Services)

Concentra as regras de negócio operacionais, isolando as decisões lógicas das camadas externas.

## Camada de Acesso a Dados (Repositories)

Aplicação do Repository Pattern através de interfaces que estendem o `JpaRepository`, abstraindo por completo as instruções SQL/JPQL nativas.

## Data Transfer Objects (DTOs)

Implementados utilizando `records` do Java para assegurar a imutabilidade física dos dados trafegados, evitando a exposição direta das entidades de banco de dados (`Entities`) na rede.

---

# 🔗 Mapeamento Relacional e Constraints

As classes de domínio foram modeladas para garantir a integridade referencial absoluta do banco de dados relacional:

## Tutor (`tbl_tutor`)

Possui chaves exclusivas (`unique = true`) para os campos `nr_cpf` e `ds_email`, impedindo duplicidade cadastral de usuários no sistema.

## Pet (`tbl_pet`)

Estabelece um relacionamento `@ManyToOne` com `Tutor`, mapeado por uma chave estrangeira obrigatória (`nullable = false`), garantindo que nenhum animal fique sem um responsável legal associado.

## CheckinHistorico e EventoConsulta

Possuem relacionamentos `@ManyToOne` obrigatórios direcionados à entidade `Pet`, consolidando o histórico longitudinal clínico de maneira amarrada e rastreável.

---

# ⚙️ Funcionalidades e Requisitos Técnicos Atendidos

## ✅ CRUD Completo e Funcional

Operações completas de persistência, leitura, atualização e exclusão aplicadas em todos os módulos core:

- Tutores
- Pets
- Check-ins
- Eventos Clínicos

---

## ✅ Validação Estatística (Bean Validation)

Garantia da consistência de dados na entrada do Controller via interceptação `@Valid`:

- `@NotBlank` para preenchimentos mandatórios de identificação.
- `@CPF` da Hibernate Validator para impedir inserção de documentos inválidos.
- `@Email` garantindo a formatação adequada de comunicação eletrônica.

---

## ✅ Paginação e Ordenação de Resultados

Implementado o recebimento dinâmico de parâmetros de paginação através do objeto `Pageable` com ordenação padrão (`sort = "nome"`) na listagem geral de Pets, protegendo a aplicação contra gargalos de memória.

---

## ✅ Busca Parametrizada

Método customizado `findByEspecieIgnoreCase` mapeado via Query Methods do Spring Data JPA, permitindo buscas flexíveis ignorando caixas altas ou baixas.

---

## ✅ Otimização de Performance (Spring Cache)

- `@Cacheable` acoplado nos métodos de listagem e busca parametrizada para evitar acessos desnecessários ao banco de dados para queries idênticas.
- `@CacheEvict(allEntries = true)` disparado nos métodos de escrita (atualizar e excluir) para garantir a invalidação correta e consistência do cache.

---

## ✅ Tratamento Centralizado de Erros (Exception Handler)

Presença da classe especialista `GlobalExceptionHandler` anotada com `@ControllerAdvice`, eliminando vazamentos de stacktraces internos e capturando:

- Falhas de validação sintática (`400 Bad Request`)
- Entidades não encontradas por ID (`404 Not Found`)

---

## ✅ Modelo de Maturidade RESTful

A API respeita os pilares do REST:

- Uso correto de verbos HTTP (`POST`, `GET`, `PUT`, `DELETE`)
- Mapeamentos lógicos no plural
- Retorno apropriado de códigos HTTP
- `204 No Content` para exclusões bem-sucedidas

---

# 🛠️ Como Executar o Projeto

## 📌 Pré-requisitos

- Java 17 ou superior
- Maven instalado
- Opcional: Docker e Docker Compose

---

# ▶️ Modo 1: Execução Local Padrão (Banco H2 em Memória)

## Clone o repositório e navegue até a pasta

```bash
git clone https://github.com/KuraVet-Challenge-2026/java-advanced.git
cd java-advanced/kuravet
```

## Compile o projeto e instale as dependências

```bash
mvn clean install
```

## Execute o servidor Spring Boot

```bash
mvn spring-boot:run
```

## Acesse a documentação Swagger interativa

```bash
http://localhost:8080/swagger-ui/index.html
```

---

# ▶️ Modo 2: Execução via Docker Compose (Com Banco Oracle XE)

Para validar a portabilidade do projeto e a persistência em ambiente Oracle de produção, utilize a infraestrutura containerizada configurada no projeto.

## Execute na raiz do projeto

```bash
docker-compose up --build
```

Isso fará o download da imagem oficial do Oracle Database, subirá o banco e compilará a API apontando automaticamente para o perfil de produção (`SPRING_PROFILES_ACTIVE=docker`), expondo o serviço na porta `80`.

---

# 🔍 Documentação Completa dos Endpoints

# 👤 Módulo: Tutores (`/tutores`)

| Método | Endpoint | Descrição |
|---|---|---|
| POST | `/tutores` | Cadastra um novo tutor (Valida CPF e e-mail no corpo) |
| GET | `/tutores` | Retorna uma lista de todos os tutores cadastrados |
| PUT | `/tutores/{id}` | Atualiza todos os dados cadastrais do tutor por ID |
| DELETE | `/tutores/{id}` | Exclui fisicamente o registro do tutor do banco de dados |

---

# 🐾 Módulo: Pets (`/pets`)

| Método | Endpoint | Descrição |
|---|---|---|
| POST | `/pets` | Cadastra um pet obrigatoriamente acoplado ao ID de um Tutor |
| GET | `/pets` | Listagem paginada e ordenada padrão por nome (`Page<PetResponseDTO>`) |
| GET | `/pets/busca` | Filtro parametrizado por espécie exata utilizando Cache dinâmico |
| PUT | `/pets/{id}` | Atualiza informações do animal e invalida o cache de leitura |
| DELETE | `/pets/{id}` | Remove o registro e limpa os registros de cache (`allEntries = true`) |

---

# 📄 Sintaxe do Parâmetro de Paginação

```http
GET /pets?page=0&size=5&sort=nome,asc
```

---

# 🩺 Módulo: Histórico Clínico (`/checkins` e `/eventos`)

| Método | Endpoint | Descrição |
|---|---|---|
| POST | `/checkins` | Registra uma nova entrada/admissão de um pet na recepção |
| GET | `/checkins` | Lista todo o histórico cronológico de check-ins ocorridos |
| PUT | `/checkins/{id}` | Permite editar as observações médicas anotadas na entrada |
| DELETE | `/checkins/{id}` | Exclui o registro físico de um check-in de admissão |
| POST | `/eventos` | Cria uma ocorrência de consulta clínica (diagnóstico/tratamento) |
| GET | `/eventos` | Retorna o prontuário completo de consultas longitudinais |
| PUT | `/eventos/{id}` | Edita as conclusões clínicas ou prescrições do atendimento |
| DELETE | `/eventos/{id}` | Remove um evento de consulta médica |

---

# 📂 Organização dos Artefatos de Entrega

Para fins de avaliação do corpo docente, os arquivos comprobatórios de testes e gerenciamento de configuração encontram-se estruturados na pasta `/documentos` do repositório:

- `KuraVet-API.postman_collection.json`
  - Arquivo exportado contendo o conjunto completo de requisições de teste lógico orquestradas (`POST → GET → PUT → DELETE`).

- **Diagramas de Engenharia**
  - Prints do Diagrama de Classes de Entidade e do DER (Modelo Entidade Relacionamento) sincronizados.

- **Cronograma Operacional**
  - Documento discriminando a divisão transparente de tarefas (quem fez o quê e prazos cumpridos da Sprint).

- **Link do Vídeo Demonstrativo**
  - Link público direcionando à apresentação e execução da API publicada no YouTube.

---

# 🧪 Roteiro de Evidência de Teste Sintático (Postman)

## 1️⃣ Criação do Tutor (`POST /tutores`)

Insira um JSON válido com CPF de 11 dígitos numéricos limpos.

Salve o valor da propriedade `"id"` recebido na resposta.

---

## 2️⃣ Vinculação do Pet (`POST /pets`)

Envie os dados do animal passando no campo `"idTutor"` o número obtido no passo anterior para amarrar a chave estrangeira.

---

## 3️⃣ Evolução do Fluxo Clínico (`POST /checkins` ou `POST /eventos`)

Envie requisições referenciando o `"idPet"` para popular o prontuário preventivo longitudinal do paciente.

---

## 4️⃣ Validação de Queries e Performance (`GET`)

Dispare o `GET /pets` para verificar a estrutura de paginação e use o `GET /pets/busca?especie=` repetidas vezes para observar o ganho drástico de velocidade através da resposta cacheada.

---

# 🖼️ Preview da Aplicação
<a href="https://ibb.co/hxqp4nGs"><img src="https://i.ibb.co/WNTSMdrn/imagem-7.png" alt="imagem-7" border="0"></a>
---

# 📌 Objetivo Acadêmico

Este projeto foi desenvolvido para sedimentar os critérios práticos de avaliação da disciplina de Java Advanced:

- Arquiteturas desacopladas e escaláveis
- Padronização corporativa de tráfego de dados e barramento de segurança
- Otimização de recursos de infraestrutura e performance computacional
- Alinhamento de engenharia com as demandas técnicas do Challenge de 2026

---

# 🐾 KuraVet

### Transformando o cuidado veterinário em uma jornada contínua, preventiva e inteligente.
