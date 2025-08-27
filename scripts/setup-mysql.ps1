#!/usr/bin/env pwsh
# ============================================================================
# Script de configuración de MySQL - Sistema de Gestión de Guardias
# ============================================================================
# Configura las bases de datos MySQL necesarias para el proyecto
# Uso: .\scripts\setup-mysql.ps1

Write-Host "============================================================================" -ForegroundColor Cyan
Write-Host "                     CONFIGURACION MYSQL                                   " -ForegroundColor Cyan
Write-Host "                    Sistema de Gestion de Guardias                        " -ForegroundColor Cyan
Write-Host "============================================================================" -ForegroundColor Cyan

# Verificar si MySQL está instalado
Write-Host "[*] Verificando MySQL..." -ForegroundColor Yellow

try {
    $mysqlVersion = mysql --version 2>$null
    if ($mysqlVersion) {
        Write-Host "[OK] MySQL encontrado: $mysqlVersion" -ForegroundColor Green
    } else {
        Write-Host "[ERROR] MySQL no encontrado" -ForegroundColor Red
        Write-Host "[INFO] Instala MySQL desde: https://dev.mysql.com/downloads/mysql/" -ForegroundColor Yellow
        exit 1
    }
} catch {
    Write-Host "[ERROR] MySQL no encontrado en PATH" -ForegroundColor Red
    Write-Host "[INFO] Asegurate de que MySQL este instalado y en el PATH" -ForegroundColor Yellow
    exit 1
}

# Verificar si el archivo SQL existe
$sqlFile = "database_setup.sql"
if (-not (Test-Path $sqlFile)) {
    Write-Host "[ERROR] No se encuentra el archivo $sqlFile" -ForegroundColor Red
    exit 1
}

Write-Host "[*] Configurando bases de datos..." -ForegroundColor Yellow

# Ejecutar script SQL
Write-Host "[INFO] Ejecutando script de configuración de base de datos..." -ForegroundColor Cyan
Write-Host "[INFO] Se te pedirá la contraseña de MySQL root" -ForegroundColor Yellow

try {
    mysql -u root -p < $sqlFile
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "[OK] Bases de datos configuradas correctamente" -ForegroundColor Green
        
        Write-Host @"

[*] BASES DE DATOS CREADAS:
 -> gestion_guardias (Backend Guardias - Puerto 8081)
 -> gestion_horarios (Backend Horarios - Puerto 8082)

[*] CONFIGURACION EN .env:
 -> DATABASE_URL=jdbc:mysql://localhost:3306/gestion_guardias
 -> HORARIOS_DATABASE_URL=jdbc:mysql://localhost:3306/gestion_horarios
 -> DATABASE_USERNAME=root
 -> DATABASE_PASSWORD=root (cambia si usas otra contraseña)

[*] SIGUIENTE PASO:
 -> Ejecuta: .\dev.ps1 para iniciar los servicios

"@ -ForegroundColor Green
        
    } else {
        Write-Host "[ERROR] Error al configurar las bases de datos" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "[ERROR] Error al ejecutar el script SQL: $_" -ForegroundColor Red
    exit 1
}

Write-Host "[OK] Configuración de MySQL completada!" -ForegroundColor Green
