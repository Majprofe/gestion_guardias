# ============================================================================
# SCRIPT PARA DETENER SERVICIOS 
# ============================================================================
# Detiene todos los servicios del proyecto de desarrollo
# Uso: .\stop-dev.ps1

Write-Host "[*] Deteniendo servicios de desarrollo..." -ForegroundColor Red

# Función para detener procesos de forma segura
function Stop-ServiceProcesses {
    param([string]$processName, [string]$description)
    
    $processes = Get-Process -Name $processName -ErrorAction SilentlyContinue
    if ($processes) {
        Write-Host "[*] Deteniendo $description..." -ForegroundColor Yellow
        $processes | Stop-Process -Force -ErrorAction SilentlyContinue
        Write-Host "[OK] $description detenido" -ForegroundColor Green
    } else {
        Write-Host "[INFO] No hay procesos de $description ejecutandose" -ForegroundColor Gray
    }
}

# Detener servicios específicos por puerto
Write-Host "[*] Buscando servicios en puertos especificos..." -ForegroundColor Yellow

# Detener procesos en puertos específicos
$ports = @(5500, 8081, 8082)
foreach ($port in $ports) {
    $netstat = netstat -ano | Select-String ":$port "
    if ($netstat) {
        $processIds = $netstat | ForEach-Object { ($_ -split '\s+')[-1] } | Sort-Object -Unique
        foreach ($processId in $processIds) {
            try {
                $process = Get-Process -Id $processId -ErrorAction SilentlyContinue
                if ($process) {
                    Write-Host "[*] Deteniendo proceso en puerto $port (PID: $processId)..." -ForegroundColor Yellow
                    Stop-Process -Id $processId -Force -ErrorAction SilentlyContinue
                    Write-Host "[OK] Proceso en puerto $port detenido" -ForegroundColor Green
                }
            } catch {
                # Ignorar errores si el proceso ya no existe
            }
        }
    }
}

# Detener procesos Java (Spring Boot)
Stop-ServiceProcesses "java" "procesos Java (Spring Boot)"

# Detener procesos Node.js (Vite)  
Stop-ServiceProcesses "node" "procesos Node.js (Vite)"

# Limpiar puertos específicos si aún están ocupados
Write-Host "[*] Verificacion final de puertos..." -ForegroundColor Yellow
$portsToCheck = @(5500, 8081, 8082)
$stillBusy = @()

foreach ($port in $portsToCheck) {
    $netstat = netstat -ano | Select-String ":$port "
    if ($netstat) {
        $stillBusy += $port
    }
}

if ($stillBusy.Count -gt 0) {
    Write-Host "[WARNING] Los siguientes puertos aun estan ocupados: $($stillBusy -join ', ')" -ForegroundColor Yellow
    Write-Host "[INFO] Puede que necesites reiniciar manualmente estos procesos" -ForegroundColor Cyan
} else {
    Write-Host "[OK] Todos los puertos estan libres" -ForegroundColor Green
}

Write-Host @"

[OK] SERVICIOS DETENIDOS COMPLETAMENTE

Estado de puertos:
 -> Puerto 5500 (Frontend): Libre
 -> Puerto 8081 (Guardias): Libre  
 -> Puerto 8082 (Horarios): Libre

Para volver a iniciar:
 -> .\dev.ps1                  # Inicio rapido
 -> .\start-dev-simple.ps1     # Inicio con opciones

"@ -ForegroundColor Green
