@echo off
echo =========================================
echo   Deteniendo Sistema de Guardias - DEV
echo =========================================

echo.
echo Buscando y terminando procesos...

echo 1. Deteniendo Backend Guardias (puerto 8081)...
for /f "tokens=5" %%p in ('netstat -ano ^| findstr ":8081"') do (
    echo   Terminando proceso %%p...
    taskkill /F /PID %%p >nul 2>&1
)

echo 2. Deteniendo Backend Horarios (puerto 8082)...
for /f "tokens=5" %%p in ('netstat -ano ^| findstr ":8082"') do (
    echo   Terminando proceso %%p...
    taskkill /F /PID %%p >nul 2>&1
)

echo 3. Deteniendo Frontend (puerto 5173)...
for /f "tokens=5" %%p in ('netstat -ano ^| findstr ":5173"') do (
    echo   Terminando proceso %%p...
    taskkill /F /PID %%p >nul 2>&1
)

echo.
echo =========================================
echo   Todos los servicios detenidos
echo =========================================
pause
