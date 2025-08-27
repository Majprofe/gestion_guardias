# ============================================================================
# SCRIPT DE DESARROLLO - Sistema de Gestion de Guardias
# ============================================================================
# Script para desarrollo con verificaciones
# Uso: .\start-dev-simple.ps1 [all|frontend|backend|horarios|stop]

param(
    [string]$Action = "all"
)

Write-Host "============================================================================" -ForegroundColor Cyan
Write-Host "                     MODO DESARROLLO ACTIVADO                              " -ForegroundColor Cyan
Write-Host "                    Sistema de Gestion de Guardias                        " -ForegroundColor Cyan
Write-Host "============================================================================" -ForegroundColor Cyan

# ============================================================================
# FUNCIONES AUXILIARES
# ============================================================================

function Test-Prerequisites {
    Write-Host "[*] Verificando prerrequisitos..." -ForegroundColor Yellow
    
    $checks = @()
    
    # Verificar Node.js
    try {
        $nodeVersion = node --version 2>$null
        if ($nodeVersion) {
            $checks += "[OK] Node.js: $nodeVersion"
        } else {
            $checks += "[ERROR] Node.js: No encontrado"
            return $false
        }
    } catch {
        $checks += "[ERROR] Node.js: No encontrado"
        return $false
    }
    
    # Verificar Java
    try {
        $javaVersion = java -version 2>&1 | Select-String "version" | ForEach-Object { $_.ToString().Split('"')[1] }
        if ($javaVersion) {
            $checks += "[OK] Java: $javaVersion"
        } else {
            $checks += "[ERROR] Java: No encontrado"
            return $false
        }
    } catch {
        $checks += "[ERROR] Java: No encontrado"
        return $false
    }
    
    # Verificar .env
    if (Test-Path ".env") {
        $checks += "[OK] Archivo .env: Encontrado"
    } else {
        $checks += "[ERROR] Archivo .env: No encontrado"
        return $false
    }
    
    # Verificar dependencias del frontend
    if (Test-Path "apps\frontend\node_modules") {
        $checks += "[OK] Dependencias Frontend: Instaladas"
    } else {
        $checks += "[WARNING] Dependencias Frontend: Faltan (se instalaran)"
    }
    
    # Mostrar resultados
    $checks | ForEach-Object { Write-Host "  $_" -ForegroundColor White }
    
    return $true
}

function Install-FrontendDependencies {
    if (-not (Test-Path "apps\frontend\node_modules")) {
        Write-Host "[*] Instalando dependencias del frontend..." -ForegroundColor Yellow
        $originalLocation = Get-Location
        Set-Location "apps\frontend"
        npm install
        Set-Location $originalLocation
        Write-Host "[OK] Dependencias instaladas" -ForegroundColor Green
    }
}

function Stop-AllServices {
    Write-Host "[*] Deteniendo todos los servicios..." -ForegroundColor Red
    
    # Matar procesos Java (Spring Boot)
    Get-Process -Name "java" -ErrorAction SilentlyContinue | Where-Object {
        $_.CommandLine -like "*spring-boot*" -or 
        $_.CommandLine -like "*mvnw*" -or
        $_.CommandLine -like "*8081*" -or 
        $_.CommandLine -like "*8082*"
    } | Stop-Process -Force -ErrorAction SilentlyContinue
    
    # Matar procesos Node.js (Vite)
    Get-Process -Name "node" -ErrorAction SilentlyContinue | Where-Object {
        $_.CommandLine -like "*vite*" -or 
        $_.CommandLine -like "*5500*"
    } | Stop-Process -Force -ErrorAction SilentlyContinue
    
    Write-Host "[OK] Servicios detenidos" -ForegroundColor Green
}

function Start-Service {
    param(
        [string]$ServiceName,
        [string]$Command,
        [string]$WorkingDirectory,
        [string]$Color = "White"
    )
    
    Write-Host "[*] Iniciando $ServiceName..." -ForegroundColor $Color
    
    $scriptBlock = @"
Set-Location '$WorkingDirectory'
Write-Host '[INFO] Iniciando $ServiceName en: $WorkingDirectory' -ForegroundColor $Color
Write-Host '[CMD] Comando: $Command' -ForegroundColor Gray
Write-Host '============================================================' -ForegroundColor Cyan
$Command
"@
    
    Start-Process powershell -ArgumentList "-NoExit", "-Command", $scriptBlock
}

# ============================================================================
# LOGICA PRINCIPAL
# ============================================================================

# Verificar prerrequisitos
if (-not (Test-Prerequisites)) {
    Write-Host "[ERROR] Faltan prerrequisitos. Por favor instala las dependencias faltantes." -ForegroundColor Red
    exit 1
}

# Ejecutar acción solicitada
switch ($Action.ToLower()) {
    "stop" {
        Stop-AllServices
        exit 0
    }
    "frontend" {
        Install-FrontendDependencies
        Write-Host "[*] Iniciando Frontend en modo desarrollo..." -ForegroundColor Cyan
        Start-Service "Frontend Vue.js" "npm run dev" "$PWD\apps\frontend" "Cyan"
    }
    "backend" {
        Write-Host "[*] Iniciando Backend Guardias..." -ForegroundColor Green
        Start-Service "Backend Guardias" ".\mvnw.cmd spring-boot:run" "$PWD\apps\backend" "Green"
    }
    "horarios" {
        Write-Host "[*] Iniciando Backend Horarios..." -ForegroundColor Magenta
        Start-Service "Backend Horarios" ".\mvnw.cmd spring-boot:run" "$PWD\apps\horarios" "Magenta"
    }
    "all" {
        Install-FrontendDependencies
        
        Write-Host "[*] Iniciando stack completo de desarrollo..." -ForegroundColor Green
        
        # Backend Horarios primero
        Start-Service "Backend Horarios" ".\mvnw.cmd spring-boot:run" "$PWD\apps\horarios" "Magenta"
        Start-Sleep -Seconds 3
        
        # Backend Guardias segundo
        Start-Service "Backend Guardias" ".\mvnw.cmd spring-boot:run" "$PWD\apps\backend" "Green"
        Start-Sleep -Seconds 3
        
        # Frontend último
        Start-Service "Frontend Vue.js" "npm run dev" "$PWD\apps\frontend" "Cyan"
        
        Write-Host @"

[*] SERVICIOS INICIADOS:
 -> Frontend: http://localhost:5500
 -> Guardias: http://localhost:8081/swagger-ui.html  
 -> Horarios: http://localhost:8082/swagger-ui.html

[*] Listo para desarrollar!
"@ -ForegroundColor Green
    }
    default {
        Write-Host "[ERROR] Accion no reconocida: $Action" -ForegroundColor Red
        Write-Host "[INFO] Uso: .\start-dev-simple.ps1 [all|frontend|backend|horarios|stop]" -ForegroundColor Yellow
        exit 1
    }
}

Write-Host "[OK] Modo desarrollo activado. Happy coding!" -ForegroundColor Green
