# Sobe PostgreSQL, backend e frontend do Gerenciador de Prazos Processuais
$Raiz = Split-Path $PSScriptRoot -Parent
Set-Location $Raiz
New-Item -ItemType Directory -Force -Path "$Raiz\.run" | Out-Null

. "$Raiz\config\ambiente.ps1"

Write-Host "`n[1/4] Subindo PostgreSQL (Docker)..." -ForegroundColor Cyan
docker compose up -d
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

Write-Host "Aguardando healthcheck do PostgreSQL..." -ForegroundColor Yellow
$max = 30
for ($i = 0; $i -lt $max; $i++) {
    $saude = docker inspect --format='{{.State.Health.Status}}' attus-prazos-postgres 2>$null
    if ($saude -eq 'healthy') { break }
    Start-Sleep -Seconds 2
}
Write-Host "PostgreSQL pronto." -ForegroundColor Green

Write-Host "`n[2/4] Compilando backend..." -ForegroundColor Cyan
mvn -q compile -DskipTests
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

Write-Host "`n[3/4] Iniciando backend (porta 8080)..." -ForegroundColor Cyan
$mvnCmd = (Get-Command mvn -ErrorAction SilentlyContinue).Source
if (-not $mvnCmd) { $mvnCmd = "mvn.cmd" }
$backend = Start-Process -FilePath $mvnCmd `
    -ArgumentList "spring-boot:run","-q" `
    -WorkingDirectory $Raiz `
    -PassThru -WindowStyle Hidden
$backend.Id | Set-Content "$Raiz\.run\backend.pid"

Write-Host "Aguardando API..." -ForegroundColor Yellow
for ($i = 0; $i -lt 60; $i++) {
    try {
        $r = Invoke-WebRequest -Uri "http://localhost:8080/api-docs" -UseBasicParsing -TimeoutSec 2
        if ($r.StatusCode -eq 200) { break }
    } catch { Start-Sleep -Seconds 3 }
}

Write-Host "`n[4/4] Iniciando frontend (porta 5173)..." -ForegroundColor Cyan
if (-not (Test-Path "$Raiz\frontend\node_modules")) {
    Set-Location "$Raiz\frontend"
    npm install
    Set-Location $Raiz
}
$npmCmd = (Get-Command npm -ErrorAction SilentlyContinue).Source
if (-not $npmCmd) { $npmCmd = "npm.cmd" }
$frontend = Start-Process -FilePath $npmCmd `
    -ArgumentList "run","dev" `
    -WorkingDirectory "$Raiz\frontend" `
    -PassThru -WindowStyle Hidden
$frontend.Id | Set-Content "$Raiz\.run\frontend.pid"

Write-Host "`n========================================" -ForegroundColor Green
Write-Host " Ambiente Attus em execução!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host " Frontend : http://localhost:5173"
Write-Host " API      : http://localhost:8080"
Write-Host " Swagger  : http://localhost:8080/swagger-ui.html"
Write-Host "`nPara parar: .\scripts\parar-ambiente.ps1"
