<template>
  <div class="estadisticas-container">
    <h1>📊 Dashboard de Estadísticas</h1>
    
    <!-- Métricas Generales -->
    <div class="metricas-grid">
      <div class="metrica-card">
        <div class="metrica-icon">👥</div>
        <div class="metrica-info">
          <h3>{{ totalProfesores }}</h3>
          <p>Profesores Activos</p>
        </div>
      </div>
      
      <div class="metrica-card">
        <div class="metrica-icon">📋</div>
        <div class="metrica-info">
          <h3>{{ totalGuardias }}</h3>
          <p>Guardias Realizadas</p>
        </div>
      </div>
      
      <div class="metrica-card">
        <div class="metrica-icon">⚠️</div>
        <div class="metrica-info">
          <h3>{{ totalProblematicas }}</h3>
          <p>Guardias Problemáticas</p>
        </div>
      </div>
      
      <div class="metrica-card">
        <div class="metrica-icon">🎯</div>
        <div class="metrica-info">
          <h3>{{ totalConvivencia }}</h3>
          <p>Aula Convivencia</p>
        </div>
      </div>
    </div>

    <!-- Dashboard de Equidad -->
    <div class="dashboard-section">
      <h2>⚖️ Dashboard de Equidad</h2>
      <div class="filtros-dashboard">
        <select v-model="filtroTipo" @change="actualizarGraficos">
          <option value="total">Total de Guardias</option>
          <option value="normales">Guardias Normales</option>
          <option value="problematicas">Guardias Problemáticas</option>
          <option value="convivencia">Aula de Convivencia</option>
        </select>
      </div>
      
      <div class="grafico-container">
        <canvas ref="graficoEquidad" width="800" height="400"></canvas>
      </div>
    </div>

    <!-- Reporte de Desbalance -->
    <div class="dashboard-section">
      <h2>📈 Reporte de Desbalance</h2>
      <div class="tabla-desbalance">
        <table v-if="reporteDesbalance.length">
          <thead>
            <tr>
              <th>Profesor</th>
              <th>Guardias Normales</th>
              <th>Guardias Problemáticas</th>
              <th>Aula Convivencia</th>
              <th>Total</th>
              <th>Estado</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="profesor in reporteDesbalance" :key="profesor.profesorEmail" 
                :class="getEstadoClass(profesor)">
              <td>{{ emailAcortado(profesor.profesorEmail) }}</td>
              <td>{{ profesor.guardiasNormales }}</td>
              <td>{{ profesor.guardiasProblematicas }}</td>
              <td>{{ profesor.guardiasConvivencia }}</td>
              <td><strong>{{ profesor.totalGuardias }}</strong></td>
              <td>
                <span :class="'badge ' + getEstadoBadge(profesor)">
                  {{ getEstadoTexto(profesor) }}
                </span>
              </td>
            </tr>
          </tbody>
        </table>
        <p v-else>Cargando reporte de desbalance...</p>
      </div>
    </div>

    <!-- Estadísticas por Día y Hora -->
    <div class="dashboard-section">
      <h2>📅 Distribución por Día y Hora</h2>
      <div class="filtros-dia-hora">
        <select v-model="diaSeleccionado" @change="cargarEstadisticasDiaHora">
          <option value="">Todos los días</option>
          <option value="LUNES">Lunes</option>
          <option value="MARTES">Martes</option>
          <option value="MIERCOLES">Miércoles</option>
          <option value="JUEVES">Jueves</option>
          <option value="VIERNES">Viernes</option>
        </select>
        
        <select v-model="horaSeleccionada" @change="cargarEstadisticasDiaHora">
          <option value="">Todas las horas</option>
          <option v-for="hora in 6" :key="hora" :value="hora">
            {{ hora }}ª Hora
          </option>
        </select>
      </div>
      
      <div class="grid-dia-hora" v-if="estadisticasDiaHora.length">
        <div v-for="estadistica in estadisticasDiaHora" :key="estadistica.id" 
             class="card-dia-hora">
          <h4>{{ emailAcortado(estadistica.profesorEmail) }}</h4>
          <p>{{ estadistica.diaSemana }} - {{ estadistica.hora }}ª</p>
          <div class="contadores-mini">
            <span>N: {{ estadistica.guardiasNormales }}</span>
            <span>P: {{ estadistica.guardiasProblematicas }}</span>
            <span>C: {{ estadistica.guardiasConvivencia }}</span>
          </div>
        </div>
      </div>
      <p v-else-if="diaSeleccionado || horaSeleccionada">
        No hay datos para los filtros seleccionados
      </p>
    </div>

    <!-- Alertas del Sistema -->
    <div class="dashboard-section" v-if="alertas.length">
      <h2>🚨 Alertas del Sistema</h2>
      <div class="alertas-container">
        <div v-for="alerta in alertas" :key="alerta.id" 
             :class="'alerta ' + alerta.tipo">
          <div class="alerta-icon">
            {{ alerta.tipo === 'warning' ? '⚠️' : alerta.tipo === 'error' ? '❌' : 'ℹ️' }}
          </div>
          <div class="alerta-content">
            <h4>{{ alerta.titulo }}</h4>
            <p>{{ alerta.mensaje }}</p>
            <small>{{ formatearFecha(alerta.fecha) }}</small>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useToast } from 'vue-toastification'
