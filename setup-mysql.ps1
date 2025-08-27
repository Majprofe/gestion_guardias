#!/usr/bin/env pwsh
# ============================================================================
# Script de acceso directo - Configuración MySQL
# ============================================================================
# Ejecuta el script de configuración de MySQL desde la carpeta scripts
# Uso: .\setup-mysql.ps1

$ErrorActionPreference = "Stop"

# Verificar que estamos en el directorio correcto
if (-not (Test-Path "scripts\setup-mysql.ps1")) {
    Write-Host "[ERROR] No se encuentra el script en scripts\setup-mysql.ps1" -ForegroundColor Red
    Write-Host "[INFO] Asegurate de ejecutar este script desde el directorio raiz del proyecto" -ForegroundColor Yellow
    exit 1
}

# Ejecutar el script real
& ".\scripts\setup-mysql.ps1"
