# Pedido ao time do java-advanced — integração com o app mobile KuraVet

Para: time responsável pelo repositório `java-advanced` (API Spring Boot da KuraVet).
De: time do app mobile (`kuravet-app`).
Data: 2026-09-02.

Este documento é autocontido — não depende de abrir outros arquivos do repositório mobile para
ser entendido e agido. Três itens são bloqueantes para o app mobile conseguir integrar com a API
real na Sprint 3; um item é um pedido não bloqueante, com uma alternativa caso não seja atendido a
tempo.

## Contexto rápido

A API usa HTTP Basic (Spring Security) autenticando contra a tabela `USUARIO`, sem emitir token.
O app mobile decidiu usar a API Java como única fonte de identidade (abandonou uma tentativa
anterior com Firebase). Para isso funcionar, faltam dois endpoints e falta uma correção de
segurança em um endpoint já existente — detalhados abaixo.

---

## Item 1 (bloqueante) — `POST /api/auth/cadastro`

**O que falta:** hoje `POST /api/tutores` cria só a linha em `TUTOR`, sem criar o `USUARIO`
correspondente (login). Não existe nenhum jeito de um tutor novo se cadastrar e sair de lá com uma
credencial que funcione contra a própria API.

**Contrato esperado:**

Cria `Tutor` + `Usuario` (perfil `TUTOR`, associado ao tutor recém-criado) em uma única transação,
com a senha já em BCrypt.

Request:
```json
{
  "nome": "string, obrigatorio",
  "cpf": "string, obrigatorio",
  "telefone": "string, opcional",
  "email": "string, opcional, formato de e-mail",
  "endereco": "string, opcional",
  "username": "string, obrigatorio, unico",
  "senha": "string, obrigatorio, politica minima a definir (ex.: 6+ caracteres)"
}
```

Response `201 Created`:
```json
{
  "idTutor": 5,
  "nome": "Nome Completo",
  "cpf": "...",
  "telefone": "...",
  "email": "...",
  "endereco": "...",
  "dataCadastro": "2026-09-02",
  "username": "novo_username",
  "perfil": "TUTOR"
}
```
A senha nunca deve voltar na resposta, nem em texto nem em hash.

Erros esperados: `400` se `username` já existir (mensagem clara, ex. `"Nome de usuario ja em
uso."`), `400` de validação por campo no mesmo formato já usado pelo `ApiExceptionHandler` atual
(objeto `campos` com `{ nomeDoCampo: mensagem }`).

**Motivo:** é o único jeito de o app oferecer uma tela de cadastro real (regra do projeto mobile:
zero usuário fixo no código, autenticação real).

**Impacto se não vier:** o app não consegue implementar cadastro de novos tutores. Fica limitado
aos dois usuários já seedados no banco (`veterinario`/`vet123`, `tutor`/`tutor123`) — inviável para
qualquer demonstração ou avaliação além de um teste isolado.

---

## Item 2 (bloqueante) — `GET /api/auth/me`

**O que falta:** a API não emite token, então não há como o app validar uma combinação
username/senha digitada no login sem "adivinhar" chamando algum endpoint de negócio (ex.:
`GET /api/pets`) e torcer para o retorno servir de sinal de sucesso. Também não há como o app obter
o `idTutor`/`perfil` de quem está logado sem depender de outro endpoint que não foi feito para
isso.

**Contrato esperado:**

Endpoint autenticado (HTTP Basic). Retorna o perfil de quem está logado.

Response `200 OK`:
```json
{
  "idUsuario": 2,
  "username": "tutor",
  "perfil": "TUTOR",
  "idTutor": 1,
  "nomeTutor": "Ana Beatriz Souza"
}
```
(`idTutor`/`nomeTutor` nulos quando `perfil` é `VETERINARIO`.)

Erro esperado: `401` (formato padrão do Spring Security) se `Authorization` ausente ou inválido —
é exatamente essa resposta que o app usa para decidir "login falhou" vs. "login OK, salvar
credenciais".

**Motivo:** é o ponto único e correto para validar login e obter o perfil do usuário, em vez de
inferir a partir de um endpoint de negócio.

**Impacto se não vier:** o app não tem como confirmar que uma senha digitada está correta antes de
salvá-la localmente, nem como saber com segurança se quem logou é `TUTOR` ou `VETERINARIO` — a
tela de login fica sem forma confiável de validar a entrada do usuário.

---

## Item 3 (bloqueante) — correção de ownership em `TutorController`

