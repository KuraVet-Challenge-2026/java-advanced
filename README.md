# KuraVet - Ecossistema de Saúde Animal Contínua

## 📋 Sobre o Projeto
O **KuraVet** é uma solução tecnológica desenvolvida para o **Challenge 2026** da FIAP, em parceria com a **Clyvo Vet**. O objetivo do projeto é transformar a jornada de saúde animal, migrando de um modelo reativo e episódico para um ecossistema de cuidado contínuo, preventivo e inteligente.

A plataforma permite que tutores e clínicas veterinárias acompanhem o histórico clínico longitudinal do pet, gerenciem check-ins e monitorem indicadores de vitalidade, garantindo maior fidelização para as clínicas e melhor qualidade de vida para os animais.

---

# 📊 Diagrama de Classe UML

<p align="center">
  <img src="https://i.ibb.co/wZDTMbKg/mermaid-diagram-1.png" alt="Diagrama de Classe UML KuraVet" width="900">
</p>

---

## 👥 Equipe (Squad)

- **Pedro Henrique Luiz Alves Duarte**
- **Guilherme Macedo Martins**
- **Henrique Martins**

**Turma:** 2TDSPO

---

## 🚀 Tecnologias Utilizadas

- **Java 17**
- **Spring Boot 4.0.6**
- **Spring Data JPA** (Persistência de Dados)
- **Oracle SQL / H2 Database** (Banco de Dados Relacional)
- **Bean Validation** (Validação de Dados)
- **Spring Cache** (Otimização de Performance)
- **Springdoc OpenAPI (Swagger)** (Documentação da API)
- **Maven** (Gerenciador de Dependências)

---

## ⚙️ Funcionalidades e Requisitos Implementados

A API foi desenvolvida seguindo os padrões **RESTful** e cumpre todos os requisitos técnicos da disciplina de **Java Advanced**:

1. **CRUD Completo**
   - Gerenciamento de Tutores
   - Gerenciamento de Pets
   - Gerenciamento de Check-ins
   - Gerenciamento de Eventos de Consulta

2. **Validação de Dados**
   - Uso de `@Valid`
   - Bean Validation:
     - `@NotBlank`
     - `@CPF`
     - `@Email`

3. **Paginação e Ordenação**
   - Implementada na listagem de Pets para garantir escalabilidade.

4. **Busca com Parâmetros**
   - Filtro de busca de Pets por espécie.

5. **Cache**
   - Utilização de:
     - `@Cacheable`
     - `@CacheEvict`

6. **Tratamento Global de Erros**
   - `GlobalExceptionHandler`
   - Retornos HTTP padronizados:
     - 400
     - 404
     - 500

7. **Documentação Interativa**
   - Swagger UI configurado para testes rápidos.

---

## 🛠️ Como Executar o Projeto

### 📌 Pré-requisitos

- Java 17 ou superior
- Maven instalado

---

### ▶️ Passo a Passo

#### 1. Clone o repositório

```bash
git clone https://github.com/KuraVet-Challenge-2026/java-advanced.git
```

---

#### 2. Navegue até a pasta do projeto

```bash
cd java-advanced
```

---

#### 3. Instale as dependências

```bash
mvn clean install
```

---

#### 4. Execute a aplicação

```bash
mvn spring-boot:run
```

---

#### 5. Acesse a documentação Swagger

```bash
http://localhost:8080/swagger-ui/index.html
```

---

## 🔍 Documentação da API (Principais Endpoints)

# 👤 Tutores

| Método | Endpoint | Descrição |
|---|---|---|
| POST | `/tutores` | Cadastra um novo tutor |
| GET | `/tutores` | Lista todos os tutores |
| PUT | `/tutores/{id}` | Atualiza dados do tutor |
| DELETE | `/tutores/{id}` | Remove um tutor |

---

# 🐾 Pets

| Método | Endpoint | Descrição |
|---|---|---|
| POST | `/pets` | Cadastra um pet vinculado a um tutor |
| GET | `/pets` | Lista pets |
| GET | `/pets/busca?especie=Gato` | Filtra pets por espécie |
| PUT | `/pets/{id}` | Atualiza dados do pet |
| DELETE | `/pets/{id}` | Remove um pet |

### 📄 Exemplo de Paginação

```http
GET /pets?page=0&size=5&sort=nome,asc
```

---

# 🩺 Histórico e Consultas

| Método | Endpoint | Descrição |
|---|---|---|
| POST | `/checkins` | Registra um novo check-in |
| GET | `/checkins` | Lista histórico de check-ins |
| POST | `/eventos` | Registra uma nova consulta clínica |
| GET | `/eventos` | Lista histórico de consultas |

---

## 📂 Estrutura de Documentos para Avaliação

Os arquivos abaixo encontram-se na pasta `/documentos` ou `/docs` na raiz do repositório.

### 📁 Arquivos Entregáveis

1. **Postman Collection**
   - `KuraVet-API.postman_collection.json`

2. **Diagramas**
   - Diagrama de Classes
   - DER (Modelo Entidade Relacionamento)

3. **Cronograma**
   - Documento detalhando divisão de tarefas e prazos.

4. **Vídeo Pitch / Demonstração**
   - Link do YouTube do projeto.

---

## 🧪 Instruções para Testes (Postman)

Para validar corretamente o funcionamento da API, siga esta ordem lógica:

### 1️⃣ Cadastre um Tutor

```http
POST /tutores
```

---

### 2️⃣ Cadastre um Pet vinculado ao Tutor

```http
POST /pets
```

---

### 3️⃣ Registre um Check-in ou Consulta

```http
POST /checkins
POST /eventos
```

---

### 4️⃣ Teste listagens e filtros

```http
GET /pets
GET /pets/busca
GET /checkins
GET /eventos
```

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

## Transformando o cuidado veterinário em uma jornada contínua e inteligente.
