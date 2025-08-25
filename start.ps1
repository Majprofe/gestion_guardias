#!/usr/bin/env pwsh
# ============================================================================
# Script de acceso directo - Inicio con Logging
# ============================================================================
# Ejecuta el script de desarrollo completo desde la carpeta scripts
# Uso: .\start.ps1 [all|frontend|backend|horarios|stop|help]

param(
    [string]$Action = "all"
)

$ErrorActionPreference = "Stop"

# Verificar que estamos en el directorio correcto
if (-not (Test-Path "scripts\start-dev-simple.ps1")) {
    Write-Host "[ERROR] No se encuentra el script en scripts\start-dev-simple.ps1" -ForegroundColor Red
    Write-Host "[INFO] Asegurate de ejecutar este script desde el directorio raiz del proyecto" -ForegroundColor Yellow
    exit 1
}

# Ejecutar el script real con los parámetros
& ".\scripts\start-dev-simple.ps1" $Action
