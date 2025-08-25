#!/usr/bin/env pwsh
# ============================================================================
# Script de acceso directo - Inicio Rápido
# ============================================================================
# Ejecuta el script de desarrollo rápido desde la carpeta scripts
# Uso: .\dev.ps1

$ErrorActionPreference = "Stop"

# Verificar que estamos en el directorio correcto
if (-not (Test-Path "scripts\dev.ps1")) {
    Write-Host "[ERROR] No se encuentra el script en scripts\dev.ps1" -ForegroundColor Red
    Write-Host "[INFO] Asegurate de ejecutar este script desde el directorio raiz del proyecto" -ForegroundColor Yellow
    exit 1
}

# Ejecutar el script real
& ".\scripts\dev.ps1"