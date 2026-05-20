# 🐾 KuraVet - Ecossistema de Saúde Animal Contínua

## 📋 Sobre o Projeto

O **KuraVet** é uma solução tecnológica desenvolvida para o **Challenge 2026 da FIAP**, em parceria com a **Clyvo Vet**.

O objetivo do projeto é transformar a jornada de saúde animal, migrando de um modelo reativo e episódico para um ecossistema de cuidado contínuo, preventivo e inteligente.

A plataforma permite que tutores e clínicas veterinárias acompanhem o histórico clínico longitudinal do pet, gerenciem check-ins e monitorem indicadores de vitalidade, garantindo maior fidelização para as clínicas e melhor qualidade de vida para os animais.

---

# 📊 Diagrama de Classe UML

---

# 👥 Equipe (Squad)

- **Pedro Henrique Luiz Alves Duarte**
- **Guilherme Macedo Martins**
- **Henrique Martins**

**Turma:** 2TDSPO

---

# 🚀 Tecnologias Utilizadas

- Java 17
- Spring Boot 4.0.6
- Spring Data JPA (Persistência de Dados)
- Oracle SQL / H2 Database (Banco de Dados Relacional)
- Bean Validation (Validação de Dados)
- Spring Cache (Otimização de Performance)
- Springdoc OpenAPI (Swagger) (Documentação da API)
- Maven (Gerenciador de Dependências)

---

# ⚙️ Funcionalidades e Requisitos Implementados

A API foi desenvolvida seguindo os padrões RESTful e cumpre todos os requisitos técnicos da disciplina de Java Advanced:

## ✅ CRUD Completo

- Gerenciamento de Tutores
- Gerenciamento de Pets
- Gerenciamento de Check-ins
- Gerenciamento de Eventos de Consulta

---

## ✅ Validação de Dados

Uso de `@Valid`

### Bean Validation

- `@NotBlank`
- `@CPF`
- `@Email`

---

## ✅ Paginação e Ordenação

Implementada na listagem de Pets para garantir escalabilidade.

---

## ✅ Busca com Parâmetros

Filtro de busca de Pets por espécie.

---

## ✅ Cache

Utilização de:

- `@Cacheable`
- `@CacheEvict`

---

## ✅ Tratamento Global de Erros

`GlobalExceptionHandler`

### Retornos HTTP padronizados

- `400`
- `404`
- `500`

---

## ✅ Documentação Interativa

Swagger UI configurado para testes rápidos.

---

# 🛠️ Como Executar o Projeto

## 📌 Pré-requisitos

- Java 17 ou superior
- Maven instalado

---

## ▶️ Passo a Passo

### 1. Clone o repositório

```bash
git clone https://github.com/KuraVet-Challenge-2026/java-advanced.git
```

### 2. Navegue até a pasta do projeto

```bash
cd java-advanced
```

### 3. Instale as dependências

```bash
mvn clean install
```

### 4. Execute a aplicação

```bash
mvn spring-boot:run
```

### 5. Acesse a documentação Swagger

```bash
http://localhost:8080/swagger-ui/index.html
```

---

# 🔍 Documentação da API (Principais Endpoints)

# 👤 Tutores

| Método | Endpoint | Descrição |
|---|---|---|
| POST | `/tutores` | Cadastra um novo tutor |
| GET | `/tutores` | Lista todos os tutores |
| PUT | `/tutores/{id}` | Atualiza dados de um tutor existente |
| DELETE | `/tutores/{id}` | Remove um tutor do sistema |

---

# 🐾 Pets

| Método | Endpoint | Descrição |
|---|---|---|
| POST | `/pets` | Cadastra um pet vinculado a um tutor |
| GET | `/pets` | Lista pets cadastrados (suporta paginação) |
| GET | `/pets/busca?especie=Gato` | Filtra pets por espécie |
| PUT | `/pets/{id}` | Atualiza dados de um pet existente |
| DELETE | `/pets/{id}` | Remove um pet do sistema |

---

# 📄 Exemplo de Paginação

```http
GET /pets?page=0&size=5&sort=nome,asc
```

---

# 🩺 Histórico e Consultas

| Método | Endpoint | Descrição |
|---|---|---|
| POST | `/checkins` | Registra um novo check-in de um pet |
| GET | `/checkins` | Lista histórico de check-ins |
| PUT | `/checkins/{id}` | Atualiza observações de um check-in |
| DELETE | `/checkins/{id}` | Remove um check-in do sistema |
| POST | `/eventos` | Registra uma nova consulta clínica |
| GET | `/eventos` | Lista histórico de consultas |
| PUT | `/eventos/{id}` | Atualiza diagnóstico/tratamento de uma consulta |
| DELETE | `/eventos/{id}` | Remove o registro de uma consulta |

---

# 📂 Estrutura de Documentos para Avaliação

Os arquivos abaixo encontram-se na pasta `/documentos` ou `/docs` na raiz do repositório.

## 📁 Arquivos Entregáveis

### 📌 Postman Collection

- `KuraVet-API.postman_collection.json`

### 📌 Diagramas

- Diagrama de Classes
- DER (Modelo Entidade Relacionamento)

### 📌 Cronograma

Documento detalhando divisão de tarefas e prazos.

### 📌 Vídeo Pitch / Demonstração

Link do YouTube do projeto.

---

# 🧪 Instruções para Testes (Postman)

Para validar corretamente o funcionamento da API, siga esta ordem lógica:

## 1️⃣ Cadastre um Tutor

```http
POST /tutores
```

## 2️⃣ Cadastre um Pet vinculado ao Tutor

```http
POST /pets
```

## 3️⃣ Registre um Check-in ou Consulta

```http
POST /checkins
POST /eventos
```

## 4️⃣ Teste as atualizações, listagens e filtros

```http
PUT /tutores/{id}
GET /pets
GET /pets/busca
GET /checkins
GET /eventos
DELETE /pets/{id}
```

---

# 🖼️ Preview da Aplicação

<p align="center">
  <a href="https://ibb.co/B2q6ttvw">
    <img src="https://i.ibb.co/qYkgddhx/imagem.png" alt="Preview da aplicação KuraVet" border="0" width="100%">
  </a>
</p>

---

# 📌 Objetivo Acadêmico

Este projeto foi desenvolvido para aplicar conceitos avançados de:

- APIs RESTful
- Spring Boot
- JPA/Hibernate
- Arquitetura em Camadas
- Persistência Relacional
- Tratamento de Exceções
- Cache e Performance
- Documentação de APIs
- Boas práticas em Java

---

# 🐾 KuraVet

### Transformando o cuidado veterinário em uma jornada contínua e inteligente.
