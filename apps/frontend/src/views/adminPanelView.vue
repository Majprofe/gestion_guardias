<template>
  <div class="admin-panel-container">
    <h1>🛠️ Panel de Administración Avanzado</h1>
    
    <!-- Tabs de navegación -->
    <div class="tabs-container">
      <button 
        v-for="tab in tabs" 
        :key="tab.id"
        :class="['tab-button', { active: activeTab === tab.id }]"
        @click="activeTab = tab.id"
      >
        {{ tab.icon }} {{ tab.name }}
      </button>
    </div>

    <!-- Gestión de Coberturas -->
    <div v-if="activeTab === 'coberturas'" class="tab-content">
      <div class="section-header">
        <h2>📋 Gestión de Coberturas</h2>
        <div class="filtros-avanzados">
          <input 
            v-model="filtros.fecha" 
            type="date" 
            placeholder="Filtrar por fecha"
            @change="cargarCoberturas"
          />
          <select v-model="filtros.estado" @change="cargarCoberturas">
            <option value="">Todos los estados</option>
            <option value="ASIGNADA">Asignada</option>
            <option value="VALIDADA">Validada</option>
            <option value="COMPLETADA">Completada</option>
          </select>
          <button @click="redistribuirDia" class="btn btn-warning" :disabled="!filtros.fecha">
            🔄 Redistribuir Día
          </button>
        </div>
      </div>

      <div class="coberturas-grid">
        <div v-for="cobertura in coberturasFiltered" :key="cobertura.id" class="cobertura-card">
          <div class="cobertura-header">
            <span class="cobertura-fecha">{{ formatearFecha(cobertura.fecha) }}</span>
            <span :class="'estado-badge ' + cobertura.estado.toLowerCase()">
              {{ cobertura.estado }}
            </span>
          </div>
          
          <div class="cobertura-body">
            <div class="cobertura-info">
              <p><strong>📍 Aula:</strong> {{ cobertura.aula }}</p>
              <p><strong>👥 Grupo:</strong> {{ cobertura.grupo }}</p>
              <p><strong>🕒 Hora:</strong> {{ cobertura.hora }}ª</p>
              <p><strong>👨‍🏫 Profesor:</strong> {{ emailAcortado(cobertura.profesorEmail) }}</p>
              <p><strong>📝 Tarea:</strong> {{ cobertura.tarea || 'Sin tarea específica' }}</p>
            </div>
            
            <div class="cobertura-actions">
              <button 
                @click="cambiarProfesorCobertura(cobertura)" 
                class="btn btn-sm btn-primary"
                :disabled="cobertura.estado === 'COMPLETADA'"
              >
                👥 Cambiar Profesor
              </button>
              
              <button 
                @click="validarCobertura(cobertura)" 
                class="btn btn-sm btn-success"
                v-if="cobertura.estado === 'ASIGNADA'"
              >
                ✅ Validar
              </button>
              
              <button 
                @click="eliminarCobertura(cobertura)" 
                class="btn btn-sm btn-danger"
                :disabled="cobertura.estado === 'COMPLETADA'"
              >
                🗑️ Eliminar
              </button>
            </div>
          </div>
          
          <!-- Archivos adjuntos si los hay -->
          <div v-if="cobertura.archivos && cobertura.archivos.length" class="cobertura-archivos">
            <h5>📎 Archivos adjuntos:</h5>
            <div class="archivos-lista-mini">
              <a 
                v-for="archivo in cobertura.archivos" 
                :key="archivo.id"
                @click="descargarArchivo(archivo.id)"
                class="archivo-link"
              >
                {{ getFileIcon(archivo.nombreOriginal) }} {{ archivo.nombreOriginal }}
              </a>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Asignación Manual -->
    <div v-if="activeTab === 'asignacion'" class="tab-content">
      <div class="section-header">
        <h2>🎯 Asignación Manual de Guardias</h2>
      </div>
      
      <div class="asignacion-manual">
        <div class="asignacion-form">
          <div class="form-row">
            <div class="form-group">
              <label>📅 Fecha:</label>
              <input v-model="asignacionManual.fecha" type="date" required />
            </div>
            
            <div class="form-group">
              <label>🕒 Hora:</label>
              <select v-model="asignacionManual.hora" required>
                <option value="">Seleccionar hora</option>
                <option v-for="hora in 6" :key="hora" :value="hora">{{ hora }}ª Hora</option>
              </select>
            </div>
          </div>
          
          <div class="form-row">
            <div class="form-group">
              <label>👨‍🏫 Profesor Ausente:</label>
              <select v-model="asignacionManual.profesorAusenteEmail">
                <option value="">Seleccionar profesor</option>
                <option v-for="profesor in profesoresList" :key="profesor.email" :value="profesor.email">
                  {{ profesor.nombre }} ({{ profesor.email }})
                </option>
              </select>
            </div>
            
            <div class="form-group">
              <label>👥 Grupo:</label>
              <input v-model="asignacionManual.grupo" placeholder="Ej: 1ºDAM" required />
            </div>
          </div>
          
          <div class="form-row">
            <div class="form-group">
              <label>📍 Aula:</label>
              <input v-model="asignacionManual.aula" placeholder="Ej: A01" required />
            </div>
            
            <div class="form-group">
              <label>⚠️ ¿Grupo problemático?</label>
              <label class="checkbox-label">
                <input type="checkbox" v-model="asignacionManual.esProblematico" />
                <span class="checkmark"></span>
                Sí
              </label>
            </div>
          </div>
          
          <div class="form-group">
            <label>📝 Tarea:</label>
            <textarea v-model="asignacionManual.tarea" placeholder="Descripción de la tarea..."></textarea>
          </div>
          
          <button @click="crearAsignacionManual" class="btn btn-primary" :disabled="!validarAsignacionManual()">
            ➕ Crear Asignación Manual
          </button>
        </div>
        
        <!-- Profesores disponibles para esa hora -->
        <div class="profesores-disponibles" v-if="profesoresDisponibles.length">
          <h3>👥 Profesores Disponibles</h3>
          <p class="disponibles-hint">
            Profesores de guardia en {{ asignacionManual.fecha }} a las {{ asignacionManual.hora }}ª hora:
          </p>
          
          <div class="profesores-grid">
            <div 
              v-for="profesor in profesoresDisponibles" 
              :key="profesor.email"
              :class="['profesor-card', { selected: asignacionManual.profesorGuardiaEmail === profesor.email }]"
              @click="asignacionManual.profesorGuardiaEmail = profesor.email"
            >
              <div class="profesor-info">
                <h4>{{ emailAcortado(profesor.email) }}</h4>
                <p>{{ profesor.nombre }}</p>
              </div>
              
              <div class="profesor-stats">
                <div class="stat">
                  <span class="stat-label">Normal:</span>
                  <span class="stat-value">{{ profesor.guardiasNormales || 0 }}</span>
                </div>
                <div class="stat">
                  <span class="stat-label">Problemáticas:</span>
                  <span class="stat-value">{{ profesor.guardiasProblematicas || 0 }}</span>
                </div>
                <div class="stat">
                  <span class="stat-label">Convivencia:</span>
                  <span class="stat-value">{{ profesor.guardiasConvivencia || 0 }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Alertas y Monitores -->
    <div v-if="activeTab === 'alertas'" class="tab-content">
      <div class="section-header">
        <h2>🚨 Sistema de Alertas y Monitoreo</h2>
        <button @click="verificarCobertura" class="btn btn-info">
          🔍 Verificar Cobertura Actual
        </button>
      </div>
      
      <div class="alertas-grid">
        <!-- Alerta de insuficiencia -->
        <div class="alerta-card error" v-if="alertaInsuficiencia.length">
          <div class="alerta-header">
            <h3>❌ Insuficiencia de Profesores</h3>
          </div>
          <div class="alerta-body">
            <p>Los siguientes tramos horarios no tienen suficientes profesores de guardia:</p>
            <ul>
              <li v-for="tramo in alertaInsuficiencia" :key="tramo.id">
                <strong>{{ tramo.dia }} - {{ tramo.hora }}ª hora:</strong> 
                {{ tramo.profesoresDisponibles }} disponibles, {{ tramo.ausenciasTotal }} ausencias
              </li>
            </ul>
          </div>
        </div>
        
        <!-- Alerta de desbalance extremo -->
        <div class="alerta-card warning" v-if="alertaDesbalance.length">
          <div class="alerta-header">
            <h3>⚖️ Desbalance Extremo</h3>
          </div>
          <div class="alerta-body">
            <p>Profesores con carga muy desigual de guardias:</p>
            <ul>
              <li v-for="profesor in alertaDesbalance" :key="profesor.email">
                <strong>{{ emailAcortado(profesor.email) }}:</strong> 
                {{ profesor.totalGuardias }} guardias (promedio: {{ promedioGuardias.toFixed(1) }})
              </li>
            </ul>
          </div>
        </div>
        
        <!-- Resumen de estado -->
        <div class="alerta-card success">
          <div class="alerta-header">
            <h3>📊 Resumen del Sistema</h3>
          </div>
          <div class="alerta-body">
            <div class="stats-grid">
              <div class="stat-item">
                <span class="stat-number">{{ resumenSistema.totalCoberturas }}</span>
                <span class="stat-label">Coberturas Activas</span>
              </div>
              <div class="stat-item">
                <span class="stat-number">{{ resumenSistema.coberturasValidadas }}</span>
                <span class="stat-label">Validadas</span>
              </div>
              <div class="stat-item">
                <span class="stat-number">{{ resumenSistema.profesoresActivos }}</span>
                <span class="stat-label">Profesores Activos</span>
              </div>
              <div class="stat-item">
                <span class="stat-number">{{ resumenSistema.horasSinCobertura }}</span>
                <span class="stat-label">Horas Sin Cobertura</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Configuración -->
    <div v-if="activeTab === 'configuracion'" class="tab-content">
      <div class="section-header">
        <h2>⚙️ Configuración del Sistema</h2>
      </div>
      
      <div class="configuracion-grid">
        <div class="config-card">
          <h3>🔄 Redistribución Automática</h3>
          <div class="config-options">
            <label class="checkbox-label">
              <input type="checkbox" v-model="configuracion.redistribucionAutomatica" />
              <span class="checkmark"></span>
              Habilitar redistribución automática al crear ausencias
            </label>
            
            <label class="checkbox-label">
              <input type="checkbox" v-model="configuracion.notificacionesEmail" />
              <span class="checkmark"></span>
              Enviar notificaciones por email a profesores asignados
            </label>
          </div>
        </div>
        
        <div class="config-card">
          <h3>📊 Métricas y Reportes</h3>
          <div class="config-options">
            <button @click="exportarDatos('csv')" class="btn btn-outline">
              📄 Exportar a CSV
            </button>
            <button @click="exportarDatos('excel')" class="btn btn-outline">
              📊 Exportar a Excel
            </button>
            <button @click="limpiarDatosAntiguos" class="btn btn-warning">
              🧹 Limpiar Datos Antiguos
            </button>
          </div>
        </div>
        
        <div class="config-card">
          <h3>🔧 Herramientas de Mantenimiento</h3>
          <div class="config-options">
            <button @click="recalcularContadores" class="btn btn-info">
              🔢 Recalcular Todos los Contadores
            </button>
            <button @click="verificarIntegridad" class="btn btn-secondary">
              🔍 Verificar Integridad de Datos
            </button>
            <button @click="reiniciarSistema" class="btn btn-danger">
              🔄 Reiniciar Sistema
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>

  <!-- Modal para cambio de profesor -->
  <div v-if="modalCambioProfesor.show" class="modal-overlay" @click="cerrarModalCambio">
    <div class="modal-content" @click.stop>
      <div class="modal-header">
        <h3>👥 Cambiar Profesor de Cobertura</h3>
        <button @click="cerrarModalCambio" class="btn-close">❌</button>
      </div>
      
      <div class="modal-body">
        <p>Cobertura actual: <strong>{{ modalCambioProfesor.cobertura?.grupo }}</strong> - {{ modalCambioProfesor.cobertura?.hora }}ª hora</p>
        <p>Profesor actual: <strong>{{ emailAcortado(modalCambioProfesor.cobertura?.profesorEmail) }}</strong></p>
        
        <div class="nuevo-profesor-selection">
          <label>Seleccionar nuevo profesor:</label>
          <select v-model="modalCambioProfesor.nuevoProfesorEmail">
            <option value="">Seleccionar...</option>
            <option 
              v-for="profesor in profesoresDisponiblesModal" 
              :key="profesor.email" 
              :value="profesor.email"
            >
              {{ profesor.nombre }} - Guardias: {{ profesor.totalGuardias || 0 }}
            </option>
          </select>
        </div>
        
        <div class="razon-cambio">
          <label>Razón del cambio (opcional):</label>
          <textarea v-model="modalCambioProfesor.razon" placeholder="Ej: Solicitud del profesor, mejor capacitación..."></textarea>
        </div>
      </div>
      
      <div class="modal-footer">
        <button @click="confirmarCambioProfesor" class="btn btn-primary" :disabled="!modalCambioProfesor.nuevoProfesorEmail">
          ✅ Confirmar Cambio
        </button>
        <button @click="cerrarModalCambio" class="btn btn-secondary">
          ❌ Cancelar
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { useToast } from 'vue-toastification'
import { 
  getCoberturas,
  deleteCoberturaByAusencia,
  asignarCobertura,
  getProfesoresByNombre,
  getGuardiaByDayAndHour,
  getArchivosByAusencia,
  downloadArchivo,
  actualizarEstadoCobertura,
  redistribuirCoberturasDia
} from '@/services/apiService'

const toast = useToast()

// Tabs
const tabs = [
  { id: 'coberturas', name: 'Gestión de Coberturas', icon: '📋' },
  { id: 'asignacion', name: 'Asignación Manual', icon: '🎯' },
  { id: 'alertas', name: 'Alertas y Monitoreo', icon: '🚨' },
  { id: 'configuracion', name: 'Configuración', icon: '⚙️' }
]

const activeTab = ref('coberturas')

// Estados para coberturas
const coberturas = ref([])
const filtros = ref({
  fecha: '',
  estado: '',
  profesor: ''
})

// Estados para asignación manual
const asignacionManual = ref({
  fecha: '',
  hora: '',
  profesorAusenteEmail: '',
  profesorGuardiaEmail: '',
  grupo: '',
  aula: '',
  tarea: '',
  esProblematico: false
})

const profesoresList = ref([])
const profesoresDisponibles = ref([])

// Estados para alertas
const alertaInsuficiencia = ref([])
const alertaDesbalance = ref([])
const resumenSistema = ref({
  totalCoberturas: 0,
  coberturasValidadas: 0,
  profesoresActivos: 0,
  horasSinCobertura: 0
})

// Estados para configuración
const configuracion = ref({
  redistribucionAutomatica: true,
  notificacionesEmail: false
})

// Modal de cambio de profesor
const modalCambioProfesor = ref({
  show: false,
  cobertura: null,
  nuevoProfesorEmail: '',
  razon: ''
})

const profesoresDisponiblesModal = ref([])

// Computeds
const coberturasFiltered = computed(() => {
  let filtered = coberturas.value

  if (filtros.value.fecha) {
    filtered = filtered.filter(c => c.fecha === filtros.value.fecha)
  }
  
  if (filtros.value.estado) {
    filtered = filtered.filter(c => c.estado === filtros.value.estado)
  }
  
  return filtered
})

const promedioGuardias = computed(() => {
  if (!profesoresList.value.length) return 0
  const total = profesoresList.value.reduce((sum, p) => sum + (p.totalGuardias || 0), 0)
  return total / profesoresList.value.length
})

// Watchers
watch([() => asignacionManual.value.fecha, () => asignacionManual.value.hora], async () => {
  if (asignacionManual.value.fecha && asignacionManual.value.hora) {
    await cargarProfesoresDisponibles()
  }
})

// Lifecycle
onMounted(async () => {
  await Promise.all([
    cargarCoberturas(),
    cargarProfesores(),
    verificarCobertura()
  ])
})

// Métodos principales
const cargarCoberturas = async () => {
  try {
    const response = await getCoberturas(filtros.value)
    coberturas.value = response.data || []
    
    // Cargar archivos para cada cobertura
    for (const cobertura of coberturas.value) {
      try {
        const archivosResp = await getArchivosByAusencia(cobertura.ausenciaId)
        cobertura.archivos = archivosResp.data || []
      } catch (error) {
        cobertura.archivos = []
      }
    }
  } catch (error) {
    console.error('Error cargando coberturas:', error)
    toast.error('Error al cargar coberturas')
  }
}

const cargarProfesores = async () => {
  try {
    const response = await getProfesoresByNombre('')
    profesoresList.value = response.data || []
  } catch (error) {
    console.error('Error cargando profesores:', error)
    toast.error('Error al cargar lista de profesores')
  }
}

const cargarProfesoresDisponibles = async () => {
  if (!asignacionManual.value.fecha || !asignacionManual.value.hora) return
  
  try {
    const fecha = new Date(asignacionManual.value.fecha)
    const diaSemana = fecha.getDay() === 0 ? 7 : fecha.getDay()
    
    const response = await getGuardiaByDayAndHour(diaSemana, asignacionManual.value.hora)
    profesoresDisponibles.value = response.data.profesores || []
  } catch (error) {
    console.error('Error cargando profesores disponibles:', error)
    profesoresDisponibles.value = []
  }
}

// Métodos de coberturas
const cambiarProfesorCobertura = async (cobertura) => {
  modalCambioProfesor.value.show = true
  modalCambioProfesor.value.cobertura = cobertura
  modalCambioProfesor.value.nuevoProfesorEmail = ''
  modalCambioProfesor.value.razon = ''
  
  // Cargar profesores disponibles para esa fecha/hora
  try {
    const fecha = new Date(cobertura.fecha)
    const diaSemana = fecha.getDay() === 0 ? 7 : fecha.getDay()
    
    const response = await getGuardiaByDayAndHour(diaSemana, cobertura.hora)
    profesoresDisponiblesModal.value = response.data.profesores || []
  } catch (error) {
    profesoresDisponiblesModal.value = profesoresList.value
  }
}

const confirmarCambioProfesor = async () => {
  try {
    // Eliminar cobertura actual
    await deleteCoberturaByAusencia(modalCambioProfesor.value.cobertura.ausenciaId)
    
    // Crear nueva cobertura
    await asignarCobertura({
      ausenciaId: modalCambioProfesor.value.cobertura.ausenciaId,
      profesorEmail: modalCambioProfesor.value.nuevoProfesorEmail,
      observaciones: modalCambioProfesor.value.razon
    })
    
    toast.success('Profesor de cobertura cambiado exitosamente')
    await cargarCoberturas()
    cerrarModalCambio()
  } catch (error) {
    console.error('Error cambiando profesor:', error)
    toast.error('Error al cambiar profesor de cobertura')
  }
}

const cerrarModalCambio = () => {
  modalCambioProfesor.value.show = false
  modalCambioProfesor.value.cobertura = null
  modalCambioProfesor.value.nuevoProfesorEmail = ''
  modalCambioProfesor.value.razon = ''
}

const validarCobertura = async (cobertura) => {
  try {
    await actualizarEstadoCobertura(cobertura.id, 'VALIDADA')
    toast.success('Cobertura validada exitosamente')
    await cargarCoberturas()
  } catch (error) {
    console.error('Error validando cobertura:', error)
    toast.error('Error al validar cobertura')
  }
}

const eliminarCobertura = async (cobertura) => {
  if (!confirm('¿Estás seguro de eliminar esta cobertura?')) return
  
  try {
    await deleteCoberturaByAusencia(cobertura.ausenciaId)
    toast.success('Cobertura eliminada exitosamente')
    await cargarCoberturas()
  } catch (error) {
    console.error('Error eliminando cobertura:', error)
    toast.error('Error al eliminar cobertura')
  }
}

const redistribuirDia = async () => {
  if (!filtros.value.fecha) {
    toast.error('Selecciona una fecha para redistribuir')
    return
  }
  
  try {
    await redistribuirCoberturasDia(filtros.value.fecha)
    toast.success('Día redistribuido exitosamente')
    await cargarCoberturas()
  } catch (error) {
    console.error('Error redistribuyendo día:', error)
    toast.error('Error al redistribuir el día')
  }
}

// Métodos de asignación manual
const validarAsignacionManual = () => {
  return asignacionManual.value.fecha && 
         asignacionManual.value.hora && 
         asignacionManual.value.profesorAusenteEmail &&
         asignacionManual.value.grupo && 
         asignacionManual.value.aula
}

const crearAsignacionManual = async () => {
  if (!validarAsignacionManual()) {
    toast.error('Complete todos los campos obligatorios')
    return
  }
  
  try {
    // Aquí iría la lógica para crear la asignación manual
    // Esto requeriría endpoints específicos en el backend
    
    toast.success('Asignación manual creada exitosamente')
    
    // Limpiar formulario
    asignacionManual.value = {
      fecha: '',
      hora: '',
      profesorAusenteEmail: '',
      profesorGuardiaEmail: '',
      grupo: '',
      aula: '',
      tarea: '',
      esProblematico: false
    }
    
    await cargarCoberturas()
  } catch (error) {
    console.error('Error creando asignación manual:', error)
    toast.error('Error al crear asignación manual')
  }
}

// Métodos de alertas
const verificarCobertura = async () => {
  try {
    // Simulación de verificación de cobertura
    // En producción esto vendría del backend
    
    alertaInsuficiencia.value = [
      {
        id: 1,
        dia: 'Lunes',
        hora: 3,
        profesoresDisponibles: 2,
        ausenciasTotal: 4
      }
    ]
    
    alertaDesbalance.value = profesoresList.value.filter(p => {
      const diferencia = Math.abs((p.totalGuardias || 0) - promedioGuardias.value)
      return diferencia > 3
    })
    
    resumenSistema.value = {
      totalCoberturas: coberturas.value.length,
      coberturasValidadas: coberturas.value.filter(c => c.estado === 'VALIDADA').length,
      profesoresActivos: profesoresList.value.length,
      horasSinCobertura: Math.floor(Math.random() * 5) // Simulado
    }
    
    toast.info('Verificación de cobertura completada')
  } catch (error) {
    console.error('Error verificando cobertura:', error)
    toast.error('Error al verificar cobertura')
  }
}

// Métodos de configuración
const exportarDatos = (formato) => {
  toast.info(`Exportando datos en formato ${formato.toUpperCase()}...`)
  // Aquí iría la lógica de exportación
}

const limpiarDatosAntiguos = () => {
  if (!confirm('¿Eliminar datos de más de 6 meses? Esta acción no se puede deshacer.')) return
  
  toast.info('Limpiando datos antiguos...')
  // Aquí iría la lógica de limpieza
}

const recalcularContadores = () => {
  if (!confirm('¿Recalcular todos los contadores? Esto puede tomar varios minutos.')) return
  
  toast.info('Recalculando contadores...')
  // Aquí iría la lógica de recálculo
}

const verificarIntegridad = () => {
  toast.info('Verificando integridad de datos...')
  // Aquí iría la lógica de verificación
}

const reiniciarSistema = () => {
  if (!confirm('¿REINICIAR COMPLETAMENTE EL SISTEMA? Esto eliminará TODOS los datos.')) return
  if (!confirm('¿Estás COMPLETAMENTE SEGURO? Esta acción NO se puede deshacer.')) return
  
  toast.error('Función de reinicio deshabilitada por seguridad')
  // En producción esto estaría muy protegido
}

// Métodos auxiliares
const emailAcortado = (email) => {
  if (!email) return 'N/A'
  const index = email.indexOf('@')
  return index > 0 ? email.substring(0, index) : email.substring(0, 10)
}

const formatearFecha = (fecha) => {
  return new Intl.DateTimeFormat('es-ES', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric'
  }).format(new Date(fecha))
}

