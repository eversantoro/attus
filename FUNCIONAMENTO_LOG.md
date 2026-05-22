# Funcionamento dos Logs — Gerenciador de Prazos Processuais

Este documento descreve como a observabilidade mínima foi implementada no **backend** do projeto, para apoiar a análise técnica do desafio Attus. O frontend **não** possui camada de logging estruturado; o feedback ao usuário é feito via toasts (react-hot-toast).

---

## 1. Stack utilizada

| Componente | Papel |
|------------|--------|
| **SLF4J** | API de logging (interfaces usadas no código Java) |
| **Logback** | Implementação concreta (configurada pelo Spring Boot) |
| **Logstash Logback Encoder** | Serialização dos eventos em **JSON** no console |
| **MDC** (Mapped Diagnostic Context) | Propagação do `correlationId` por requisição HTTP |

Dependência no `pom.xml`:

```xml
<dependency>
    <groupId>net.logstash.logback</groupId>
    <artifactId>logstash-logback-encoder</artifactId>
</dependency>
```

Não há uso de `System.out.println` para diagnóstico de negócio ou de erros.

---

## 2. Configuração

### 2.1. `logback-spring.xml`

Arquivo: `src/main/resources/logback-spring.xml`

- **Appender:** `CONSOLE` (saída padrão do processo Java).
- **Formato:** JSON via `LogstashEncoder`.
- **Campos fixos:** `aplicacao` = nome definido em `spring.application.name` (`gerenciador-prazos-processuais`).
- **Campo dinâmico do MDC:** `correlationId` (quando presente na thread da requisição).
- **Nível global:** `INFO` no root logger.

### 2.2. `application.yml`

```yaml
logging:
  level:
    root: INFO
    ai.attus.prazos: INFO
```

- Todo código do pacote `ai.attus.prazos` registra eventos a partir de **INFO**.
- Bibliotecas de terceiros (Spring, Hibernate, etc.) seguem o nível `root`; em testes de integração costumam aparecer principalmente em **WARN** e **ERROR**.

### 2.3. Perfil de teste

Em `src/test/resources/application-test.yml` o nível de `root` é **WARN**, reduzindo ruído durante `mvn verify`, mantendo `ai.attus.prazos` em **INFO** quando aplicável.

---

## 3. Correlation ID (rastreio por requisição)

### 3.1. Filtro HTTP

Classe: `ai.attus.prazos.infraestrutura.configuracao.FiltroCorrelationId`

| Etapa | Comportamento |
|-------|----------------|
| Entrada | Lê o cabeçalho `X-Correlation-Id` |
| Ausência do cabeçalho | Gera um UUID |
| Durante o request | Armazena o valor no MDC com a chave `correlationId` |
| Resposta | Repete o mesmo ID no cabeçalho `X-Correlation-Id` da resposta HTTP |
| Finalização | Remove a chave do MDC no bloco `finally` (evita “vazamento” entre requisições na mesma thread do pool) |

O filtro possui `@Order(Ordered.HIGHEST_PRECEDENCE)` para executar **antes** dos controllers.

### 3.2. Uso em erros da API

Classe: `ai.attus.prazos.apresentacao.excecao.ManipuladorExcecaoGlobal`

- O `correlationId` é lido do MDC e incluído no corpo da resposta no padrão **RFC 7807** (Problem Details), na propriedade `correlationId`.
- Permite ao analista ou ao frontend correlacionar o erro exibido ao usuário com a linha exata no log do servidor.

**Exemplo de chamada com ID fixo:**

```http
POST /api/v1/prazos HTTP/1.1
X-Correlation-Id: analise-revisor-001
Content-Type: application/json
```

O mesmo valor deve aparecer no JSON do log e na resposta de erro (se houver falha).

---

## 4. O que é registrado e em qual nível

### 4.1. Nível INFO — operações de negócio bem-sucedidas

