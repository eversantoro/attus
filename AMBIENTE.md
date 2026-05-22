# Guia de Ambiente — Attus

## Configuração

| Serviço | URL | Status |
|---------|-----|--------|
| PostgreSQL | `localhost:5432` | Docker `attus-prazos-postgres` (healthy) |
| Backend API | http://localhost:8080 | Spring Boot 3.3 + Java 21 |
| Swagger | http://localhost:8080/swagger-ui.html | OpenAPI |
| Frontend | http://localhost:5173 | Vite + React |

## Configuração automática

O arquivo `config/ambiente.ps1` define:

- `JAVA_HOME` → JDK 21
- `PATH` → Maven, Docker CLI, Node.js
- `DOCKER_HOST` → pipe do Docker Desktop (Windows)

**Sempre execute antes de comandos Maven/Docker:**

```powershell
. .\config\ambiente.ps1
```

## Eclipse/ VS Code

- **Java 21** configurado em `.classpath`, `.settings/org.eclipse.jdt.core.prefs`
- **Launch:** `.launch/GerenciadorPrazos.launch`
- **VS Code:** `.vscode/settings.json` aponta para JDK 21

No Eclipse: *Project → Properties → Java Compiler → 21* e *Maven → Update Project*.

## Persistir no PowerShell (opcional)

```powershell
.\config\perfil-usuario.ps1
```

## Comandos úteis

```powershell
.\scripts\iniciar-ambiente.ps1      # Sobe tudo
.\scripts\parar-ambiente.ps1         # Para API + UI
.\scripts\parar-ambiente.ps1 -PararPostgres  # Para tudo + banco
.\scripts\testar-tudo.ps1            # mvn verify + npm test
```

## Testes de integração

Usam o **mesmo PostgreSQL** do `docker-compose` (perfil `test`). O container deve estar rodando antes de `mvn verify`.
