# Executa testes backend e frontend
$Raiz = Split-Path $PSScriptRoot -Parent
Set-Location $Raiz
. "$Raiz\config\ambiente.ps1"

Write-Host "`n=== Testes Backend (JUnit + integração RestAssured / PostgreSQL) ===" -ForegroundColor Cyan
mvn verify
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

Write-Host "`n=== Testes Frontend (Vitest) ===" -ForegroundColor Cyan
Set-Location "$Raiz\frontend"
npm test
exit $LASTEXITCODE
