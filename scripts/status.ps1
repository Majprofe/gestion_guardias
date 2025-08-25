# ============================================================================
# 📊 SCRIPT DE ESTADO - Sistema de Gestión de Guardias
# ============================================================================
# Verifica el estado de todos los servicios del proyecto
# Uso: .\status.ps1

Write-Host @"
╔══════════════════════════════════════════════════════════════════════════════╗
║                     📊 ESTADO DEL SISTEMA                                    ║
║                    Sistema de Gestión de Guardias                           ║
╚══════════════════════════════════════════════════════════════════════════════╝
"@ -ForegroundColor Cyan

# Función para verificar si un puerto está en uso
function Test-Port {
    param([int]$Port)
    $netstat = netstat -ano | Select-String ":$Port "
    return $netstat -ne $null
}

# Función para hacer una petición HTTP simple
function Test-HttpService {
    param([string]$Url, [int]$TimeoutSeconds = 5)
    try {
        $response = Invoke-WebRequest -Uri $Url -Method Get -TimeoutSec $TimeoutSeconds -UseBasicParsing -ErrorAction Stop
        return $response.StatusCode -eq 200
    } catch {
        return $false
    }
}

# Función para verificar si un proceso está corriendo
function Test-Process {
    param([string]$ProcessName, [string]$CommandLinePattern = "")
    $processes = Get-Process -Name $ProcessName -ErrorAction SilentlyContinue
    if ($CommandLinePattern -and $processes) {
        return $processes | Where-Object { $_.CommandLine -like "*$CommandLinePattern*" } | Measure-Object | Select-Object -ExpandProperty Count -gt 0
    }
    return $processes -ne $null
}

Write-Host "🔍 Verificando estado de servicios..." -ForegroundColor Yellow
Write-Host ""

# Verificar Frontend (Puerto 5500)
Write-Host "🌐 FRONTEND (Vue.js + Vite)" -ForegroundColor Cyan
Write-Host "├── Puerto 5500: " -NoNewline
if (Test-Port 5500) {
    Write-Host "✅ Ocupado" -ForegroundColor Green
    Write-Host "├── Proceso Node.js: " -NoNewline
    if (Test-Process "node" "vite") {
        Write-Host "✅ Corriendo" -ForegroundColor Green
    } else {
        Write-Host "⚠️  No encontrado" -ForegroundColor Yellow
    }
    Write-Host "├── Servicio HTTP: " -NoNewline
    if (Test-HttpService "https://localhost:5500" 3) {
        Write-Host "✅ Respondiendo" -ForegroundColor Green
        Write-Host "└── URL: https://localhost:5500" -ForegroundColor White
    } else {
        Write-Host "❌ No responde" -ForegroundColor Red
        Write-Host "└── URL: https://localhost:5500 (Verificar manualmente)" -ForegroundColor Gray
    }
} else {
    Write-Host "❌ Libre" -ForegroundColor Red
    Write-Host "└── Estado: Servicio no iniciado" -ForegroundColor Gray
}

Write-Host ""

# Verificar Backend Guardias (Puerto 8081)
Write-Host "🔧 BACKEND GUARDIAS (Spring Boot)" -ForegroundColor Green
Write-Host "├── Puerto 8081: " -NoNewline
if (Test-Port 8081) {
    Write-Host "✅ Ocupado" -ForegroundColor Green
    Write-Host "├── Proceso Java: " -NoNewline
    if (Test-Process "java" "8081") {
        Write-Host "✅ Corriendo" -ForegroundColor Green
    } else {
        Write-Host "⚠️  No encontrado" -ForegroundColor Yellow
    }
    Write-Host "├── Health Check: " -NoNewline
    if (Test-HttpService "http://localhost:8081/actuator/health" 3) {
        Write-Host "✅ Saludable" -ForegroundColor Green
    } else {
        Write-Host "⚠️  Iniciando..." -ForegroundColor Yellow
    }
    Write-Host "├── API: http://localhost:8081" -ForegroundColor White
    Write-Host "└── Swagger: http://localhost:8081/swagger-ui.html" -ForegroundColor White
} else {
    Write-Host "❌ Libre" -ForegroundColor Red
    Write-Host "└── Estado: Servicio no iniciado" -ForegroundColor Gray
}

