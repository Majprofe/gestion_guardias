# ============================================================================
# SCRIPT RAPIDO - Desarrollo Express
# ============================================================================
# Lanza todos los servicios rapidamente
# Uso: .\dev.ps1

Write-Host "[*] Lanzamiento rapido - Gestion de Guardias" -ForegroundColor Yellow

# Función simple para lanzar servicios
function Quick-Start {
    param([string]$name, [string]$cmd, [string]$dir, [string]$color)
    Write-Host "[*] $name..." -ForegroundColor $color
    Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$dir'; Write-Host '[INFO] $name en $dir' -ForegroundColor $color; $cmd"
}

# Verificación rápida del .env
if (-not (Test-Path ".env")) {
    Write-Host "[ERROR] Falta archivo .env" -ForegroundColor Red
    exit 1
}

Write-Host "[*] Iniciando stack completo..." -ForegroundColor Green

# Lanzar todos los servicios
$root = Get-Location
Quick-Start "Backend Horarios" ".\mvnw.cmd spring-boot:run" "$root\apps\horarios" "Magenta"
Start-Sleep -Seconds 2

Quick-Start "Backend Guardias" ".\mvnw.cmd spring-boot:run" "$root\apps\backend" "Green"  
Start-Sleep -Seconds 2

Quick-Start "Frontend Vue.js" "npm run dev" "$root\apps\frontend" "Cyan"

Write-Host @"

[*] SERVICIOS INICIADOS:
 -> Frontend: http://localhost:5500
 -> Guardias: http://localhost:8081/swagger-ui.html  
 -> Horarios: http://localhost:8082/swagger-ui.html

[*] Listo para desarrollar!
"@ -ForegroundColor Yellow
