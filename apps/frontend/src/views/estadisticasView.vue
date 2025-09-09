<template>
  <div class="estadisticas-container">
    <div class="estadisticas-header">
      <h1>📊 Estadísticas del Sistema</h1>
      <div class="header-actions">
        <select v-model="selectedPeriod" @change="loadData" class="period-selector">
          <option value="week">Esta semana</option>
          <option value="month">Este mes</option>
          <option value="quarter">Este trimestre</option>
          <option value="year">Este año</option>
        </select>
        <button @click="refreshData" class="refresh-btn" :disabled="loading">
          <span v-if="loading">🔄</span>
          <span v-else>↻</span>
          Actualizar
        </button>
      </div>
    </div>

    <!-- Tarjetas de resumen -->
    <div class="stats-summary">
      <div class="stat-card">
        <div class="stat-icon">📋</div>
        <div class="stat-content">
          <h3>{{ stats.totalAusencias }}</h3>
          <p>Ausencias Totales</p>
          <span class="stat-change" :class="stats.ausenciasChange >= 0 ? 'positive' : 'negative'">
            {{ stats.ausenciasChange >= 0 ? '+' : '' }}{{ stats.ausenciasChange }}%
          </span>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon">👥</div>
        <div class="stat-content">
          <h3>{{ stats.totalGuardias }}</h3>
          <p>Guardias Realizadas</p>
          <span class="stat-change" :class="stats.guardiasChange >= 0 ? 'positive' : 'negative'">
            {{ stats.guardiasChange >= 0 ? '+' : '' }}{{ stats.guardiasChange }}%
          </span>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon">🏫</div>
        <div class="stat-content">
          <h3>{{ stats.profesoresActivos }}</h3>
          <p>Profesores Activos</p>
          <span class="stat-change positive">
            {{ stats.profesoresNuevos }} nuevos
          </span>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon">⏱️</div>
        <div class="stat-content">
          <h3>{{ stats.horasCobertura }}</h3>
          <p>Horas de Cobertura</p>
          <span class="stat-change" :class="stats.coberturaChange >= 0 ? 'positive' : 'negative'">
            {{ stats.coberturaChange >= 0 ? '+' : '' }}{{ stats.coberturaChange }}%
          </span>
        </div>
      </div>
    </div>

    <!-- Gráficos -->
    <div class="charts-section">
      <div class="chart-container">
        <h2>📈 Ausencias por Mes</h2>
        <div class="chart-placeholder">
          <div class="bar-chart">
            <div v-for="(month, index) in chartData.months" :key="index" class="bar-item">
              <div class="bar" :style="{ height: `${(month.value / chartData.maxValue) * 100}%` }"></div>
              <span class="bar-label">{{ month.name }}</span>
              <span class="bar-value">{{ month.value }}</span>
            </div>
          </div>
        </div>
      </div>

      <div class="chart-container">
        <h2>🥧 Tipos de Ausencia</h2>
        <div class="chart-placeholder">
          <div class="pie-chart">
            <div class="pie-legend">
              <div v-for="(tipo, index) in chartData.tiposAusencia" :key="index" class="legend-item">
                <span class="legend-color" :style="{ backgroundColor: tipo.color }"></span>
                <span class="legend-label">{{ tipo.name }}</span>
                <span class="legend-value">{{ tipo.value }}%</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Tabla de profesores más activos -->
    <div class="top-profesores-section">
      <h2>🏆 Profesores con Más Guardias</h2>
      <div class="table-container">
        <table class="profesores-table">
          <thead>
            <tr>
              <th>Posición</th>
              <th>Profesor</th>
              <th>Guardias</th>
              <th>Horas</th>
              <th>Porcentaje</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(profesor, index) in topProfesores" :key="index" :class="{ 'highlight': index < 3 }">
              <td>
                <span class="position" :class="`position-${index + 1}`">
                  {{ index + 1 }}
                  <span v-if="index === 0">🥇</span>
                  <span v-else-if="index === 1">🥈</span>
                  <span v-else-if="index === 2">🥉</span>
                </span>
              </td>
              <td class="profesor-name">{{ profesor.nombre }}</td>
              <td class="guardias-count">{{ profesor.guardias }}</td>
              <td class="horas-count">{{ profesor.horas }}h</td>
              <td>
                <div class="progress-bar">
                  <div class="progress-fill" :style="{ width: `${profesor.porcentaje}%` }"></div>
                  <span class="progress-text">{{ profesor.porcentaje }}%</span>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Alertas y notificaciones -->
    <div class="alerts-section" v-if="alerts.length > 0">
      <h2>⚠️ Alertas del Sistema</h2>
      <div class="alerts-container">
        <div v-for="(alert, index) in alerts" :key="index" class="alert-item" :class="alert.type">
          <div class="alert-icon">{{ alert.icon }}</div>
          <div class="alert-content">
            <h4>{{ alert.title }}</h4>
            <p>{{ alert.message }}</p>
          </div>
          <button @click="dismissAlert(index)" class="alert-dismiss">×</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useToast } from 'vue-toastification';