Write-Host ""

# Verificar Backend Horarios (Puerto 8082)
Write-Host "📊 BACKEND HORARIOS (Spring Boot)" -ForegroundColor Magenta
Write-Host "├── Puerto 8082: " -NoNewline
if (Test-Port 8082) {
    Write-Host "✅ Ocupado" -ForegroundColor Green
    Write-Host "├── Proceso Java: " -NoNewline
    if (Test-Process "java" "8082") {
        Write-Host "✅ Corriendo" -ForegroundColor Green
    } else {
        Write-Host "⚠️  No encontrado" -ForegroundColor Yellow
    }
    Write-Host "├── Health Check: " -NoNewline
    if (Test-HttpService "http://localhost:8082/actuator/health" 3) {
        Write-Host "✅ Saludable" -ForegroundColor Green
    } else {
        Write-Host "⚠️  Iniciando..." -ForegroundColor Yellow
    }
    Write-Host "├── API: http://localhost:8082" -ForegroundColor White
    Write-Host "└── Swagger: http://localhost:8082/swagger-ui.html" -ForegroundColor White
} else {
    Write-Host "❌ Libre" -ForegroundColor Red
    Write-Host "└── Estado: Servicio no iniciado" -ForegroundColor Gray
}

Write-Host ""

# Verificar configuración del proyecto
Write-Host "⚙️  CONFIGURACIÓN DEL PROYECTO" -ForegroundColor Yellow
Write-Host "├── Archivo .env: " -NoNewline
if (Test-Path ".env") {
    Write-Host "✅ Encontrado" -ForegroundColor Green
} else {
    Write-Host "❌ No encontrado" -ForegroundColor Red
}

Write-Host "├── Dependencias Frontend: " -NoNewline
if (Test-Path "apps\frontend\node_modules") {
    Write-Host "✅ Instaladas" -ForegroundColor Green
} else {
    Write-Host "❌ Faltan" -ForegroundColor Red
}

Write-Host "├── Logs de desarrollo: " -NoNewline
if (Test-Path "logs") {
    $logFiles = Get-ChildItem "logs" -File | Measure-Object | Select-Object -ExpandProperty Count
    Write-Host "✅ $logFiles archivos" -ForegroundColor Green
} else {
    Write-Host "ℹ️  No creados" -ForegroundColor Gray
}

Write-Host "└── Certificados SSL: " -NoNewline
if ((Test-Path "apps\frontend\cert.pem") -and (Test-Path "apps\frontend\key.pem")) {
    Write-Host "✅ Disponibles" -ForegroundColor Green
} else {
    Write-Host "⚠️  Faltan" -ForegroundColor Yellow
}

# Resumen general
Write-Host ""
$frontendRunning = Test-Port 5500
$guardiasRunning = Test-Port 8081
$horariosRunning = Test-Port 8082

if ($frontendRunning -and $guardiasRunning -and $horariosRunning) {
    $status = "🟢 COMPLETAMENTE OPERATIVO"
    $color = "Green"
} elseif ($frontendRunning -or $guardiasRunning -or $horariosRunning) {
    $status = "🟡 PARCIALMENTE OPERATIVO"
    $color = "Yellow"
} else {
    $status = "🔴 SERVICIOS DETENIDOS"
    $color = "Red"
}

Write-Host @"
╔══════════════════════════════════════════════════════════════════════════════╗
║                          $status                          ║
╚══════════════════════════════════════════════════════════════════════════════╝
"@ -ForegroundColor $color

Write-Host ""
Write-Host "⚡ COMANDOS ÚTILES:" -ForegroundColor Cyan
Write-Host "├── .\dev.ps1              # Inicio rápido de todos los servicios"
Write-Host "├── .\start-dev.ps1        # Inicio con logs detallados"
Write-Host "├── .\stop-dev.ps1         # Detener todos los servicios" 
Write-Host "└── .\status.ps1           # Ver este estado (ejecutar de nuevo)"
