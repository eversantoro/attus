# Nota Técnica — Gerenciador de Prazos Processuais

## Decisões arquiteturais

### Clean Architecture

A separação em `dominio`, `aplicacao`, `infraestrutura` e `apresentacao` isola regras de negócio (CNJ, datas, status) de detalhes Spring/JPA. Casos de uso (`CriarPrazoCasoUso`, etc.) orquestram o domínio sem conhecer HTTP ou SQL, facilitando testes unitários com mocks e evolução futura (ex.: troca de REST por mensageria).

### Banco relacional (PostgreSQL)

Prazos processuais exigem integridade referencial, consultas ordenadas por vencimento, paginação e transações ACID — requisitos naturais de um SGBD relacional. Flyway versiona o schema (`V1__criar_tabela_prazo_processual.sql`) de forma reproduzível em dev, testes (Testcontainers) e produção.

### Soft delete

Exclusão lógica (`excluido`, `data_exclusao`) preserva histórico e atende contexto jurídico, onde remoção física imediata pode ser inadequada para auditoria.

### Controle de concorrência otimista

`@Version` no JPA previne lost updates quando múltiplos usuários editam o mesmo prazo — alinhado ao cenário do `INCIDENT_ANALYSIS.md`.

### RFC 7807 (Problem Details)

Erros padronizados com `title`, `detail`, `type` e `correlationId` melhoram integração frontend e observabilidade.

---

## Trade-offs (escopo de teste técnico)

| Atalho | Motivo |
|--------|--------|
| Sem autenticação JWT/OAuth2 | Foco no fluxo de negócio ponta a ponta |
| Sem CI/CD (GitHub Actions, etc.) | Entrega em tempo limitado de avaliação |
| Mapeador manual domínio ↔ JPA | Evita MapStruct extra; suficiente para uma entidade |
| Validação de versão também no caso de uso | Dupla camada (aplicação + Hibernate) — aceitável para demonstração |
| Frontend sem roteador (SPA única) | Dashboard único atende o escopo |
| Sem notificações push/e-mail de vencimento | Fora do MVP do desafio |

---

## Melhorias futuras (produção)

1. **Segurança:** Keycloak/OAuth2, perfis (procurador, administrador), auditoria por usuário.
2. **Cache:** Redis para listagens frequentes e detalhe de prazo.
3. **Mensageria:** RabbitMQ/Kafka para eventos `PrazoCriado`, `PrazoVencendo` e workers de notificação.
4. **Agendamento:** Job diário para alertar prazos que vencem em D-1/D-0.
5. **Observabilidade:** Micrometer + Prometheus + Grafana; tracing OpenTelemetry.
6. **API:** Versionamento formal, rate limiting, HATEOAS opcional.
7. **Frontend:** React Query para cache/sync; modo offline; PWA.
8. **Infra:** Kubernetes, Helm, pipelines GitOps, ambientes efêmeros por PR.

---

## Testes

- **Unitários:** `ValidadorNumeroCnj`, `PrazoProcessual`, `CriarPrazoCasoUso`
- **Integração:** `PrazoProcessualIntegracaoTest` com PostgreSQL do `docker-compose` + RestAssured
- **Frontend:** Vitest + Testing Library (formulário, validação CNJ, chamada API mockada)

---

## Nomenclatura

Classes, pacotes e mensagens de negócio em **português** (`PrazoProcessual`, `ManipuladorExcecaoGlobal`, `CriarPrazoCasoUso`), pacote raiz `ai.attus`, conforme solicitado.
