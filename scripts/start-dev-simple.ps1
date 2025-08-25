# ============================================================================
# SCRIPT DE DESARROLLO - Sistema de Gestion de Guardias
# ============================================================================
# Script optimizado para desarrollo con verificaciones y logging
# Uso: .\start-dev-simple.ps1 [all|frontend|backend|horarios|stop]

param(
    [string]$Action = "all"  # all, frontend, backend, horarios, stop
)

$Host.UI.RawUI.WindowTitle = "Sistema Gestion Guardias - Desarrollo"

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

function Start-ServiceWithLogging {
    param(
        [string]$ServiceName,
        [string]$Command,
        [string]$WorkingDirectory,
        [string]$LogFile,
        [string]$Color = "White"
    )
    
    Write-Host "[*] Iniciando $ServiceName..." -ForegroundColor $Color
    
    # Crear directorio de logs si no existe
    $logsDir = "$PWD\logs"
    if (-not (Test-Path $logsDir)) {
        New-Item -ItemType Directory -Path $logsDir | Out-Null
    }
    
    $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    $logPath = "$logsDir\$LogFile"
    
    $scriptBlock = @"
Set-Location '$WorkingDirectory'
Write-Host "[$timestamp] [INFO] Iniciando $ServiceName en: $WorkingDirectory" -ForegroundColor $Color
Write-Host "[$timestamp] [CMD] Comando: $Command" -ForegroundColor Gray
Write-Host "[$timestamp] [LOG] Logs guardandose en: $logPath" -ForegroundColor Gray
Write-Host "============================================================" -ForegroundColor Cyan

# Ejecutar comando con logging
$Command 2>&1 | Tee-Object -FilePath '$logPath'
"@
    
    Start-Process powershell -ArgumentList "-NoExit", "-Command", $scriptBlock
}

function Show-DevelopmentInfo {
    Write-Host @"

PANEL DE DESARROLLO
============================================================================
  Frontend (Vue.js + Vite)                                                 
     URL: http://localhost:5500                                         
     DevTools: http://localhost:5500/__devtools__/                      
                                                                              
  Backend Guardias (Spring Boot)                                          
     API: http://localhost:8081                                          
     Swagger: http://localhost:8081/swagger-ui.html                      
     Health: http://localhost:8081/actuator/health                       
                                                                              
  Backend Horarios (Spring Boot)                                          
     API: http://localhost:8082                                          
     Swagger: http://localhost:8082/swagger-ui.html                      
     Health: http://localhost:8082/actuator/health                       
                                                                              
  Logs de desarrollo: ./logs/                                             
============================================================================
"@ -ForegroundColor Cyan
}

function Show-QuickCommands {
    Write-Host @"

COMANDOS RAPIDOS:
 -> .\start-dev-simple.ps1 all       # Iniciar todo el stack
 -> .\start-dev-simple.ps1 frontend  # Solo frontend  
 -> .\start-dev-simple.ps1 backend   # Solo backend guardias
 -> .\start-dev-simple.ps1 horarios  # Solo backend horarios  
 -> .\start-dev-simple.ps1 stop      # Detener todos los servicios
 -> .\start-dev-simple.ps1 help      # Mostrar esta ayuda

HERRAMIENTAS DE DESARROLLO:
 -> Hot Reload: Activado en todos los servicios
 -> Error Logging: Centralizado en ./logs/
 -> API Documentation: Swagger disponible
 -> Vue DevTools: Disponible en el navegador

"@ -ForegroundColor Yellow
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
    "help" {
        Show-QuickCommands
        exit 0
    }
    "frontend" {
        Install-FrontendDependencies
        Write-Host "[*] Iniciando Frontend en modo desarrollo..." -ForegroundColor Cyan
        Start-ServiceWithLogging "Frontend Vue.js" "npm run dev" "$PWD\apps\frontend" "frontend.log" "Cyan"
        Show-DevelopmentInfo
    }
    "backend" {
        Write-Host "[*] Iniciando Backend Guardias..." -ForegroundColor Green
        Start-ServiceWithLogging "Backend Guardias" ".\mvnw.cmd spring-boot:run" "$PWD\apps\backend" "backend-guardias.log" "Green"
        Show-DevelopmentInfo
    }
    "horarios" {
        Write-Host "[*] Iniciando Backend Horarios..." -ForegroundColor Magenta
        Start-ServiceWithLogging "Backend Horarios" ".\mvnw.cmd spring-boot:run" "$PWD\apps\horarios" "backend-horarios.log" "Magenta"
        Show-DevelopmentInfo
    }
    "all" {
        Install-FrontendDependencies
        
        Write-Host "[*] Iniciando stack completo de desarrollo..." -ForegroundColor Green
        Write-Host "[INFO] Los servicios se iniciaran en orden con delays para evitar conflictos..." -ForegroundColor Yellow
        
        # Backend Horarios primero (más lento en arrancar)
        Start-ServiceWithLogging "Backend Horarios" ".\mvnw.cmd spring-boot:run" "$PWD\apps\horarios" "backend-horarios.log" "Magenta"
        Start-Sleep -Seconds 3
        
        # Backend Guardias segundo
        Start-ServiceWithLogging "Backend Guardias" ".\mvnw.cmd spring-boot:run" "$PWD\apps\backend" "backend-guardias.log" "Green"
        Start-Sleep -Seconds 3
        
        # Frontend último (más rápido)
        Start-ServiceWithLogging "Frontend Vue.js" "npm run dev" "$PWD\apps\frontend" "frontend.log" "Cyan"
        
        Write-Host "[OK] Stack completo iniciandose..." -ForegroundColor Green
        Write-Host "[INFO] El frontend estara disponible en ~30 segundos" -ForegroundColor Yellow
        Write-Host "[INFO] Los backends tardaran 60-90 segundos en estar listos" -ForegroundColor Yellow
        
        Show-DevelopmentInfo
        Show-QuickCommands
    }
    default {
        Write-Host "[ERROR] Accion no reconocida: $Action" -ForegroundColor Red
        Show-QuickCommands
        exit 1
    }
}

Write-Host "[OK] Modo desarrollo activado. Happy coding!" -ForegroundColor Green