Logs emitidos nos **casos de uso** após persistência bem-sucedida:

| Classe | Evento | Campos principais |
|--------|--------|-------------------|
| `CriarPrazoCasoUso` | Prazo criado | `id`, `numeroProcesso` |
| `AtualizarPrazoCasoUso` | Prazo alterado | `id`, `status` |
| `ExcluirPrazoCasoUso` | Soft delete | `id` |

**Não há** log INFO para:

- Listagem paginada (`GET /api/v1/prazos`)
- Consulta por ID (`GET /api/v1/prazos/{id}`)

Essa escolha reduz volume de log em endpoints de leitura frequente.

### 4.2. Nível ERROR — falhas tratadas globalmente

Classe: `ManipuladorExcecaoGlobal` (`@RestControllerAdvice`)

| Situação | HTTP | Log ERROR | Stack trace no log |
|----------|------|-----------|-------------------|
| `RegraNegocioException` (CNJ inválido, data retroativa, descrição, etc.) | 400 | Sim — mensagem de negócio | **Não** (apenas `ex.getMessage()`) |
| `MethodArgumentNotValidException` (Bean Validation nos DTOs) | 400 | Sim — mapa `erros` por campo | **Sim** |
| `ConcorrenciaPrazoException` / `OptimisticLockingFailureException` | 409 | Sim | **Sim** |
| Qualquer outra `Exception` não mapeada | 500 | Sim | **Sim** |
| `PrazoNaoEncontradoException` | 404 | **Não** | — |

Para **404**, a API retorna Problem Details adequado, mas **sem** linha de log dedicada — decisão de não poluir o log com consultas a recursos inexistentes.

---

## 5. Formato de saída (exemplo)

Cada linha no console é um JSON. Exemplo ilustrativo após criação de prazo:

```json
{
  "@timestamp": "2026-05-21T17:21:22.3806102-03:00",
  "@version": "1",
  "message": "Prazo processual criado | id=18c222b3-7839-49a1-8fec-f75523605199 | numeroProcesso=0001234-56.2024.8.26.0100",
  "logger_name": "ai.attus.prazos.aplicacao.casouso.CriarPrazoCasoUso",
  "thread_name": "http-nio-8080-exec-3",
  "level": "INFO",
  "level_value": 20000,
  "correlationId": "3e2ba4a6-862e-46e3-b02a-770b19eb57f7",
  "aplicacao": "gerenciador-prazos-processuais"
}
```

Exemplo de erro de validação de negócio:

```json
{
  "level": "ERROR",
  "message": "Falha de validação de negócio | correlationId=492b2c46-aa1a-4694-ac91-ce01a41d5140 | uri=/api/v1/prazos | mensagem=Número do processo inválido. Utilize o formato CNJ: NNNNNNN-DD.AAAA.J.TR.OOOO",
  "logger_name": "ai.attus.prazos.apresentacao.excecao.ManipuladorExcecaoGlobal",
  "correlationId": "492b2c46-aa1a-4694-ac91-ce01a41d5140",
  "aplicacao": "gerenciador-prazos-processuais"
}
```

Campos úteis para análise em ferramentas como ELK, Loki ou CloudWatch (não integradas neste MVP):

- `correlationId` — agrupar todos os logs de uma mesma requisição
- `logger_name` — identificar camada (caso de uso vs. tratamento de exceção)
- `level` — filtrar INFO vs. ERROR
- `aplicacao` — distinguir instâncias se houver vários serviços

---

## 6. Fluxo da requisição (visão geral)

```
Cliente HTTP
    │
    ▼
FiltroCorrelationId  ──► MDC["correlationId"] = header ou UUID
    │
    ▼
Controller REST
    │
    ▼
Caso de uso
    │
    ├─► Sucesso (criar / alterar / excluir) ──► log INFO + correlationId no JSON
    │
    └─► Exceção ──► ManipuladorExcecaoGlobal
                        │
                        ├─► log ERROR (+ stack trace quando aplicável)
                        └─► Resposta RFC 7807 com property "correlationId"
    │
    ▼
FiltroCorrelationId (finally) ──► MDC.remove("correlationId")
```

