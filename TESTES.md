# Estratégia e Execução dos Testes — Gerenciador de Prazos Processuais

Este documento orienta **avaliadores técnicos** sobre a cobertura de testes do desafio Attus: o que foi testado, em qual camada, com quais ferramentas e como reproduzir os resultados localmente.

---

## 1. Visão geral

| Camada | Ferramenta | Quantidade de cenários | Banco de dados |
|--------|------------|------------------------|----------------|
| **Backend — domínio** | JUnit 5 | 10 (inclui parametrizados) | Não |
| **Backend — aplicação** | JUnit 5 + Mockito | 1 | Não (mock) |
| **Backend — integração HTTP** | JUnit 5 + Spring Boot Test + RestAssured | 3 | PostgreSQL (`docker compose`) |
| **Frontend — utilitários** | Vitest | 2 | Não |
| **Frontend — UI / formulário** | Vitest + Testing Library + user-event | 3 | Não (API mockada) |

**Total aproximado:** 19 execuções de teste por rodada completa (backend + frontend), sem contar repetição do módulo de integração no ciclo `mvn verify` (ver seção 3.2).

---

## 2. Como executar

### 2.1. Pré-requisitos

- **JDK 21** e **Maven 3.9+**
- **Node.js 20+** e `npm install` em `frontend/`
- **Docker Desktop** com PostgreSQL do projeto em execução (obrigatório para testes de integração do backend)

```powershell
. .\config\ambiente.ps1
docker compose up -d
```

### 2.2. Comando único (backend + frontend)

```powershell
.\scripts\testar-tudo.ps1
```

Equivale a `mvn verify` na raiz e `npm test` em `frontend/`.

### 2.3. Comandos separados

**Backend:**

```powershell
. .\config\ambiente.ps1
mvn test          # testes unitários (Surefire)
mvn verify        # test + integração (Failsafe na fase verify)
```

**Frontend:**

```powershell
cd frontend
npm test          # execução única (CI)
npm run test:watch  # modo interativo durante desenvolvimento
```

### 2.4. Relatórios Maven

Após `mvn test` ou `mvn verify`:

- `target/surefire-reports/` — resultados dos testes `*Test.java`
- `target/failsafe-reports/` — resultados dos testes `*IntegracaoTest.java` / `*IT.java`

---

## 3. Backend (Java)

### 3.1. Organização no build Maven

| Plugin | Fase | Padrão de classes |
|--------|------|-------------------|
| **maven-surefire-plugin** | `test` | `**/*Test.java`, `**/*Tests.java` |
| **maven-failsafe-plugin** | `integration-test` + `verify` | `**/*IT.java`, `**/*IntegracaoTest.java` |

A classe `PrazoProcessualIntegracaoTest` corresponde aos **dois** padrões (`*Test` e `*IntegracaoTest`). Por isso, em `mvn verify` ela pode ser executada na fase `test` (Surefire) e novamente na fase `integration-test` (Failsafe). O comportamento é redundante, mas garante que o fluxo HTTP seja validado antes do empacotamento final.

### 3.2. Perfil e infraestrutura de teste

Arquivo: `src/test/resources/application-test.yml`

- Conecta ao mesmo PostgreSQL do ambiente local (`localhost:5432`, banco `attus_prazos`).
- Flyway ativo — schema criado pela migração `V1__criar_tabela_prazo_processual.sql`.
- Hibernate em modo `validate` (não recria tabelas).
- Logging: `root` em WARN para reduzir ruído; pacote `ai.attus.prazos` permanece em INFO.

**Não** há Testcontainers neste projeto: a integração depende do container Docker já definido em `docker-compose.yml`.

---

### 3.3. Testes unitários — domínio

#### `ValidadorNumeroCnjTest`

**Arquivo:** `src/test/java/ai/attus/prazos/dominio/servico/ValidadorNumeroCnjTest.java`  
**Foco:** formato CNJ `NNNNNNN-DD.AAAA.J.TR.OOOO` (regra de negócio jurídica).

| Método | Cenário | Resultado esperado |
|--------|---------|-------------------|
| `deveAceitarNumeroCnjValido` | `0001234-56.2024.8.26.0100` | `ehValido` = true |
| `deveRejeitarNumeroCnjInvalido` (parametrizado) | vazio, incompleto, letras, dígito extra | `ehValido` = false |

Valida a **regex** centralizada em `ValidadorNumeroCnj`, reutilizada por `PrazoProcessual` e pelo frontend.

#### `PrazoProcessualTest`

**Arquivo:** `src/test/java/ai/attus/prazos/dominio/modelo/PrazoProcessualTest.java`  
**Foco:** factory `PrazoProcessual.criar()` e exceções `RegraNegocioException`.

