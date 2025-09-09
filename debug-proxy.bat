@echo off
echo ===========================================
echo 🔍 DEBUG DEL PROXY "Horarios Proxy"
echo ===========================================
echo.

echo 📍 Probando endpoints paso a paso...
echo.

echo 1. Verificando que el backend principal esté funcionando...
curl -s http://localhost:8081/actuator/health
echo.
echo.

echo 2. Verificando conectividad con backend de horarios directamente...
curl -s http://localhost:8082/health
echo.
echo.

echo 3. Probando endpoint proxy de salud...
curl -s http://localhost:8081/api/horarios/health
echo.
echo.

echo 4. Probando endpoint proxy de actividades...
curl -s http://localhost:8081/api/horarios/actividades
echo.
echo.

echo 5. Probando endpoint proxy de asignaturas...
curl -s http://localhost:8081/api/horarios/asignaturas
echo.
echo.

echo ===========================================
echo ✅ Pruebas completadas
echo ===========================================
pause