const getFileIcon = (fileName) => {
  const ext = fileName.split('.').pop().toLowerCase()
  const icons = {
    'pdf': '📄',
    'doc': '📝',
    'docx': '📝',
    'txt': '📃',
    'jpg': '🖼️',
    'jpeg': '🖼️',
    'png': '🖼️'
  }
  return icons[ext] || '📎'
}

const descargarArchivo = async (archivoId) => {
  try {
    const response = await downloadArchivo(archivoId)
    
    // Crear enlace de descarga
    const url = window.URL.createObjectURL(new Blob([response.data]))
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', 'archivo')
    document.body.appendChild(link)
    link.click()
    link.remove()
    window.URL.revokeObjectURL(url)
    
    toast.success('Archivo descargado exitosamente')
  } catch (error) {
    console.error('Error descargando archivo:', error)
    toast.error('Error al descargar archivo')
  }
}
</script>

<style scoped>
.admin-panel-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 20px;
  font-family: 'Montserrat', sans-serif;
}

.tabs-container {
  display: flex;
  gap: 10px;
  margin-bottom: 30px;
  border-bottom: 2px solid #f0f0f0;
  overflow-x: auto;
}

.tab-button {
  padding: 12px 20px;
  border: none;
  background: transparent;
  color: #666;
  font-weight: 600;
  cursor: pointer;
  border-bottom: 3px solid transparent;
  transition: all 0.3s ease;
  white-space: nowrap;
}

