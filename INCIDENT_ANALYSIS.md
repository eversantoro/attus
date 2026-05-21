# Análise de Incidente — Conflito de Concorrência em Prazo Processual

## Log analisado

```text
2024-05-10T14:32:01.123Z ERROR [http-nio-8080-exec-4] c.a.p.api.GlobalExceptionHandler - Error updating Prazo ID 123e4567-e89b-12d3-a456-426614174000
org.springframework.orm.ObjectOptimisticLockingFailureException: Object of class [com.attus.prazos.domain.PrazoProcessual] with identifier [123e4567-e89b-12d3-a456-426614174000]: optimistic locking failed; nested exception is org.hibernate.StaleObjectStateException: Row was updated or deleted by another transaction (or unsaved-value mapping was incorrect)
```

---

## Causa raiz

O erro `ObjectOptimisticLockingFailureException` / `StaleObjectStateException` indica que o Hibernate tentou persistir uma entidade cuja **versão otimista** (`@Version`) não coincide mais com o valor gravado no banco.

Em aplicações web transacionais isso ocorre tipicamente quando:

1. O **Usuário A** abre a tela de edição do prazo (versão `N` carregada na interface).
2. O **Usuário B** salva uma alteração no mesmo prazo (versão no banco passa para `N+1`).
3. O **Usuário A** envia o `PUT` ainda com dados baseados na versão `N`.
4. O JPA detecta o descompasso e lança a exceção para evitar **perda silenciosa de atualização** (lost update).

No domínio de procuradorias, é cenário realista: dois procuradores podem atualizar status, descrição ou data do mesmo prazo processual simultaneamente.

---

## Impacto

| Aspecto | Efeito |
|---------|--------|
| Usuário | A operação de salvar falha; sem tratamento adequado, vê erro genérico ou tela quebrada |
| Dados | Nenhuma corrupção: a transação do Usuário A é revertida; a alteração do Usuário B permanece |
| Operação | Pico de erros 409/500 no endpoint de atualização; aumento de chamados ao suporte |
| Auditoria | Log `ERROR` sem mensagem amigável dificulta distinguir bug de conflito esperado |

---

## Correção proposta (curto prazo)

### Backend

1. Manter `@Version` na entidade JPA (`PrazoProcessualEntidade.versao`).
2. Tratar `OptimisticLockingFailureException` no `@RestControllerAdvice`, retornando **RFC 7807** com HTTP **409 Conflict** e mensagem clara:

   > *"O prazo foi alterado por outro usuário. Atualize a página e tente novamente."*

3. Expor o campo `versao` no JSON de resposta para o frontend enviar no `PUT`.
4. Registrar conflito como `ERROR` com `correlationId` e stack trace (diagnóstico), sem classificar como falha interna 500.

### Frontend

1. Enviar `versao` recebida na última leitura ao atualizar.
2. Ao receber HTTP 409, exibir toast informativo e **recarregar** o prazo antes de nova edição.

### Banco

Nenhuma alteração estrutural além da coluna `versao` já utilizada pelo Hibernate.

---

## Medidas de prevenção (longo prazo)

| Padrão | Benefício |
|--------|-----------|
| **Controle otimista explícito** (`versao` no DTO) | Falha previsível e recuperável |
| **ETag / If-Match** no HTTP | Padrão REST para concorrência sem acoplar ao JPA |
| **WebSocket ou SSE** | Notificar que o recurso mudou enquanto outro usuário edita |
| **Bloqueio pessimista opcional** | Para fluxos críticos: "assumir edição" por X minutos |
| **Histórico de alterações (audit trail)** | Permite merge manual e rastreabilidade jurídica |
| **UI com indicador "editado por X há Y min"** | Reduz tentativas concorrentes desinformadas |
| **Idempotência e comandos (CQRS)** | Separa leitura/escrita e facilita retry seguro |

---

## Implementação neste projeto

Este repositório já incorpora as correções de curto prazo:

- `ManipuladorExcecaoGlobal` mapeia `OptimisticLockingFailureException` → **409**
- Campo `versao` em respostas e requisições de atualização
- Frontend envia `versao` e exibe feedback via toast (axios + react-hot-toast)
