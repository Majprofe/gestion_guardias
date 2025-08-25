#!/usr/bin/env pwsh
# ============================================================================
# Script de acceso directo - Detener Servicios
# ============================================================================
# Ejecuta el script para detener servicios desde la carpeta scripts
# Uso: .\stop.ps1

$ErrorActionPreference = "Stop"

# Verificar que estamos en el directorio correcto
if (-not (Test-Path "scripts\stop-dev.ps1")) {
    Write-Host "[ERROR] No se encuentra el script en scripts\stop-dev.ps1" -ForegroundColor Red
    Write-Host "[INFO] Asegurate de ejecutar este script desde el directorio raiz del proyecto" -ForegroundColor Yellow
    exit 1
}

# Ejecutar el script real
& ".\scripts\stop-dev.ps1"