.tab-button:hover {
  color: #1f86a1;
}

.tab-button.active {
  color: #1f86a1;
  border-bottom-color: #1f86a1;
}

.tab-content {
  min-height: 600px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 25px;
  flex-wrap: wrap;
  gap: 15px;
}

.section-header h2 {
  margin: 0;
  color: #333;
}

.filtros-avanzados {
  display: flex;
  gap: 15px;
  align-items: center;
  flex-wrap: wrap;
}

.filtros-avanzados input,
.filtros-avanzados select {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  background: white;
}

.btn {
  padding: 8px 16px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 600;
  text-decoration: none;
  display: inline-flex;
  align-items: center;
  gap: 5px;
  transition: all 0.3s ease;
}

.btn-primary {
  background: #1f86a1;
  color: white;
}

.btn-primary:hover {
  background: #176a82;
}

.btn-warning {
  background: #ffa502;
  color: white;
}

.btn-danger {
  background: #ff4757;
  color: white;
}

.btn-success {
  background: #2ed573;
  color: white;
}

.btn-info {
  background: #3742fa;
  color: white;
}

.btn-secondary {
  background: #8e8e93;
  color: white;
}

.btn-outline {
  background: transparent;
  color: #1f86a1;
  border: 1px solid #1f86a1;
}

