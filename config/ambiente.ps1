# Configura JAVA_HOME, Docker, Maven, Node e PATH para o projeto Attus
$env:Path = [System.Environment]::GetEnvironmentVariable("Path", "Machine") + ";" +
            [System.Environment]::GetEnvironmentVariable("Path", "User")

$Jdk21 = "C:\Program Files\Java\jdk-21"
$DockerBin = "C:\Program Files\Docker\Docker\resources\bin"
$MavenHome = "C:\java\apache-maven-3.9.5"

$NodePaths = @(
    "$env:ProgramFiles\nodejs",
    "${env:ProgramFiles(x86)}\nodejs",
    "$env:APPDATA\npm",
    "$env:LOCALAPPDATA\fnm_multishells",
    "$env:USERPROFILE\.nvm"
)

if (-not (Test-Path $Jdk21)) {
    Write-Error "JDK 21 não encontrado em: $Jdk21"
    exit 1
}

$env:JAVA_HOME = $Jdk21

$Prioritarios = @(
    "$Jdk21\bin",
    $DockerBin,
    "$MavenHome\bin"
)

foreach ($np in $NodePaths) {
    if (Test-Path $np) { $Prioritarios += $np }
}

$Restante = $env:Path -split ';' | Where-Object {
    $_ -and ($_ -notin $Prioritarios) -and $_ -notmatch '\\jdk-1\.8\\|\\jre1\.8|\\Jdk8\\'
}
$env:Path = (($Prioritarios + $Restante) | Select-Object -Unique) -join ';'

if (Test-Path "$DockerBin\docker.exe") {
    $env:DOCKER_HOST = "npipe:////./pipe/dockerDesktopLinuxEngine"
    Write-Host "Docker    = $(docker --version)" -ForegroundColor Green
} else {
    Write-Warning "Docker CLI não encontrado. Instale o Docker Desktop."
}

Write-Host "JAVA_HOME = $env:JAVA_HOME" -ForegroundColor Green
if (Get-Command mvn -ErrorAction SilentlyContinue) {
    Write-Host "Maven     = $(mvn -version 2>&1 | Select-Object -First 1)" -ForegroundColor Green
}
if (Get-Command node -ErrorAction SilentlyContinue) {
    Write-Host "Node      = $(node --version)" -ForegroundColor Green
}
java -version
