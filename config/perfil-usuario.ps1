# Execute UMA VEZ para persistir JAVA_HOME no perfil do Windows (opcional)
$perfil = $PROFILE.CurrentUserAllHosts
$linha = '. "C:\java\eclipse-workspace\attus\config\ambiente.ps1"'
if (-not (Test-Path $perfil)) { New-Item -Path $perfil -ItemType File -Force | Out-Null }
if (-not (Select-String -Path $perfil -Pattern 'attus\\config\\ambiente' -Quiet)) {
    Add-Content -Path $perfil -Value "`n# Attus - Gerenciador de Prazos`n$linha"
    Write-Host "Perfil atualizado: $perfil" -ForegroundColor Green
} else {
    Write-Host "Perfil já configurado." -ForegroundColor Yellow
}