.btn-sm {
  padding: 4px 8px;
  font-size: 12px;
}

.btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* Coberturas */
.coberturas-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
  gap: 20px;
}

.cobertura-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
  border-left: 4px solid #1f86a1;
}

.cobertura-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.cobertura-fecha {
  font-weight: 600;
  color: #333;
}

.estado-badge {
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
  text-transform: uppercase;
}

.estado-badge.asignada {
  background: #fff3cd;
  color: #856404;
}

.estado-badge.validada {
  background: #d1ecf1;
  color: #0c5460;
}

.estado-badge.completada {
  background: #d4edda;
  color: #155724;
}

.cobertura-info p {
  margin: 8px 0;
  color: #666;
}

.cobertura-actions {
  display: flex;
  gap: 10px;
  margin-top: 15px;
  flex-wrap: wrap;
}

.cobertura-archivos {
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px solid #eee;
}

.archivos-lista-mini {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.archivo-link {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 4px 8px;
  background: #f8f9fa;
  border-radius: 4px;
  color: #1f86a1;
  cursor: pointer;
  text-decoration: none;
  font-size: 12px;
}

.archivo-link:hover {
  background: #e9ecef;
}

/* Asignación Manual */
.asignacion-manual {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 30px;
}

.asignacion-form {
  background: white;
  padding: 25px;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 15px;
  margin-bottom: 15px;
}

.form-group {
  display: flex;
  flex-direction: column;
}

.form-group label {
  font-weight: 600;
  margin-bottom: 5px;
  color: #333;
}

.form-group input,
.form-group select,
.form-group textarea {
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
}

.form-group textarea {
  resize: vertical;
  height: 80px;
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.profesores-disponibles {
  background: white;
  padding: 25px;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
}

.disponibles-hint {
  color: #666;
  font-size: 14px;
  margin-bottom: 15px;
}

.profesores-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 15px;
}

.profesor-card {
  padding: 15px;
  border: 2px solid #eee;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.profesor-card:hover {
  border-color: #1f86a1;
}

.profesor-card.selected {
  border-color: #1f86a1;
  background: #f0f8ff;
}

.profesor-info h4 {
  margin: 0 0 5px 0;
  color: #333;
}

.profesor-info p {
  margin: 0;
  color: #666;
  font-size: 14px;
}

.profesor-stats {
  margin-top: 10px;
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.stat {
  display: flex;
  align-items: center;
  gap: 5px;
}

.stat-label {
  font-size: 12px;
  color: #666;
}

.stat-value {
  font-weight: 600;
  color: #1f86a1;
}

/* Alertas */
.alertas-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
  gap: 20px;
}

.alerta-card {
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
}

.alerta-card.error {
  background: rgba(255, 71, 87, 0.1);
  border-left: 4px solid #ff4757;
}

.alerta-card.warning {
  background: rgba(255, 165, 2, 0.1);
  border-left: 4px solid #ffa502;
}

.alerta-card.success {
  background: rgba(46, 213, 115, 0.1);
  border-left: 4px solid #2ed573;
}

.alerta-header h3 {
  margin: 0 0 15px 0;
  color: #333;
}

.alerta-body ul {
  margin: 10px 0;
  padding-left: 20px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
  gap: 15px;
}

.stat-item {
  text-align: center;
}

.stat-number {
  display: block;
  font-size: 2em;
  font-weight: 600;
  color: #1f86a1;
}

/* Configuración */
.configuracion-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(350px, 1fr));
  gap: 20px;
}