import { 
  getDashboardEquidad, 
  getReporteDesbalance, 
  getContadoresPorDiaHora,
  getContadoresProfesor
} from '@/services/apiService'

const toast = useToast()

// Estados reactivos
const totalProfesores = ref(0)
const totalGuardias = ref(0)
const totalProblematicas = ref(0)
const totalConvivencia = ref(0)
const dashboardEquidad = ref({})
const reporteDesbalance = ref([])
const estadisticasDiaHora = ref([])
const alertas = ref([])

// Filtros
const filtroTipo = ref('total')
const diaSeleccionado = ref('')
const horaSeleccionada = ref('')

// Referencias
const graficoEquidad = ref(null)

// Cargar datos al montar
onMounted(async () => {
  await Promise.all([
    cargarDashboardEquidad(),
    cargarReporteDesbalance(),
    cargarEstadisticasDiaHora(),
    generarAlertas()
  ])
  
  // Dar tiempo a que se monte el canvas
  setTimeout(() => {
    actualizarGraficos()
  }, 100)
})

// Métodos para cargar datos
const cargarDashboardEquidad = async () => {
  try {
    const response = await getDashboardEquidad()
    dashboardEquidad.value = response.data
    
    // Actualizar métricas generales
    totalProfesores.value = dashboardEquidad.value.totalProfesores || 0
    totalGuardias.value = dashboardEquidad.value.totalGuardias || 0
    totalProblematicas.value = dashboardEquidad.value.totalProblematicas || 0
    totalConvivencia.value = dashboardEquidad.value.totalConvivencia || 0
  } catch (error) {
    console.error('Error cargando dashboard de equidad:', error)
    toast.error('Error al cargar estadísticas generales')
  }
}

const cargarReporteDesbalance = async () => {
  try {
    const response = await getReporteDesbalance()
    reporteDesbalance.value = response.data || []
  } catch (error) {
    console.error('Error cargando reporte de desbalance:', error)
    toast.error('Error al cargar reporte de desbalance')
  }
}

const cargarEstadisticasDiaHora = async () => {
  try {
    if (!diaSeleccionado.value && !horaSeleccionada.value) {
      estadisticasDiaHora.value = []
      return
    }
    
    const response = await getContadoresPorDiaHora(diaSeleccionado.value, horaSeleccionada.value)
    estadisticasDiaHora.value = response.data || []
  } catch (error) {
    console.error('Error cargando estadísticas día/hora:', error)
    toast.error('Error al cargar estadísticas por día y hora')
  }
}

const generarAlertas = async () => {
  // Generar alertas basadas en los datos
  const alertasGeneradas = []
  
  // Alerta de desbalance extremo
  const profesoresDesbalanceados = reporteDesbalance.value.filter(p => 
    Math.abs(p.totalGuardias - (totalGuardias.value / totalProfesores.value)) > 3
  )
  
  if (profesoresDesbalanceados.length > 0) {
    alertasGeneradas.push({
      id: 'desbalance-extremo',
      tipo: 'warning',
      titulo: 'Desbalance Detectado',
      mensaje: `${profesoresDesbalanceados.length} profesores tienen una carga muy desigual de guardias`,
      fecha: new Date()
    })
  }
  
  // Alerta de baja cobertura
  if (totalGuardias.value < totalProfesores.value * 2) {
    alertasGeneradas.push({
      id: 'baja-cobertura',
      tipo: 'info',
      titulo: 'Cobertura Baja',
      mensaje: 'El promedio de guardias por profesor es bajo. Considere redistribuir.',
      fecha: new Date()
    })
  }
  
  alertas.value = alertasGeneradas
}

