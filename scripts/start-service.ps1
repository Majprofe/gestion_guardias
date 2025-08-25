# ============================================================================
# 🚀 SCRIPT DE INICIO - Sistema de Gestión de Guardias
# ============================================================================
# Ejecuta todos los servicios del proyecto: Frontend + Backends
# Variables de entorno centralizadas en .env raíz

param(
    [string]$Service = "all"  # Opciones: all, frontend, backend, horarios
)

Write-Host "🌟 Sistema de Gestión de Guardias - Iniciando servicios..." -ForegroundColor Green
Write-Host "============================================================" -ForegroundColor Cyan

# Función para iniciar un servicio en una nueva ventana
function Start-ServiceInNewWindow {
    param(
        [string]$ServiceName,
        [string]$Command,
        [string]$WorkingDirectory
    )
    
    Write-Host "🚀 Iniciando $ServiceName..." -ForegroundColor Yellow
    
    $arguments = @(
        "-NoExit",
        "-Command",
        "Set-Location '$WorkingDirectory'; Write-Host '📍 Directorio: $WorkingDirectory' -ForegroundColor Cyan; $Command"
    )
    
    Start-Process powershell -ArgumentList $arguments
}

# Verificar que existe el archivo .env
if (-not (Test-Path ".env")) {
    Write-Host "❌ Error: No se encontró el archivo .env en el directorio raíz" -ForegroundColor Red
    exit 1
}

Write-Host "✅ Archivo .env encontrado - Variables centralizadas" -ForegroundColor Green

# Función para mostrar información del servicio
function Show-ServiceInfo {
    Write-Host "`n📋 INFORMACIÓN DE SERVICIOS:" -ForegroundColor Magenta
    Write-Host "├── 🌐 Frontend (Vue.js):     https://localhost:5500" -ForegroundColor White
    Write-Host "├── 🔧 Backend Guardias:      http://localhost:8081/swagger-ui.html" -ForegroundColor White
    Write-Host "└── 📊 Backend Horarios:      http://localhost:8082/swagger-ui.html" -ForegroundColor White
    Write-Host ""
}

# Obtener la ruta absoluta del proyecto
$projectRoot = Get-Location

# Ejecutar servicios según el parámetro
switch ($Service.ToLower()) {
    "frontend" {
        Write-Host "🌐 Iniciando solo Frontend..." -ForegroundColor Cyan
        Start-ServiceInNewWindow "Frontend Vue.js" "npm run dev" "$projectRoot\apps\frontend"
    }
    "backend" {
        Write-Host "🔧 Iniciando solo Backend Guardias..." -ForegroundColor Cyan
        Start-ServiceInNewWindow "Backend Guardias" ".\mvnw.cmd spring-boot:run" "$projectRoot\apps\backend"
    }
    "horarios" {
        Write-Host "📊 Iniciando solo Backend Horarios..." -ForegroundColor Cyan
        Start-ServiceInNewWindow "Backend Horarios" ".\mvnw.cmd spring-boot:run" "$projectRoot\apps\horarios"
    }
    "all" {
        Write-Host "🚀 Iniciando todos los servicios..." -ForegroundColor Cyan
        Write-Host "⏳ Espere mientras se abren las ventanas de cada servicio..." -ForegroundColor Yellow
        
        # Iniciar todos los servicios con un pequeño delay
        Start-ServiceInNewWindow "Frontend Vue.js" "npm run dev" "$projectRoot\apps\frontend"
        Start-Sleep -Seconds 2
        
        Start-ServiceInNewWindow "Backend Guardias" ".\mvnw.cmd spring-boot:run" "$projectRoot\apps\backend"
        Start-Sleep -Seconds 2
        
        Start-ServiceInNewWindow "Backend Horarios" ".\mvnw.cmd spring-boot:run" "$projectRoot\apps\horarios"
        
        Write-Host "`n✅ Todos los servicios están iniciándose..." -ForegroundColor Green
        Write-Host "⚡ Las ventanas de PowerShell se abrirán automáticamente" -ForegroundColor Yellow
    }
    default {
        Write-Host "❌ Servicio no reconocido: $Service" -ForegroundColor Red
        Write-Host "📋 Opciones válidas: all, frontend, backend, horarios" -ForegroundColor Yellow
        exit 1
    }
}

Show-ServiceInfo

Write-Host "🎯 Servicios solicitados en proceso de inicio..." -ForegroundColor Green
Write-Host "📝 Las variables de entorno se cargan automáticamente desde .env" -ForegroundColor Cyan
Write-Host "============================================================" -ForegroundColor Cyan

# Ejemplos de uso:
Write-Host "`n📚 EJEMPLOS DE USO:" -ForegroundColor Magenta
Write-Host "├── .\start-service.ps1                 # Inicia todos los servicios" -ForegroundColor White  
Write-Host "├── .\start-service.ps1 frontend        # Solo frontend" -ForegroundColor White
Write-Host "├── .\start-service.ps1 backend         # Solo backend guardias" -ForegroundColor White
Write-Host "└── .\start-service.ps1 horarios        # Solo backend horarios" -ForegroundColor White
