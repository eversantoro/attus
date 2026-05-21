# Para backend, frontend e opcionalmente o PostgreSQL
param([switch]$PararPostgres)

$Raiz = Split-Path $PSScriptRoot -Parent
. "$Raiz\config\ambiente.ps1"
$PidDir = "$Raiz\.run"

foreach ($arquivo in @("backend.pid", "frontend.pid")) {
    $caminho = Join-Path $PidDir $arquivo
    if (Test-Path $caminho) {
        $pid = Get-Content $caminho
        $proc = Get-Process -Id $pid -ErrorAction SilentlyContinue
        if ($proc) {
            Stop-Process -Id $pid -Force -ErrorAction SilentlyContinue
            # Encerra árvore de processos filhos (mvn/node)
            Get-CimInstance Win32_Process | Where-Object { $_.ParentProcessId -eq $pid } |
                ForEach-Object { Stop-Process -Id $_.ProcessId -Force -ErrorAction SilentlyContinue }
        }
        Remove-Item $caminho -Force
        Write-Host "Processo $arquivo (PID $pid) encerrado." -ForegroundColor Yellow
    }
}

# Mata processos órfãos nas portas 8080 e 5173 se ainda existirem
foreach ($porta in @(8080, 5173)) {
    $conexao = Get-NetTCPConnection -LocalPort $porta -ErrorAction SilentlyContinue | Select-Object -First 1
    if ($conexao) {
        Stop-Process -Id $conexao.OwningProcess -Force -ErrorAction SilentlyContinue
        Write-Host "Porta $porta liberada." -ForegroundColor Yellow
    }
}

if ($PararPostgres) {
    Set-Location $Raiz
    docker compose down
    Write-Host "PostgreSQL encerrado." -ForegroundColor Green
} else {
    Write-Host "PostgreSQL mantido (use -PararPostgres para encerrar)." -ForegroundColor DarkGray
}

Write-Host "Ambiente parado." -ForegroundColor Green