| Método | Cenário | Resultado esperado |
|--------|---------|-------------------|
| `deveCriarPrazoComDadosValidos` | CNJ, descrição ≥ 10 chars, vencimento futuro | Sem exceção |
| `deveRejeitarDataVencimentoNoPassado` | Data = ontem | `RegraNegocioException` |
| `deveRejeitarDescricaoCurta` | Descrição "curta" | `RegraNegocioException` |
| `deveRejeitarNumeroCnjInvalido` | Número "invalido" | `RegraNegocioException` |

Garante que regras de negócio permanecem na **camada de domínio**, independentes de Spring ou JPA.

---

### 3.4. Testes unitários — aplicação (caso de uso)

#### `CriarPrazoCasoUsoTest`

**Arquivo:** `src/test/java/ai/attus/prazos/aplicacao/casouso/CriarPrazoCasoUsoTest.java`  
**Ferramentas:** `@ExtendWith(MockitoExtension.class)`, `@Mock` no repositório, `@InjectMocks` no caso de uso.

| Método | Cenário | Resultado esperado |
|--------|---------|-------------------|
| `deveCriarPrazoComSucesso` | Comando válido; repositório devolve a entidade salva | `PrazoResposta` com `id` não nulo e CNJ correto |

**O que valida:** orquestração do caso de uso (criação no domínio + chamada ao repositório).  
**O que não valida:** HTTP, JPA, Flyway ou logs (isolado com mock).

---

### 3.5. Testes de integração — API REST

#### `PrazoProcessualIntegracaoTest`

**Arquivo:** `src/test/java/ai/attus/prazos/apresentacao/controlador/PrazoProcessualIntegracaoTest.java`  
**Ferramentas:**

- `@SpringBootTest(webEnvironment = RANDOM_PORT)` — sobe o contexto Spring completo.
- **RestAssured** — chamadas HTTP reais contra a porta aleatória.
- **PostgreSQL** — persistência real (pré-requisito: `docker compose up -d`).

| Método | Cenário | Endpoints / asserts |
|--------|---------|---------------------|
| `deveCriarListarBuscarAtualizarEExcluirPrazo` | Fluxo CRUD completo | `POST` 201 → `GET` lista → `GET` por id → `PUT` 200 status CONCLUIDO (com `versao`) → `DELETE` 204 → `GET` 404 |
| `deveRetornarErroValidacaoParaCnjInvalido` | CNJ inválido no POST | HTTP 400, `title` = "Regra de negócio violada" (RFC 7807) |
| `deveRetornar404ParaIdInexistente` | UUID aleatório | HTTP 404 |

**O que valida:**

- Controllers, validação, casos de uso, adaptador JPA, Flyway e **soft delete** (recurso some após DELETE).
- Contrato JSON básico (`id`, `numeroProcesso`, `status`, `versao`, paginação `content`).

**O que não valida explicitamente:**

- Concorrência otimista (409) — cenário descrito em `INCIDENT_ANALYSIS.md`, sem teste automatizado dedicado.
- Paginação/ordenação além de “lista com pelo menos 1 item”.
- Cabeçalho `X-Correlation-Id` (coberto indiretamente em execução manual; ver `FUNCIONAMENTO_LOG.md`).

---

## 4. Frontend (React + TypeScript)

### 4.1. Configuração

- **Runner:** Vitest 2.x (`npm test` = `vitest run`).
- **Ambiente DOM:** `jsdom`.
- **Setup global:** `frontend/src/test/setup.ts` — importa `@testing-library/jest-dom`.
- **Config:** bloco `test` em `frontend/vite.config.ts`.

### 4.2. `cnj.test.ts` — utilitários

**Arquivo:** `frontend/src/test/cnj.test.ts`  
**Alvo:** `util/cnj.ts` (máscara e validação alinhadas ao backend).

| Caso | Verificação |
|------|-------------|
| Número CNJ válido | `numeroCnjEhValido` retorna `true` |
| Máscara progressiva | `aplicarMascaraCnj` insere hífen/pontos ao digitar dígitos |

### 4.3. `FormularioPrazo.test.tsx` — componente e API

**Arquivo:** `frontend/src/test/FormularioPrazo.test.tsx`  
**Alvo:** `componentes/ModalPrazo.tsx`  
**Stack:** React Testing Library, `user-event`, **Zod** (via `react-hook-form` + `@hookform/resolvers`).

O módulo `servicos/apiPrazos` é **mockado** (`vi.mock`) — não exige backend rodando.

| Caso | Verificação |
|------|-------------|
| Renderização | Título "Novo prazo processual" e campos CNJ, descrição, data |
| Validação CNJ | CNJ "123" → mensagem "Formato CNJ inválido"; `criarPrazo` **não** é chamado |
| Submissão válida | Dados corretos → `criarPrazo` chamado com payload esperado |

**O que valida:** UX do formulário, integração RHF + Zod, contrato da chamada POST.  
**O que não valida:** listagem (`TabelaPrazos`), edição, exclusão, toasts reais ou proxy Vite.

