<template>
  <div class="mis-guardias-container">

    <!-- Botones para alternar -->
    <div class="toggle-buttons">
      <button 
        :class="{ active: vista === 'ausencias' }"
        @click="vista = 'ausencias'"
      >
        Ausencias
      </button>
      <button 
        :class="{ active: vista === 'coberturas' }"
        @click="vista = 'coberturas'"
      >
        Coberturas
      </button>
    </div>

    <!-- Tabla de Ausencias -->
    <div v-if="vista === 'ausencias'">
      <h1>Mis Ausencias</h1>
      <div class="table-wrapper">
        <table v-if="misAusencias.length">
          <thead>
            <tr>
              <th>Fecha</th>
              <th>Hora</th>
              <th>Aula</th>
              <th>Grupo</th>
              <th>Tarea</th>
              <th>Prof. cubriendo</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="a in ausenciasOrdenadas" :key="a.id">
              <td data-label="Fecha" class="fecha">{{ formatearFecha(a.fecha) }}</td>
              <td data-label="Hora">{{ a.hora }}</td>
              <td data-label="Aula">{{ a.aula }}</td>
              <td data-label="Grupo">{{ a.grupo }}</td>
              <td data-label="Tarea">{{ a.tarea }}</td>
              <td data-label="Prof. cubriendo">
                {{ emailAcortado(a.profesorEnGuardiaEmail) }}
              </td>
            </tr>
          </tbody>
        </table>
        <p v-else>No tienes ausencias registradas.</p>
      </div>
    </div>

    <!-- Tabla de Coberturas -->
    <div v-else>
      <h1>Mis Coberturas</h1>
      <div class="table-wrapper">
        <table v-if="misCoberturas.length">
          <thead>
            <tr>
              <th>Fecha</th>
              <th>Hora</th>
              <th>Aula</th>
              <th>Grupo</th>
              <th>Tarea</th>
              <th>Profesor Ausente</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="c in coberturasOrdenadas" :key="c.ausenciaId">
              <td data-label="Fecha" class="fecha">{{ formatearFecha(c.fecha) }}</td>
              <td data-label="Hora">{{ c.hora ?? 'Sin hora' }}</td>
              <td data-label="Aula">{{ c.aula || 'Sin aula' }}</td>
              <td data-label="Grupo">{{ c.grupo || 'Sin grupo' }}</td>
              <td data-label="Tarea">{{ c.tarea || 'Sin tarea' }}</td>
              <td data-label="Profesor Ausente">
                {{ emailAcortado(c.profesorAusenteEmail) || 'No disponible' }}
              </td>
            </tr>
          </tbody>
        </table>
        <p v-else>No has cubierto ninguna ausencia.</p>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { getAusenciasByProfesor, getCoberturasByProfesor } from '@/services/apiService'
import { useToast } from 'vue-toastification'
import { useUserStore } from '@/stores/user'

const toast = useToast()
const userStore = useUserStore()
const misAusencias = ref([])
const misCoberturas = ref([])
const vista = ref('coberturas')
const email = computed(() => userStore.getUserEmail)

const cargarDatos = async () => {
  try {
    const [ausRes, cobRes] = await Promise.all([
      getAusenciasByProfesor(email.value),
      getCoberturasByProfesor(email.value)
    ])
    misAusencias.value = ausRes.data || []
    misCoberturas.value = Array.isArray(cobRes.data) ? cobRes.data : []
  } catch {
    toast.error('Error al cargar datos de ausencias y coberturas.')
  }
}

onMounted(() => {
  if (email) cargarDatos()
  else toast.error('Email de usuario no encontrado.')
})

const emailAcortado = (e) => {
  if (!e) return ''
  const i = e.indexOf('@')
  return i < 0 ? (e.length > 10 ? e.slice(0,10) + '...' : e) : e.slice(0,i)
}

const formatearFecha = (f) => {
  if (!f) return 'Sin fecha'
  const d = new Date(f)
  const dd = String(d.getDate()).padStart(2,'0')
  const mm = String(d.getMonth()+1).padStart(2,'0')
  return `${dd}-${mm}-${d.getFullYear()}`
}

const ordenarPorFechaHora = (lista) => {
  return [...lista].sort((a,b) => {
    const fa = new Date(`${a.fecha}T${a.hora||'00:00'}`)
    const fb = new Date(`${b.fecha}T${b.hora||'00:00'}`)
    return fa - fb
  })
}

const ausenciasOrdenadas = computed(() => ordenarPorFechaHora(misAusencias.value))
const coberturasOrdenadas = computed(() => ordenarPorFechaHora(misCoberturas.value))
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Montserrat:wght@400;600&display=swap');
* { box-sizing: border-box; font-family: 'Montserrat', sans-serif; }

.mis-guardias-container {
  max-width: 1000px;
  margin: 20px auto;
  padding: 10px;
  color: #333;
}

/* Toggle */
.toggle-buttons {
  display: flex;
  justify-content: center;
  gap: 10px;
  margin-bottom: 20px;
}
.toggle-buttons button {
  padding: 10px 20px;
  border: 2px solid #1f86a1;
  background: #fff;
  color: #1f86a1;
  font-weight: 600;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s;
}
.toggle-buttons button.active,
.toggle-buttons button:hover {
  background: #1f86a1;
  color: #fff;
}

/* Wrapper para scroll suave */
.table-wrapper {
  overflow-x: auto;
  -webkit-overflow-scrolling: touch;
  margin-bottom: 15px;
}

/* Tabla base */
table {
  width: 100%;
  border-collapse: collapse;
  box-shadow: 0 2px 6px rgba(0,0,0,0.1);
  background: #fff;
  min-width: 600px;
}
th, td {
  padding: 12px;
  text-align: center;
  border-bottom: 1px solid #e0e0e0;
  font-size: 14px;
}
th {
  background: #1f86a1;
  color: #fff;
  text-transform: uppercase;
  font-weight: 600;
}
td.fecha {
  background: #1f86a1;
  color: #fff;
  font-weight: bold;
}
p {
  text-align: center;
  font-style: italic;
  color: #666;
  margin-top: 10px;
}

/* Responsive: mantiene scroll horizontal */
@media (max-width: 480px) {
  table { min-width: 500px; }
  th, td { padding: 8px; font-size: 12px; }
}
</style>
