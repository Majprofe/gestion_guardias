#!/bin/bash
# Script para probar todos los endpoints del proxy

echo "🧪 TESTING PROXY ENDPOINTS - Backend Unificado"
echo "================================================"

BASE_URL="http://localhost:8081"

echo ""
echo "🔍 1. Health Check - Backend Principal"
curl -s "$BASE_URL/health" | jq 2>/dev/null || curl -s "$BASE_URL/health"

echo ""
echo "🔍 2. Health Check - Servicio Horarios (a través del proxy)"
curl -s "$BASE_URL/api/horarios/health" | jq 2>/dev/null || curl -s "$BASE_URL/api/horarios/health"

echo ""
echo "📚 3. Actividades"
curl -s "$BASE_URL/api/horarios/actividades" | jq '. | length' 2>/dev/null || echo "Error o sin jq"

echo ""
echo "📖 4. Asignaturas"
curl -s "$BASE_URL/api/horarios/asignaturas" | jq '. | length' 2>/dev/null || echo "Error o sin jq"

echo ""
echo "🏛️ 5. Aulas"
curl -s "$BASE_URL/api/horarios/aulas" | jq '. | length' 2>/dev/null || echo "Error o sin jq"

echo ""
echo "👥 6. Grupos"
curl -s "$BASE_URL/api/horarios/grupos" | jq '. | length' 2>/dev/null || echo "Error o sin jq"

echo ""
echo "⏰ 7. Tramos Horarios"
curl -s "$BASE_URL/api/horarios/tramohorarios" | jq '. | length' 2>/dev/null || echo "Error o sin jq"

echo ""
echo "👨‍🏫 8. Profesor por Email (ejemplo)"
curl -s "$BASE_URL/api/horarios/horario/profesor/email?email=test@iesjandula.es" | jq 2>/dev/null || curl -s "$BASE_URL/api/horarios/horario/profesor/email?email=test@iesjandula.es"

echo ""
echo "================================================"
echo "✅ Tests completados!"
echo "💡 Si ves errores, verifica que ambos backends estén ejecutándose:"
echo "   - Backend Principal: http://localhost:8081"
echo "   - Backend Horarios: http://localhost:8082"
