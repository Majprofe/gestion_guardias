# ============================================================================
# 🔬 SCRIPT DE DESARROLLO - Sistema de Gestión de Guardias
# ============================================================================
# Script optimizado para desarrollo con verificaciones y logging
# Uso: .\start-dev.ps1 [all|frontend|backend|horarios|stop]

param(
    [string]$Action = "all"  # all, frontend, backend, horarios, stop
)

# Configuración de colores y estilo
$Host.UI.RawUI.WindowTitle = "🚀 Sistema Gestión Guardias - Desarrollo"

Write-Host @"
╔══════════════════════════════════════════════════════════════════════════════╗
║                     🔬 MODO DESARROLLO ACTIVADO                              ║
║                    Sistema de Gestión de Guardias                           ║
╚══════════════════════════════════════════════════════════════════════════════╝
"@ -ForegroundColor Cyan

# ============================================================================
# FUNCIONES AUXILIARES
# ============================================================================

function Test-Prerequisites {
    Write-Host "🔍 Verificando prerrequisitos..." -ForegroundColor Yellow
    
    $checks = @()
    
    # Verificar Node.js
    try {
        $nodeVersion = node --version 2>$null
        if ($nodeVersion) {
            $checks += "✅ Node.js: $nodeVersion"
        } else {
            $checks += "❌ Node.js: No encontrado"
            return $false
        }
    } catch {
        $checks += "❌ Node.js: No encontrado"
        return $false
    }
    
    # Verificar Java
    try {
        $javaVersion = java -version 2>&1 | Select-String "version" | ForEach-Object { $_.ToString().Split('"')[1] }
        if ($javaVersion) {
            $checks += "✅ Java: $javaVersion"
        } else {
            $checks += "❌ Java: No encontrado"
            return $false
        }
    } catch {
        $checks += "❌ Java: No encontrado"
        return $false
    }
    
    # Verificar .env
    if (Test-Path ".env") {
        $checks += "✅ Archivo .env: Encontrado"
    } else {
        $checks += "❌ Archivo .env: No encontrado"
        return $false
    }
    
    # Verificar dependencias del frontend
    if (Test-Path "apps\frontend\node_modules") {
        $checks += "✅ Dependencias Frontend: Instaladas"
    } else {
        $checks += "⚠️  Dependencias Frontend: Faltan (se instalarán)"
    }
    
    # Mostrar resultados
    $checks | ForEach-Object { Write-Host "  $_" -ForegroundColor White }
    
    return $true
}

function Install-FrontendDependencies {
    if (-not (Test-Path "apps\frontend\node_modules")) {
        Write-Host "📦 Instalando dependencias del frontend..." -ForegroundColor Yellow
        Set-Location "apps\frontend"
        npm install
        Set-Location "..\..\"
        Write-Host "✅ Dependencias instaladas" -ForegroundColor Green
    }
}

function Stop-AllServices {
    Write-Host "🛑 Deteniendo todos los servicios..." -ForegroundColor Red
    
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
    
    Write-Host "✅ Servicios detenidos" -ForegroundColor Green
}

function Start-ServiceWithLogging {
    param(
        [string]$ServiceName,
        [string]$Command,
        [string]$WorkingDirectory,
        [string]$LogFile,
        [string]$Color = "White"
    )
    
    Write-Host "🚀 Iniciando $ServiceName..." -ForegroundColor $Color
    
    # Crear directorio de logs si no existe
    if (-not (Test-Path "logs")) {
        New-Item -ItemType Directory -Name "logs" | Out-Null
    }
    
    $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    
    $scriptBlock = @"
Set-Location '$WorkingDirectory'
Write-Host "[$timestamp] 📍 Iniciando $ServiceName en: $WorkingDirectory" -ForegroundColor $Color
Write-Host "[$timestamp] 🔧 Comando: $Command" -ForegroundColor Gray
Write-Host "[$timestamp] 📋 Logs guardándose en: logs\$LogFile" -ForegroundColor Gray
Write-Host "============================================================" -ForegroundColor Cyan

# Ejecutar comando con logging
$Command 2>&1 | Tee-Object -FilePath "..\logs\$LogFile"
"@
    
    Start-Process powershell -ArgumentList "-NoExit", "-Command", $scriptBlock
}