// Métodos de gráficos
const actualizarGraficos = () => {
  if (!graficoEquidad.value) return
  
  const ctx = graficoEquidad.value.getContext('2d')
  const data = reporteDesbalance.value
  
  if (!data.length) return
  
  // Limpiar canvas
  ctx.clearRect(0, 0, 800, 400)
  
  // Configuración del gráfico
  const padding = 60
  const chartWidth = 800 - 2 * padding
  const chartHeight = 400 - 2 * padding
  
  // Obtener datos según filtro
  const valores = data.map(p => {
    switch (filtroTipo.value) {
      case 'normales': return p.guardiasNormales
      case 'problematicas': return p.guardiasProblematicas
      case 'convivencia': return p.guardiasConvivencia
      default: return p.totalGuardias
    }
  })
  
  const maxValor = Math.max(...valores, 1)
  const labels = data.map(p => p.profesorEmail.split('@')[0].substring(0, 8))
  
  // Dibujar ejes
  ctx.strokeStyle = '#666'
  ctx.lineWidth = 1
  
  // Eje Y
  ctx.beginPath()
  ctx.moveTo(padding, padding)
  ctx.lineTo(padding, padding + chartHeight)
  ctx.stroke()
  
  // Eje X
  ctx.beginPath()
  ctx.moveTo(padding, padding + chartHeight)
  ctx.lineTo(padding + chartWidth, padding + chartHeight)
  ctx.stroke()
  
  // Dibujar barras
  const barWidth = chartWidth / data.length * 0.8
  const barSpacing = chartWidth / data.length * 0.2
  
  valores.forEach((valor, index) => {
    const barHeight = (valor / maxValor) * chartHeight
    const x = padding + index * (barWidth + barSpacing) + barSpacing / 2
    const y = padding + chartHeight - barHeight
    
    // Color según el valor
    const ratio = valor / maxValor
    ctx.fillStyle = ratio > 0.8 ? '#ff4757' : ratio > 0.5 ? '#ffa502' : '#2ed573'
    
    ctx.fillRect(x, y, barWidth, barHeight)
    
    // Etiqueta del valor
    ctx.fillStyle = '#333'
    ctx.font = '12px Arial'
    ctx.textAlign = 'center'
    ctx.fillText(valor.toString(), x + barWidth / 2, y - 5)
    
    // Etiqueta del profesor
    ctx.save()
    ctx.translate(x + barWidth / 2, padding + chartHeight + 15)
    ctx.rotate(-Math.PI / 4)
    ctx.textAlign = 'right'
    ctx.fillText(labels[index], 0, 0)
    ctx.restore()
  })
  
  // Título del gráfico
  ctx.fillStyle = '#333'
  ctx.font = 'bold 16px Arial'
  ctx.textAlign = 'center'
  ctx.fillText(`Distribución de ${filtroTipo.value}`, 400, 30)
  
  // Línea de promedio
  const promedio = valores.reduce((a, b) => a + b, 0) / valores.length
  const promedioY = padding + chartHeight - (promedio / maxValor) * chartHeight
  
  ctx.strokeStyle = '#ff6b6b'
  ctx.lineWidth = 2
  ctx.setLineDash([5, 5])
  ctx.beginPath()
  ctx.moveTo(padding, promedioY)
  ctx.lineTo(padding + chartWidth, promedioY)
  ctx.stroke()
  ctx.setLineDash([])
  
  // Etiqueta del promedio
  ctx.fillStyle = '#ff6b6b'
  ctx.font = '12px Arial'
  ctx.textAlign = 'left'
  ctx.fillText(`Promedio: ${promedio.toFixed(1)}`, padding + 10, promedioY - 5)
}

// Métodos auxiliares
const emailAcortado = (email) => {
  if (!email) return 'N/A'
  const index = email.indexOf('@')
  return index > 0 ? email.substring(0, index) : email.substring(0, 10)
}

const getEstadoClass = (profesor) => {
  const promedio = totalGuardias.value / totalProfesores.value
  const diferencia = Math.abs(profesor.totalGuardias - promedio)
  
  if (diferencia > 3) return 'desbalance-alto'
  if (diferencia > 1.5) return 'desbalance-medio'
  return 'equilibrado'
}

const getEstadoBadge = (profesor) => {
  const promedio = totalGuardias.value / totalProfesores.value
  const diferencia = Math.abs(profesor.totalGuardias - promedio)
  
  if (diferencia > 3) return 'badge-danger'
  if (diferencia > 1.5) return 'badge-warning'
  return 'badge-success'
}

