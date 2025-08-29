@echo off
echo =========================================
echo   Sistema de Gestion de Guardias - DEV
echo =========================================

echo.
echo 1. Verificando puertos...
netstat -ano | findstr ":8081" >nul
if %errorlevel% == 0 (
    echo ADVERTENCIA: Puerto 8081 ocupado
    for /f "tokens=5" %%p in ('netstat -ano ^| findstr ":8081"') do (
        echo Terminando proceso %%p...
        taskkill /F /PID %%p >nul 2>&1
    )
)

netstat -ano | findstr ":8082" >nul
if %errorlevel% == 0 (
    echo ADVERTENCIA: Puerto 8082 ocupado
    for /f "tokens=5" %%p in ('netstat -ano ^| findstr ":8082"') do (
        echo Terminando proceso %%p...
        taskkill /F /PID %%p >nul 2>&1
    )
)

netstat -ano | findstr ":5500" >nul
if %errorlevel% == 0 (
    echo ADVERTENCIA: Puerto 5500 ocupado
    for /f "tokens=5" %%p in ('netstat -ano ^| findstr ":5500"') do (
        echo Terminando proceso %%p...
        taskkill /F /PID %%p >nul 2>&1
    )
)

timeout /t 2 >nul

echo.
echo 2. Iniciando Backend Guardias (puerto 8081)...
start "Backend Guardias" cmd /k "cd apps\backend && mvn spring-boot:run"

timeout /t 8 /nobreak >nul

echo 3. Iniciando Backend Horarios (puerto 8082)...
start "Backend Horarios" cmd /k "cd apps\horarios && mvn spring-boot:run"

timeout /t 8 /nobreak >nul

echo 4. Iniciando Frontend (puerto 5500)...
start "Frontend" cmd /k "cd apps\frontend && npm run dev"

echo.
echo =========================================
echo   Todos los servicios iniciados
echo =========================================
echo Frontend:  http://localhost:5500
echo Guardias:  http://localhost:8081
echo Horarios:  http://localhost:8082
echo =========================================
pause