**O que falta:** `buscarPorId`, `atualizar` e `excluir` em `TutorController`/`TutorService`
recebem um `id` de `@PathVariable` e operam sobre ele sem checar quem está autenticado. Qualquer
usuário autenticado (TUTOR ou VETERINARIO) consegue ler, alterar ou excluir os dados de **qualquer
outro tutor** só sabendo o ID — nome, CPF, telefone, e-mail e endereço de terceiros ficam
expostos, e um tutor pode inclusive apagar o cadastro de outro.

Isto é diferente de `PetController`/`ConsultaController`, que já aplicam esse filtro (comparam
`principal.getIdTutor()` com o dono do registro, na camada de service).

**Comportamento esperado da correção:**

Replicar em `TutorService` o mesmo padrão já usado em `PetService.buscarPorId` e
`ConsultaService.buscarPorId`:

- Os métodos que hoje operam livremente sobre `id` (`buscarPorId`, `atualizar`, `excluir`) devem
  receber `@AuthenticationPrincipal UsuarioPrincipal principal`.
- Quando `principal.isTutor()` for verdadeiro, validar que `principal.getIdTutor()` é igual ao `id`
  do path. Se não for, lançar `TutorNaoEncontradoException` — resposta **404**, não 403 (mesmo
  raciocínio já aplicado a Pet e Consulta: não revelar a existência de registros de terceiros).
- Quando `perfil == VETERINARIO`, manter acesso a qualquer tutor — o portal web precisa gerenciar
  todos os tutores.
- `GET /api/tutores` (listagem) tem o mesmo problema em menor grau: hoje devolve todos os tutores
  para qualquer autenticado. Idealmente: `TUTOR` recebe só o próprio registro (ou lista vazia),
  `VETERINARIO` recebe a lista completa.

**Motivo:** falha de controle de acesso — não é um detalhe de polimento, é o tipo de coisa que
reprova em qualquer avaliação de segurança e viola diretamente o requisito de "proteção de rotas
com base no perfil do usuário" que a própria disciplina de Java Advanced pede.

**Impacto se não vier:** o app mobile mitiga isso do próprio lado (só oferece UI para o usuário
mexer no próprio `idTutor`), mas isso não corrige a falha na API — ela continua explorável por
qualquer cliente HTTP direto (Postman, curl, outro app), fora do controle do time mobile.

---

## Item 4 (não bloqueante) — `GET /api/veterinarios`

**O que falta:** não existe nenhum endpoint (`VeterinarioController`) que liste veterinários. A
entidade `Veterinario` e o `VeterinarioRepository` já existem, só falta o endpoint REST.

**Contrato esperado (sugestão simples, somente leitura):**

```
GET /api/veterinarios          -> lista
GET /api/veterinarios/{id}     -> um veterinário
```

Response sugerida (mesmo espírito de `TutorResponseDTO`/`PetResponseDTO` — sem expor a entidade
JPA diretamente):
```json
{ "idVeterinario": 2, "nome": "Dr. Rafael Andrade", "especialidade": "Dermatologia" }
```
(campos exatos ficam a critério do backend, conforme o que `Veterinario` já tiver.)

**Motivo:** `POST /api/consultas/solicitacoes` exige um `idVeterinario` válido para solicitar uma
teleconsulta. Sem uma lista real para o usuário escolher, o app não tem como montar esse formulário
sem fixar um ID no código — o que violaria a regra do projeto mobile de nunca usar dado mockado/
fixo como substituto de dado real da API.

**Impacto se não vier:** a funcionalidade de Teleconsulta (solicitar consulta) fica de fora da
Sprint 3 do app mobile. **Não é bloqueante** porque o app já tem duas outras funcionalidades com
CRUD completo e viável sem depender deste endpoint (Pet e Perfil do Tutor) — este item só amplia o
escopo, não é pré-requisito para a entrega mínima do mobile.

---

## Resumo

| # | Item | Bloqueante | Se não vier a tempo |
|---|---|---|---|
| 1 | `POST /api/auth/cadastro` | Sim | App fica sem cadastro de novos tutores |
| 2 | `GET /api/auth/me` | Sim | App não valida login nem obtém perfil com segurança |
| 3 | Correção de ownership em `TutorController` | Sim | Falha de controle de acesso permanece explorável por qualquer cliente HTTP |
| 4 | `GET /api/veterinarios` | Não | App segue só com Pet + Perfil do Tutor; Teleconsulta fica fora da Sprint 3 |