import { supabase } from '@/supabaseClient';

const toast = useToast();

// Estados reactivos
const loading = ref(false);
const selectedPeriod = ref('month');

// Datos de estadísticas
const stats = ref({
  totalAusencias: 156,
  ausenciasChange: 12,
  totalGuardias: 89,
  guardiasChange: -5,
  profesoresActivos: 34,
  profesoresNuevos: 2,
  horasCobertura: 267,
  coberturaChange: 8
});

// Datos para gráficos
const chartData = ref({
  months: [
    { name: 'Ene', value: 23 },
    { name: 'Feb', value: 34 },
    { name: 'Mar', value: 28 },
    { name: 'Abr', value: 45 },
    { name: 'May', value: 38 },
    { name: 'Jun', value: 52 }
  ],
  maxValue: 52,
  tiposAusencia: [
    { name: 'Enfermedad', value: 35, color: '#FF6B6B' },
    { name: 'Personal', value: 25, color: '#4ECDC4' },
    { name: 'Formación', value: 20, color: '#45B7D1' },
    { name: 'Otros', value: 20, color: '#96CEB4' }
  ]
});

// Top profesores
const topProfesores = ref([
  { nombre: 'María García López', guardias: 45, horas: 67, porcentaje: 100 },
  { nombre: 'Juan Martín Pérez', guardias: 38, horas: 57, porcentaje: 84 },
  { nombre: 'Ana Rodríguez Silva', guardias: 34, horas: 51, porcentaje: 76 },
  { nombre: 'Carlos Fernández', guardias: 29, horas: 43, porcentaje: 64 },
  { nombre: 'Laura Sánchez', guardias: 25, horas: 38, porcentaje: 56 },
  { nombre: 'Miguel Torres', guardias: 22, horas: 33, porcentaje: 49 },
  { nombre: 'Elena Ruiz', guardias: 18, horas: 27, porcentaje: 40 }
]);

// Alertas del sistema
const alerts = ref([
  {
    type: 'warning',
    icon: '⚠️',
    title: 'Alto número de ausencias',
    message: 'Se ha detectado un incremento del 15% en las ausencias esta semana.'
  },
  {
    type: 'info',
    icon: 'ℹ️',
    title: 'Actualización disponible',
    message: 'Hay una nueva versión del sistema disponible para instalar.'
  }
]);

// Métodos
const loadData = async () => {
  loading.value = true;
  try {
    // Aquí podrías cargar datos reales desde la API
    // Por ahora usamos datos de ejemplo
    await new Promise(resolve => setTimeout(resolve, 1000)); // Simular carga
    
    toast.success(`Datos actualizados para ${selectedPeriod.value}`);
  } catch (error) {
    toast.error('Error al cargar los datos');
    console.error('Error:', error);
  } finally {
    loading.value = false;
  }
};

const refreshData = () => {
  loadData();
};

const dismissAlert = (index) => {
  alerts.value.splice(index, 1);
};

// Inicialización
onMounted(() => {
  loadData();
});
</script>

<style scoped>
.estadisticas-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 20px;
  font-family: 'Montserrat', sans-serif;
}

.estadisticas-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  flex-wrap: wrap;
  gap: 20px;
}

.estadisticas-header h1 {
  color: #1F86A1;
  font-size: 2.5rem;
  margin: 0;
  font-weight: 600;
}

.header-actions {
  display: flex;
  gap: 15px;
  align-items: center;
}

.period-selector {
  padding: 10px 15px;
  border: 2px solid #dee2e6;
  border-radius: 8px;
  font-size: 14px;
  background: white;
  cursor: pointer;
}

