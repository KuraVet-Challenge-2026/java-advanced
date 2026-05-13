# KuraVet - Ecossistema de Saúde Animal Contínua

## 📋 Sobre o Projeto
O **KuraVet** é uma solução tecnológica desenvolvida para o **Challenge 2026** da FIAP, em parceria com a **Clyvo Vet**. O objetivo do projeto é transformar a jornada de saúde animal, migrando de um modelo reativo e episódico para um ecossistema de cuidado contínuo, preventivo e inteligente.

A plataforma permite que tutores e clínicas veterinárias acompanhem o histórico clínico longitudinal do pet, gerenciem check-ins e monitorem indicadores de vitalidade, garantindo maior fidelização para as clínicas e melhor qualidade de vida para os animais.

---

## 👥 Equipe (Squad)
* **Pedro Henrique Luiz Alves Duarte**
* **Guilherme Macedo Martins**
* **Henrique Martins**

**Turma:** 2TDSPO

---

## 🚀 Tecnologias Utilizadas
* **Java 17**
* **Spring Boot 4.0.6**
* **Spring Data JPA** (Persistência de Dados)
* **Oracle SQL / H2 Database** (Banco de Dados Relacional)
* **Bean Validation** (Validação de Dados)
* **Spring Cache** (Otimização de Performance)
* **Springdoc OpenAPI (Swagger)** (Documentação da API)
* **Maven** (Gerenciador de Dependências)

---

## ⚙️ Funcionalidades e Requisitos Implementados
A API foi desenvolvida seguindo os padrões **RESTful** e cumpre todos os requisitos técnicos da disciplina de **Java Advanced**:

1.  **CRUD Completo:** Gerenciamento de Tutores, Pets, Check-ins e Eventos de Consulta.
2.  **Validação de Dados:** Uso de `@Valid` e anotações do Bean Validation (ex: `@NotBlank`, `@CPF`, `@Email`).
3.  **Paginação e Ordenação:** Implementada na listagem de Pets para garantir escalabilidade.
4.  **Busca com Parâmetros:** Filtro de busca de Pets por espécie.
5.  **Cache:** Otimização de consultas repetitivas utilizando `@Cacheable` e `@CacheEvict`.
6.  **Tratamento de Erros:** `GlobalExceptionHandler` para retornos HTTP padronizados (400, 404, etc).
7.  **Documentação Interativa:** Swagger UI configurado para testes rápidos.

---

## 🛠️ Como Executar o Projeto

### Pré-requisitos
* Java 17 ou superior.
* Maven instalado.

### Passo a Passo
1.  Clone o repositório:
    ```bash
    git clone https://github.com/KuraVet-Challenge-2026/java-advanced.git
    ```
2.  Navegue até a pasta do projeto e instale as dependências:
    ```bash
    mvn clean install
    ```
3.  Execute a aplicação:
    ```bash
    mvn spring-boot:run
    ```
4.  Acesse a documentação Swagger:
    [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

---

## 🔍 Documentação da API (Principais Endpoints)

### Tutores
* `POST /tutores` - Cadastra um novo tutor.
* `GET /tutores` - Lista todos os tutores.
* `PUT /tutores/{id}` - Atualiza dados do tutor.
* `DELETE /tutores/{id}` - Remove um tutor.

### Pets
* `POST /pets` - Cadastra um pet vinculado a um tutor.
* `GET /pets` - Lista pets (Suporta paginação: `?page=0&size=5&sort=nome,asc`).
* `GET /pets/busca` - Filtra por espécie (`?especie=Gato`).
* `PUT /pets/{id}` - Atualiza dados do pet.
* `DELETE /pets/{id}` - Remove um pet.

### Histórico e Consultas
* `POST /checkins` - Registra um novo check-in.
* `GET /checkins` - Lista histórico de check-ins.
* `POST /eventos` - Registra uma nova consulta clínica.
* `GET /eventos` - Lista histórico de consultas.

---

## 📂 Estrutura de Documentos para Avaliação
Os arquivos abaixo encontram-se na pasta `/documentos` (ou `/docs`) na raiz do repositório:

1.  **Postman Collection:** `KuraVet-API.postman_collection.json` (Importe no Postman para testar os endpoints).
2.  **Diagramas:** Prints do Diagrama de Classes e do Modelo de Entidade Relacionamento (DER).
3.  **Cronograma:** Documento detalhando a divisão de tarefas e prazos cumpridos pela equipe.
4.  **Vídeo Pitch / Demonstração:** [Link para o YouTube aqui]

---

## 🧪 Instruções para Testes (Postman)
Para validar o funcionamento, siga esta ordem lógica:
1.  Cadastre um **Tutor**.
2.  Cadastre um **Pet** usando o ID do Tutor criado.
3.  Realize um **Check-in** ou registre uma **Consulta** usando o ID do Pet.
4.  Teste as listagens e filtros de busca.