.config-card {
  background: white;
  padding: 25px;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
}

.config-card h3 {
  margin-top: 0;
  color: #333;
  border-bottom: 1px solid #eee;
  padding-bottom: 10px;
}

.config-options {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

/* Modal */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 12px;
  padding: 0;
  max-width: 500px;
  width: 90%;
  max-height: 80vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #eee;
}

.modal-header h3 {
  margin: 0;
  color: #333;
}

.btn-close {
  background: none;
  border: none;
  font-size: 1.2em;
  cursor: pointer;
}

.modal-body {
  padding: 20px;
}

.nuevo-profesor-selection,
.razon-cambio {
  margin: 15px 0;
}

.nuevo-profesor-selection label,
.razon-cambio label {
  display: block;
  font-weight: 600;
  margin-bottom: 5px;
  color: #333;
}

.nuevo-profesor-selection select,
.razon-cambio textarea {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 6px;
}

.modal-footer {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
  padding: 20px;
  border-top: 1px solid #eee;
}

/* Responsive */
@media (max-width: 768px) {
  .tabs-container {
    flex-wrap: wrap;
  }
  
  .section-header {
    flex-direction: column;
    align-items: stretch;
  }
  
  .filtros-avanzados {
    flex-direction: column;
  }
  
  .coberturas-grid {
    grid-template-columns: 1fr;
  }
  
  .asignacion-manual {
    grid-template-columns: 1fr;
  }
  
  .form-row {
    grid-template-columns: 1fr;
  }
  
  .profesores-grid {
    grid-template-columns: 1fr;
  }
  
  .alertas-grid {
    grid-template-columns: 1fr;
  }
  
  .configuracion-grid {
    grid-template-columns: 1fr;
  }
  
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