const getEstadoTexto = (profesor) => {
  const promedio = totalGuardias.value / totalProfesores.value
  const diferencia = Math.abs(profesor.totalGuardias - promedio)
  
  if (diferencia > 3) return 'Desbalanceado'
  if (diferencia > 1.5) return 'Moderado'
  return 'Equilibrado'
}

const formatearFecha = (fecha) => {
  return new Intl.DateTimeFormat('es-ES', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  }).format(new Date(fecha))
}
</script>

<style scoped>
.estadisticas-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  font-family: 'Montserrat', sans-serif;
}

.metricas-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.metrica-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 20px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  box-shadow: 0 4px 15px rgba(0,0,0,0.1);
}

.metrica-icon {
  font-size: 2.5em;
  margin-right: 15px;
}

.metrica-info h3 {
  font-size: 2em;
  margin: 0;
  font-weight: 600;
}

.metrica-info p {
  margin: 5px 0 0 0;
  opacity: 0.9;
}

.dashboard-section {
  background: white;
  border-radius: 12px;
  padding: 25px;
  margin-bottom: 25px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
}

.dashboard-section h2 {
  margin-top: 0;
  color: #333;
  border-bottom: 2px solid #f0f0f0;
  padding-bottom: 10px;
}

.filtros-dashboard, .filtros-dia-hora {
  display: flex;
  gap: 15px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.filtros-dashboard select, .filtros-dia-hora select {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  background: white;
  min-width: 150px;
}

.grafico-container {
  text-align: center;
  margin: 20px 0;
}

.grafico-container canvas {
  border: 1px solid #eee;
  border-radius: 8px;
  max-width: 100%;
  height: auto;
}

.tabla-desbalance {
  overflow-x: auto;
}

.tabla-desbalance table {
  width: 100%;
  border-collapse: collapse;
  margin-top: 15px;
}

.tabla-desbalance th,
.tabla-desbalance td {
  padding: 12px;
  text-align: left;
  border-bottom: 1px solid #eee;
}

.tabla-desbalance th {
  background: #f8f9fa;
  font-weight: 600;
  color: #333;
}

.equilibrado {
  background-color: rgba(46, 213, 115, 0.1);
}

.desbalance-medio {
  background-color: rgba(255, 165, 2, 0.1);
}

.desbalance-alto {
  background-color: rgba(255, 71, 87, 0.1);
}

.badge {
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
  text-transform: uppercase;
}

.badge-success {
  background: #2ed573;
  color: white;
}

.badge-warning {
  background: #ffa502;
  color: white;
}

.badge-danger {
  background: #ff4757;
  color: white;
}

.grid-dia-hora {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 15px;
  margin-top: 15px;
}

.card-dia-hora {
  background: #f8f9fa;
  padding: 15px;
  border-radius: 8px;
  border-left: 4px solid #667eea;
}

.card-dia-hora h4 {
  margin: 0 0 5px 0;
  color: #333;
}

.card-dia-hora p {
  margin: 0 0 10px 0;
  color: #666;
  font-size: 14px;
}

.contadores-mini {
  display: flex;
  gap: 10px;
}

.contadores-mini span {
  background: #667eea;
  color: white;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 600;
}

.alertas-container {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.alerta {
  display: flex;
  align-items: flex-start;
  gap: 15px;
  padding: 15px;
  border-radius: 8px;
  border-left: 4px solid;
}

.alerta.warning {
  background: rgba(255, 165, 2, 0.1);
  border-left-color: #ffa502;
}

.alerta.error {
  background: rgba(255, 71, 87, 0.1);
  border-left-color: #ff4757;
}

.alerta.info {
  background: rgba(102, 126, 234, 0.1);
  border-left-color: #667eea;
}

.alerta-icon {
  font-size: 1.5em;
  flex-shrink: 0;
}

.alerta-content h4 {
  margin: 0 0 5px 0;
  color: #333;
}

.alerta-content p {
  margin: 0 0 5px 0;
  color: #666;
}

.alerta-content small {
  color: #999;
  font-size: 12px;
}

@media (max-width: 768px) {
  .metricas-grid {
    grid-template-columns: 1fr;
  }
  
  .filtros-dashboard, .filtros-dia-hora {
    flex-direction: column;
  }
  
  .grafico-container canvas {
    width: 100%;
    height: 300px;
  }
  
  .grid-dia-hora {
    grid-template-columns: 1fr;
  }
}
</style>
