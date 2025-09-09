@echo off
echo 🔧 DIAGNÓSTICO DE BACKEND - Paso a paso
echo ==========================================

echo.
echo 1. Probando endpoint simple del backend principal...
curl -s http://localhost:8081/api/public/simple
echo.

echo.
echo 2. Probando health check del backend principal...
curl -s http://localhost:8081/api/public/health  
echo.

echo.
echo 3. Probando actividades del backend de horarios DIRECTAMENTE...
curl -s http://localhost:8082/actividades
echo.

echo.
echo 4. Probando actividades a través del PROXY...
curl -s http://localhost:8081/api/horarios/actividades
echo.

echo.
echo 5. Probando health check del PROXY...
curl -s http://localhost:8081/api/horarios/health
echo.

echo ==========================================
echo ✅ Diagnóstico completado!
pause
