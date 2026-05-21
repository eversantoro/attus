# Gerenciador de Prazos Processuais — Attus

Solução Full Stack para gestão de **prazos processuais** no contexto de procuradorias digitais, desenvolvida como desafio técnico da [Attus Procuradoria Digital](https://www.attus.ai/).

O sistema permite cadastrar, listar, editar e excluir (soft delete) prazos vinculados a processos no padrão **CNJ**, com validações de negócio, API documentada, interface responsiva e testes automatizados.

---

## Arquitetura

| Camada | Tecnologia |
|--------|------------|
| Backend | Java 21, Spring Boot 3.3, Clean Architecture, JPA, Flyway, PostgreSQL |
| Frontend | React 18, TypeScript, Vite, TailwindCSS, React Hook Form, Zod |
| Testes | JUnit 5, Mockito, RestAssured, Testcontainers, Vitest, Testing Library |

Pacote base Java: `ai.attus.prazos`

---

## Pré-requisitos

- **JDK 21** em `C:\Program Files\Java\jdk-21`
- **Maven 3.9+** (ex.: `C:\java\apache-maven-3.9.5`)
- **Node.js 20+** e npm
- **Docker Desktop** em execução

---

## Início rápido (recomendado)

No PowerShell, na raiz do projeto:

```powershell
# Carrega JDK 21, Maven, Docker no PATH
. .\config\ambiente.ps1

# Sobe PostgreSQL + API + Frontend
.\scripts\iniciar-ambiente.ps1
```

Para parar:

```powershell
.\scripts\parar-ambiente.ps1
```

Para rodar todos os testes:

```powershell
.\scripts\testar-tudo.ps1
```

---

## 1. Subir o banco de dados (manual)

Na raiz do projeto:

```powershell
. .\config\ambiente.ps1
docker compose up -d
```

Aguarde o healthcheck do PostgreSQL. Credenciais padrão:

| Parâmetro | Valor |
|-----------|--------|
| Host | `localhost:5432` |
| Banco | `attus_prazos` |
| Usuário | `attus` |
| Senha | `attus123` |

---

## 2. Executar o backend

```powershell
. .\config\ambiente.ps1
mvn spring-boot:run
```

A API ficará disponível em `http://localhost:8080`.

### Documentação Swagger

- **Swagger UI:** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **OpenAPI JSON:** [http://localhost:8080/api-docs](http://localhost:8080/api-docs)

### Endpoints principais

| Método | Rota | Descrição |
|--------|------|-----------|
| `POST` | `/api/v1/prazos` | Cria prazo |
| `GET` | `/api/v1/prazos` | Lista com paginação (padrão: `dataVencimento,asc`) |
| `GET` | `/api/v1/prazos/{id}` | Detalhe |
| `PUT` | `/api/v1/prazos/{id}` | Atualiza |
| `DELETE` | `/api/v1/prazos/{id}` | Soft delete |

---

## 3. Executar o frontend

```bash
cd frontend
npm install
npm run dev
```

Acesse [http://localhost:5173](http://localhost:5173). O Vite faz proxy de `/api` para o backend.

---

## 4. Executar testes

### Backend (requer PostgreSQL via `docker compose up -d`)

```powershell
. .\config\ambiente.ps1
mvn verify
```

### Frontend

```bash
cd frontend
npm test
```

---

## Estrutura do repositório

```
attus/
├── docker-compose.yml
├── pom.xml
├── README.md
├── INCIDENT_ANALYSIS.md
├── TECHNICAL_NOTE.md
├── src/main/java/ai/attus/prazos/
│   ├── dominio/
│   ├── aplicacao/
│   ├── infraestrutura/
│   └── apresentacao/
└── frontend/
```

---

## Logs e diagnóstico

- Logs estruturados (JSON) via Logback + Logstash encoder
- `correlationId` propagado no cabeçalho `X-Correlation-Id` e no MDC
- `INFO` em criação/alteração/exclusão de prazos
- `ERROR` em falhas de validação e exceções não tratadas (RFC 7807 Problem Details)

---

## Licença

Projeto de avaliação técnica — uso educacional/demonstrativo.
