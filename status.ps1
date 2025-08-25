#!/usr/bin/env pwsh
# ============================================================================
# Script de acceso directo - Estado del Sistema
# ============================================================================
# Ejecuta el script de estado desde la carpeta scripts
# Uso: .\status.ps1

$ErrorActionPreference = "Stop"

# Verificar que estamos en el directorio correcto
if (-not (Test-Path "scripts\status-simple.ps1")) {
    Write-Host "[ERROR] No se encuentra el script en scripts\status-simple.ps1" -ForegroundColor Red
    Write-Host "[INFO] Asegurate de ejecutar este script desde el directorio raiz del proyecto" -ForegroundColor Yellow
    exit 1
}

# Ejecutar el script real
& ".\scripts\status-simple.ps1"