# Publish Community Edition to GitHub (Step 13)
# Prerequisite: gh auth login

$ErrorActionPreference = "Stop"

$Repo = "inanjdashdamirov/spring-microservices-platform-community"
$Description = "Production-ready Spring Boot Microservices starter with JWT, Gateway, PostgreSQL, Docker and clean architecture."
$Topics = "spring-boot,microservices,java,spring-cloud,jwt,postgresql,docker,starter-template,backend,software-architecture"

$Root = Split-Path -Parent $PSScriptRoot
Set-Location $Root

gh auth status

if (-not (gh repo view $Repo 2>$null)) {
    gh repo create $Repo `
        --public `
        --description $Description `
        --source . `
        --remote origin `
        --push
} else {
    git push -u origin main
}

gh repo edit $Repo --add-topic $Topics

Write-Host "Done: https://github.com/$Repo"