---

## 7. Como reproduzir durante a análise

### 7.1. Subir a API

```powershell
. .\config\ambiente.ps1
docker compose up -d
mvn spring-boot:run
```

Os logs aparecem no **mesmo terminal** onde o Spring Boot foi iniciado.

### 7.2. Cenários sugeridos

**Cenário A — INFO (criação)**

```powershell
Invoke-RestMethod -Method Post -Uri "http://localhost:8080/api/v1/prazos" `
  -Headers @{ "X-Correlation-Id" = "demo-info-001"; "Content-Type" = "application/json" } `
  -Body '{"numeroProcesso":"0001234-56.2024.8.26.0100","descricao":"Demonstração de log INFO","dataVencimento":"2030-06-01"}'
```

Esperado: linha JSON com `level":"INFO"` e `correlationId":"demo-info-001"`.

**Cenário B — ERROR (regra de negócio)**

```powershell
Invoke-RestMethod -Method Post -Uri "http://localhost:8080/api/v1/prazos" `
  -Headers @{ "Content-Type" = "application/json" } `
  -Body '{"numeroProcesso":"invalido","descricao":"Demonstração de log ERROR","dataVencimento":"2030-06-01"}'
```

Esperado: resposta 400 com `correlationId` no JSON de erro; no console, `ERROR` sem stack trace completo (apenas mensagem).

**Cenário C — ERROR com stack (erro interno simulado)**

Indisponível por endpoint público; o handler genérico (`Exception`) registra stack trace quando uma falha não prevista ocorre.

### 7.3. Swagger

Com a API no ar: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) — as mesmas requisições geram os mesmos padrões de log.

---

## 8. Relação com a análise de incidente

O arquivo `INCIDENT_ANALYSIS.md` cita um log de produção com:

- `ERROR` no `GlobalExceptionHandler`
- `ObjectOptimisticLockingFailureException` / `StaleObjectStateException`

No projeto entregue, o equivalente é `ManipuladorExcecaoGlobal`, que:

1. Registra **ERROR** com `correlationId` e URI.
2. Inclui **stack trace** para conflito de concorrência.
3. Responde **409 Conflict** com mensagem amigável ao usuário.
4. Expõe o campo `versao` na API para controle otimista preventivo.

---

## 9. Limitações conscientes (MVP do desafio)

| Item | Status |
|------|--------|
| Persistência de logs em arquivo | Não implementado |
| Rotação / retenção de arquivos | Não implementado |
| Envio para centralizador (ELK, Datadog, etc.) | Não implementado |
| Logs estruturados no frontend | Não implementado |
| Auditoria por usuário autenticado | Não implementado (sem JWT) |
| Trace distribuído (OpenTelemetry) | Não implementado |
| Log INFO em todas as operações (incl. GET) | Não implementado |

Melhorias indicadas para produção estão resumidas em `TECHNICAL_NOTE.md`.

---

## 10. Referência rápida de arquivos

| Arquivo | Responsabilidade |
|---------|------------------|
| `src/main/resources/logback-spring.xml` | Formato JSON e appender |
| `src/main/resources/application.yml` | Níveis de log |
| `FiltroCorrelationId.java` | MDC e cabeçalho HTTP |
| `ManipuladorExcecaoGlobal.java` | Log ERROR + RFC 7807 |
| `CriarPrazoCasoUso.java` | Log INFO na criação |
| `AtualizarPrazoCasoUso.java` | Log INFO na alteração |
| `ExcluirPrazoCasoUso.java` | Log INFO na exclusão lógica |

---

*Documento elaborado para revisores técnicos do desafio Attus — Gerenciador de Prazos Processuais.*