---

## 5. Matriz de cobertura vs. requisitos do desafio

| Requisito do desafio | Cobertura automatizada |
|----------------------|-------------------------|
| Validação CNJ | Domínio, integração POST 400, frontend util + formulário |
| Data de vencimento não retroativa | Domínio (`PrazoProcessualTest`) |
| Descrição 10–255 caracteres | Domínio (mínimo); Bean Validation no HTTP (implícito no integrado) |
| CRUD REST | Integração (fluxo completo) |
| Soft delete | Integração (404 após DELETE) |
| Paginação | Integração (lista retorna `content`) |
| Status CONCLUIDO na atualização | Integração (`PUT`) |
| Concorrência (`@Version` / 409) | Implementado; **sem teste automatizado** |
| Logs em operações | Verificado manualmente / `FUNCIONAMENTO_LOG.md` |
| Dashboard / destaque vencidos | **Sem teste E2E** (apenas manual) |
| Toasts de erro da API | **Sem teste** (mock não cobre fluxo de erro HTTP) |

---

## 6. Pirâmide de testes (como apresentado ao revisor)

```
                    ┌─────────────────────┐
                    │  Integração HTTP    │  3 cenários (RestAssured + Postgres)
                    │  PrazoProcessual    │
                    │  IntegracaoTest     │
                    └──────────┬──────────┘
                               │
              ┌────────────────┴────────────────┐
              │   Caso de uso (Mockito)         │  1 cenário
              │   CriarPrazoCasoUsoTest         │
              └────────────────┬────────────────┘
                               │
    ┌──────────────────────────┴──────────────────────────┐
    │  Domínio (JUnit puro)                                  │  10 cenários
    │  ValidadorNumeroCnjTest + PrazoProcessualTest         │
    └──────────────────────────┬──────────────────────────┘
                               │
         ┌─────────────────────┴─────────────────────┐
         │  Frontend — Vitest (isolado / mock API)  │  5 cenários
         │  cnj.test.ts + FormularioPrazo.test.tsx  │
         └───────────────────────────────────────────┘
```

A base da pirâmide concentra **regras de negócio** (domínio + validação CNJ no cliente). O topo valida o **fluxo ponta a ponta** no servidor; o frontend testa **comportamento de UI** sem subir a API.

---

## 7. Boas práticas adotadas

- Nomenclatura de métodos em **português** (`deveCriarPrazoComSucesso`), facilitando leitura pelo avaliador.
- Domínio testado **sem Spring** — feedback rápido e foco em regras.
- Integração usa **banco real** (mesmo do desenvolvimento), evitando divergência de dialect SQL.
- Frontend mocka HTTP — testes rápidos e estáveis em CI sem dependência do backend.
- Perfil `test` dedicado (`application-test.yml`) separado do `application.yml` de produção local.

---

## 8. Limitações e melhorias sugeridas

| Lacuna | Sugestão para evolução |
|--------|-------------------------|
| Sem teste de concorrência (409) | `PUT` simultâneo com versões diferentes |
| Casos de uso `Atualizar`, `Excluir`, `Listar` sem unit test | Mockito nos respectivos casos de uso |
| Frontend sem teste da tabela / destaque vermelho | Testing Library em `TabelaPrazos` com datas fixas |
| Sem E2E (Playwright/Cypress) | Fluxo login + CRUD no navegador |
| Integração executada 2× no `verify` | Renomear para `*IT.java` apenas no Failsafe, ou excluir do Surefire |
| `testar-tudo.ps1` | Atualizar comentário (não usa mais Testcontainers) |

---

## 9. Referência de arquivos

| Arquivo | Tipo |
|---------|------|
| `src/test/java/.../ValidadorNumeroCnjTest.java` | Unitário — domínio |
| `src/test/java/.../PrazoProcessualTest.java` | Unitário — domínio |
| `src/test/java/.../CriarPrazoCasoUsoTest.java` | Unitário — aplicação |
| `src/test/java/.../PrazoProcessualIntegracaoTest.java` | Integração — API |
| `src/test/resources/application-test.yml` | Configuração de teste |
| `frontend/src/test/cnj.test.ts` | Unitário — util |
| `frontend/src/test/FormularioPrazo.test.tsx` | Componente — UI |
| `frontend/src/test/setup.ts` | Setup Vitest |
| `scripts/testar-tudo.ps1` | Automação local |

Documentação relacionada:

- **[README.md](README.md)** — como subir o ambiente e rodar testes
- **[FUNCIONAMENTO_LOG.md](FUNCIONAMENTO_LOG.md)** — logs observáveis durante testes de integração
- **[TECHNICAL_NOTE.md](TECHNICAL_NOTE.md)** — trade-offs arquiteturais

---

*Documento elaborado para revisores técnicos do desafio Attus — Gerenciador de Prazos Processuais.*
