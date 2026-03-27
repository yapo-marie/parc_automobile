<# 
  Crée fleetpro / fleetpro sur PostgreSQL local (sans Docker).
  Prérequis : psql dans le PATH (ex. C:\Program Files\PostgreSQL\18\bin)

  Usage :
    .\scripts\setup-postgres-fleetpro.ps1
    .\scripts\setup-postgres-fleetpro.ps1 -PostgresUser postgres -PostgresHost localhost
#>
param(
  [string]$PostgresUser = "postgres",
  [string]$PostgresHost = "localhost"
)

$SqlFile = Join-Path $PSScriptRoot "init-local-postgres.sql"
if (-not (Test-Path $SqlFile)) {
  Write-Error "Fichier introuvable : $SqlFile"
  exit 1
}

Write-Host "Exécution de init-local-postgres.sql en tant que '$PostgresUser' sur $PostgresHost..."
Write-Host "(Saisissez le mot de passe PostgreSQL si demandé.)" -ForegroundColor DarkYellow

& psql -U $PostgresUser -h $PostgresHost -d postgres -v ON_ERROR_STOP=1 -f $SqlFile
if ($LASTEXITCODE -ne 0) {
  Write-Host ""
  Write-Host "Si le rôle ou la base existait déjà, connecte-toi avec psql et adapte, ou supprime-les puis relance." -ForegroundColor Yellow
  exit $LASTEXITCODE
}

Write-Host ""
Write-Host "OK. Tu peux lancer le backend (mot de passe applicatif = fleetpro) :" -ForegroundColor Green
Write-Host '  cd Backend; .\mvnw.cmd spring-boot:run' -ForegroundColor Cyan
