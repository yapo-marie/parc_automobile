<#
  Lance Spring Boot avec l'utilisateur super PostgreSQL « postgres » (sans créer fleetpro).
  Remplace DB_PASSWORD par le mot de passe réel de postgres.

  Usage :
    .\scripts\run-backend-as-postgres.ps1
    .\scripts\run-backend-as-postgres.ps1 -DbPassword "monSecret"
#>
param(
  [string]$DbPassword = "",
  [string]$DbUser = "postgres",
  [string]$DbUrl = "jdbc:postgresql://localhost:5432/fleetpro"
)

$backend = Join-Path (Split-Path $PSScriptRoot -Parent) "Backend"
if (-not (Test-Path $backend)) {
  Write-Error "Dossier Backend introuvable : $backend"
  exit 1
}

if ([string]::IsNullOrWhiteSpace($DbPassword)) {
  $secure = Read-Host "Mot de passe PostgreSQL ($DbUser)" -AsSecureString
  $ptr = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($secure)
  try {
    $DbPassword = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($ptr)
  }
  finally {
    [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($ptr)
  }
}

$env:DB_USERNAME = $DbUser
$env:DB_PASSWORD = $DbPassword
$env:DB_URL = $DbUrl

Write-Host "DB_URL=$DbUrl"
Write-Host "DB_USERNAME=$DbUser"
Set-Location $backend
.\mvnw.cmd spring-boot:run