function Show-DevelopmentInfo {
    Write-Host @"

📊 PANEL DE DESARROLLO
╔══════════════════════════════════════════════════════════════════════════════╗
║  🌐 Frontend (Vue.js + Vite)                                                 ║
║     └── URL: https://localhost:5500                                         ║
║     └── DevTools: https://localhost:5500/__devtools__/                      ║
║                                                                              ║
║  🔧 Backend Guardias (Spring Boot)                                          ║  
║     └── API: http://localhost:8081                                          ║
║     └── Swagger: http://localhost:8081/swagger-ui.html                      ║
║     └── Health: http://localhost:8081/actuator/health                       ║
║                                                                              ║
║  📊 Backend Horarios (Spring Boot)                                          ║
║     └── API: http://localhost:8082                                          ║
║     └── Swagger: http://localhost:8082/swagger-ui.html                      ║
║     └── Health: http://localhost:8082/actuator/health                       ║
║                                                                              ║
║  📁 Logs de desarrollo: ./logs/                                             ║
╚══════════════════════════════════════════════════════════════════════════════╝
"@ -ForegroundColor Cyan
}

function Show-QuickCommands {
    Write-Host @"

⚡ COMANDOS RÁPIDOS:
├── .\start-dev.ps1 all       # 🚀 Iniciar todo el stack
├── .\start-dev.ps1 frontend  # 🌐 Solo frontend  
├── .\start-dev.ps1 backend   # 🔧 Solo backend guardias
├── .\start-dev.ps1 horarios  # 📊 Solo backend horarios  
├── .\start-dev.ps1 stop      # 🛑 Detener todos los servicios
└── .\start-dev.ps1 help      # 📚 Mostrar esta ayuda

🔧 HERRAMIENTAS DE DESARROLLO:
├── Hot Reload: ✅ Activado en todos los servicios
├── Error Logging: ✅ Centralizado en ./logs/
├── API Documentation: ✅ Swagger disponible
└── Vue DevTools: ✅ Disponible en el navegador

"@ -ForegroundColor Yellow
}

# ============================================================================
# LÓGICA PRINCIPAL
# ============================================================================

# Verificar prerrequisitos
if (-not (Test-Prerequisites)) {
    Write-Host "❌ Faltan prerrequisitos. Por favor instala las dependencias faltantes." -ForegroundColor Red
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
        Write-Host "🌐 Iniciando Frontend en modo desarrollo..." -ForegroundColor Cyan
        Start-ServiceWithLogging "Frontend Vue.js" "npm run dev" "$PWD\apps\frontend" "frontend.log" "Cyan"
        Show-DevelopmentInfo
    }
    "backend" {
        Write-Host "🔧 Iniciando Backend Guardias..." -ForegroundColor Green
        Start-ServiceWithLogging "Backend Guardias" ".\mvnw.cmd spring-boot:run" "$PWD\apps\backend" "backend-guardias.log" "Green"
        Show-DevelopmentInfo
    }
    "horarios" {
        Write-Host "📊 Iniciando Backend Horarios..." -ForegroundColor Magenta
        Start-ServiceWithLogging "Backend Horarios" ".\mvnw.cmd spring-boot:run" "$PWD\apps\horarios" "backend-horarios.log" "Magenta"
        Show-DevelopmentInfo
    }
    "all" {
        Install-FrontendDependencies
        
        Write-Host "🚀 Iniciando stack completo de desarrollo..." -ForegroundColor Green
        Write-Host "⏳ Los servicios se iniciarán en orden con delays para evitar conflictos..." -ForegroundColor Yellow
        
        # Backend Horarios primero (más lento en arrancar)
        Start-ServiceWithLogging "Backend Horarios" ".\mvnw.cmd spring-boot:run" "$PWD\apps\horarios" "backend-horarios.log" "Magenta"
        Start-Sleep -Seconds 3
        
        # Backend Guardias segundo
        Start-ServiceWithLogging "Backend Guardias" ".\mvnw.cmd spring-boot:run" "$PWD\apps\backend" "backend-guardias.log" "Green"
        Start-Sleep -Seconds 3
        
        # Frontend último (más rápido)
        Start-ServiceWithLogging "Frontend Vue.js" "npm run dev" "$PWD\apps\frontend" "frontend.log" "Cyan"
        
        Write-Host "✅ Stack completo iniciándose..." -ForegroundColor Green
        Write-Host "⚡ El frontend estará disponible en ~30 segundos" -ForegroundColor Yellow
        Write-Host "🔧 Los backends tardarán 60-90 segundos en estar listos" -ForegroundColor Yellow
        
        Show-DevelopmentInfo
        Show-QuickCommands
    }
    default {
        Write-Host "❌ Acción no reconocida: $Action" -ForegroundColor Red
        Show-QuickCommands
        exit 1
    }
}

Write-Host "`n🎯 Modo desarrollo activado. ¡Happy coding! 🚀" -ForegroundColor Green
