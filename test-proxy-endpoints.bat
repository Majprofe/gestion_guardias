@echo off
echo 🧪 TESTING PROXY ENDPOINTS - Backend Unificado
echo ================================================

set BASE_URL=http://localhost:8081

echo.
echo 🔍 1. Health Check - Backend Principal
curl -s "%BASE_URL%/api/public/health"

echo.
echo 🔍 2. Health Check - Servicio Horarios (a través del proxy)
curl -s "%BASE_URL%/api/horarios/health"

echo.
echo 📚 3. Actividades
curl -s "%BASE_URL%/api/horarios/actividades"

echo.
echo 📖 4. Asignaturas  
curl -s "%BASE_URL%/api/horarios/asignaturas"

echo.
echo 🏛️ 5. Aulas
curl -s "%BASE_URL%/api/horarios/aulas"

echo.
echo 👥 6. Grupos
curl -s "%BASE_URL%/api/horarios/grupos"

echo.
echo ⏰ 7. Tramos Horarios
curl -s "%BASE_URL%/api/horarios/tramohorarios"

echo.
echo 👨‍🏫 8. Profesor por Email (ejemplo)
curl -s "%BASE_URL%/api/horarios/horario/profesor/email?email=test@iesjandula.es"

echo.
echo ================================================
echo ✅ Tests completados!
echo 💡 Si ves errores, verifica que ambos backends estén ejecutándose:
echo    - Backend Principal: http://localhost:8081
echo    - Backend Horarios: http://localhost:8082

pause