.refresh-btn {
  padding: 10px 20px;
  background: linear-gradient(90deg, #1F86A1, #4DB8D0);
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  gap: 8px;
}

.refresh-btn:hover:not(:disabled) {
  background: linear-gradient(90deg, #197a90, #3fa7be);
}

.refresh-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.stats-summary {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-bottom: 40px;
}

.stat-card {
  background: white;
  padding: 25px;
  border-radius: 12px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  gap: 20px;
  transition: transform 0.2s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
}

.stat-icon {
  font-size: 3rem;
  padding: 15px;
  background: linear-gradient(135deg, #1F86A1, #4DB8D0);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-content h3 {
  font-size: 2rem;
  font-weight: 700;
  color: #333;
  margin: 0 0 5px 0;
}

.stat-content p {
  color: #666;
  margin: 0 0 10px 0;
  font-weight: 500;
}

.stat-change {
  font-size: 0.9rem;
  font-weight: 600;
  padding: 4px 8px;
  border-radius: 4px;
}

.stat-change.positive {
  background: #d4edda;
  color: #155724;
}

.stat-change.negative {
  background: #f8d7da;
  color: #721c24;
}

.charts-section {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
  gap: 30px;
  margin-bottom: 40px;
}

.chart-container {
  background: white;
  padding: 25px;
  border-radius: 12px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.chart-container h2 {
  color: #333;
  margin-bottom: 20px;
  font-size: 1.3rem;
}

.bar-chart {
  display: flex;
  align-items: end;
  gap: 10px;
  height: 200px;
  padding: 20px 0;
}

.bar-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.bar {
  width: 100%;
  background: linear-gradient(to top, #1F86A1, #4DB8D0);
  border-radius: 4px 4px 0 0;
  transition: all 0.3s ease;
  min-height: 10px;
}

.bar:hover {
  filter: brightness(1.1);
}

.bar-label {
  font-size: 12px;
  font-weight: 500;
  color: #666;
}

.bar-value {
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

.pie-legend {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.legend-color {
  width: 20px;
  height: 20px;
  border-radius: 4px;
}

.legend-label {
  flex: 1;
  font-weight: 500;
  color: #333;
}

.legend-value {
  font-weight: 600;
  color: #1F86A1;
}

.top-profesores-section {
  background: white;
  padding: 25px;
  border-radius: 12px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  margin-bottom: 40px;
}

.top-profesores-section h2 {
  color: #333;
  margin-bottom: 20px;
  font-size: 1.3rem;
}

.table-container {
  overflow-x: auto;
}

.profesores-table {
  width: 100%;
  border-collapse: collapse;
}

.profesores-table th {
  background: #f8f9fa;
  padding: 15px;
  text-align: left;
  font-weight: 600;
  color: #333;
  border-bottom: 2px solid #dee2e6;
}

.profesores-table td {
  padding: 15px;
  border-bottom: 1px solid #dee2e6;
}

.profesores-table tr.highlight {
  background: #f8fffe;
}

.position {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
}

.position-1 { color: #FFD700; }
.position-2 { color: #C0C0C0; }
.position-3 { color: #CD7F32; }

.profesor-name {
  font-weight: 500;
  color: #333;
}

.guardias-count, .horas-count {
  font-weight: 600;
  color: #1F86A1;
}

.progress-bar {
  position: relative;
  background: #e9ecef;
  border-radius: 10px;
  height: 20px;
  min-width: 100px;
}

.progress-fill {
  background: linear-gradient(90deg, #1F86A1, #4DB8D0);
  height: 100%;
  border-radius: 10px;
  transition: width 0.3s ease;
}

.progress-text {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  font-size: 12px;
  font-weight: 600;
  color: white;
  text-shadow: 0 1px 2px rgba(0,0,0,0.3);
}

.alerts-section {
  background: white;
  padding: 25px;
  border-radius: 12px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.alerts-section h2 {
  color: #333;
  margin-bottom: 20px;
  font-size: 1.3rem;
}

.alerts-container {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.alert-item {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 15px;
  border-radius: 8px;
  border-left: 4px solid;
}

.alert-item.warning {
  background: #fff3cd;
  border-left-color: #ffc107;
}

.alert-item.info {
  background: #d1ecf1;
  border-left-color: #17a2b8;
}

.alert-icon {
  font-size: 1.5rem;
}

.alert-content {
  flex: 1;
}

.alert-content h4 {
  margin: 0 0 5px 0;
  color: #333;
}

.alert-content p {
  margin: 0;
  color: #666;
}

.alert-dismiss {
  background: none;
  border: none;
  font-size: 1.5rem;
  cursor: pointer;
  color: #999;
  padding: 5px;
}

.alert-dismiss:hover {
  color: #666;
}

/* Responsive */
@media (max-width: 768px) {
  .estadisticas-container {
    padding: 15px;
  }

  .estadisticas-header {
    flex-direction: column;
    align-items: stretch;
  }

  .estadisticas-header h1 {
    font-size: 2rem;
    text-align: center;
  }

  .header-actions {
    justify-content: center;
  }

  .stats-summary {
    grid-template-columns: 1fr;
  }

  .charts-section {
    grid-template-columns: 1fr;
  }

  .profesores-table {
    font-size: 14px;
  }

  .profesores-table th,
  .profesores-table td {
    padding: 10px;
  }
}
</style>
