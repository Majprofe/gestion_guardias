<template>
    <div class="horario-container">
        <h1 class="titulo">Mi Horario</h1>
        
        <div v-if="profesorNombre" class="profesor-info">
            <span class="profesor-nombre">{{ profesorNombre }}</span>
        </div>

        <div v-if="Object.keys(horarioTransformado).length" class="tabla-wrapper">
            <div class="tabla-container">
                <table class="horario-tabla">
                    <thead>
                        <tr>
                            <th class="celda-hora-header">Hora</th>
                            <th v-for="dia in ['lunes', 'martes', 'miércoles', 'jueves', 'viernes']" 
                                :key="dia" 
                                class="celda-dia-header">
                                <span class="dia-completo">{{ dia.charAt(0).toUpperCase() + dia.slice(1) }}</span>
                                <span class="dia-corto">{{ dia.slice(0, 3).toUpperCase() }}</span>
                            </th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr v-for="hora in Object.keys(horarioTransformado).sort((a, b) => a - b)" 
                            :key="hora" 
                            class="fila-horario">
                            <td class="celda-hora">{{ hora }}</td>
                            <td v-for="dia in ['lunes', 'martes', 'miércoles', 'jueves', 'viernes']" 
                                :key="dia" 
                                class="celda-clase"
                                :class="{ 'tiene-clase': horarioTransformado[hora][dia] }">
                                <div v-if="horarioTransformado[hora][dia]" class="clase-info">
                                    <div class="asignatura-nombre">{{ horarioTransformado[hora][dia].asignatura }}</div>
                                    <div class="clase-detalles">
                                        <span class="grupo">{{ horarioTransformado[hora][dia].grupo || '—' }}</span>
                                        <span class="separador">·</span>
                                        <span class="aula">{{ horarioTransformado[hora][dia].abreviaturaAula || '—' }}</span>
                                    </div>
                                </div>
                                <div v-else class="sin-clase">—</div>
                            </td>
                        </tr>
                    </tbody>
                </table>
            </div>
        </div>

        <div v-else class="loading-container">
            <div class="loading-spinner"></div>
            <p class="loading-text">Cargando horario...</p>
        </div>
    </div>
</template>


<script setup>
import axios from 'axios'
import { ref, watch } from 'vue'
import { useAuth } from "@/composables/useAuth"
import { useToast } from "vue-toastification"
import { getHorarioByEmailProfesor } from "@/services/api"

const toast = useToast()
const { userEmail } = useAuth()
const horario = ref(null)
const profesorNombre = ref('')
const horarioTransformado = ref({})

watch(
    () => userEmail.value,
    async (email) => {
        if (email) {
            try {
                const response = await getHorarioByEmailProfesor(email)
                profesorNombre.value = response.data.profesorNombre
                horario.value = response.data.horario
                transformarHorario()
            } catch (error) {
                toast.error("Error al cargar el horario del profesor.")
                setTimeout(() => {
                    window.location.href = "/";
                }, 4000);
            }
        }
    },
    { immediate: true }
)

function transformarHorario() {
    const nuevoHorario = {}
    const todasLasHoras = ['1', '2', '3', '4', '5', '6']
    const diasSemana = ['lunes', 'martes', 'miércoles', 'jueves', 'viernes']
    
    // Inicializar todas las horas
    for (const hora of todasLasHoras) {
        nuevoHorario[hora] = {}
        for (const dia of diasSemana) {
            nuevoHorario[hora][dia] = null
        }
    }
    
    // Llenar con las clases existentes
    for (const dia of diasSemana) {
        const actividades = horario.value[dia] || []
        for (const actividad of actividades) {
            const hora = actividad.hora
            if (nuevoHorario[hora]) {
                nuevoHorario[hora][dia] = actividad
            }
        }
    }

    horarioTransformado.value = nuevoHorario
}
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Montserrat:wght@300;400;500;600;700&display=swap');

* {
    box-sizing: border-box;
    font-family: 'Montserrat', sans-serif;
}

.horario-container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 1.5rem;
    background: #f8f9fa;
    min-height: 100vh;
}

.titulo {
    text-align: center;
    color: #2c5282;
    font-size: 2rem;
    font-weight: 600;
    margin-bottom: 1rem;
    letter-spacing: -0.5px;
}

.profesor-info {
    text-align: center;
    margin-bottom: 2rem;
}

.profesor-nombre {
    display: inline-block;
    padding: 0.75rem 1.5rem;
    background: #2c5282;
    color: white;
    border-radius: 6px;
    font-weight: 500;
    font-size: 1rem;
    border: 1px solid #2a4f7c;
}

.tabla-wrapper {
    background: white;
    border-radius: 8px;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
    border: 1px solid #e2e8f0;
    overflow: hidden;
}

.tabla-container {
    overflow-x: auto;
    -webkit-overflow-scrolling: touch;
}

.horario-tabla {
    width: 100%;
    border-collapse: collapse;
    min-width: 600px;
    font-size: 0.875rem;
}

/* HEADERS */
.celda-hora-header {
    background: #4a5568;
    color: white;
    padding: 1rem 0.75rem;
    font-weight: 600;
    text-align: center;
    font-size: 0.875rem;
    border-bottom: 1px solid #e2e8f0;
    min-width: 80px;
}

.celda-dia-header {
    background: #2c5282;
    color: white;
    padding: 1rem 0.75rem;
    font-weight: 600;
    text-align: center;
    font-size: 0.875rem;
    border-bottom: 1px solid #e2e8f0;
    min-width: 140px;
}

.dia-completo {
    display: block;
}

.dia-corto {
    display: none;
}

/* FILAS */
.fila-horario:nth-child(odd) {
    background-color: #f7fafc;
}

.fila-horario:nth-child(even) {
    background-color: white;
}

/* CELDAS */
.celda-hora {
    background: #edf2f7;
    color: #2d3748;
    font-weight: 600;
    text-align: center;
    padding: 1rem 0.75rem;
    font-size: 0.875rem;
    border-right: 1px solid #e2e8f0;
    border-bottom: 1px solid #e2e8f0;
}

.celda-clase {
    padding: 0.75rem;
    text-align: center;
    vertical-align: middle;
    border-right: 1px solid #e2e8f0;
    border-bottom: 1px solid #e2e8f0;
    position: relative;
    min-height: 80px;
}

.celda-clase.tiene-clase {
    background: white;
}



/* CONTENIDO DE CLASES */
.clase-info {
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
    height: 100%;
    justify-content: center;
    padding: 0.5rem;
}

.asignatura-nombre {
    font-weight: 600;
    font-size: 0.875rem;
    color: #2d3748;
    line-height: 1.3;
    text-align: center;
    margin-bottom: 0.25rem;
}

.clase-detalles {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 0.5rem;
    font-size: 0.75rem;
    color: #4a5568;
}

.grupo {
    background: #e6fffa;
    color: #234e52;
    padding: 0.25rem 0.5rem;
    border-radius: 4px;
    font-weight: 500;
    border: 1px solid #b2f5ea;
}

.aula {
    background: #fed7d7;
    color: #742a2a;
    padding: 0.25rem 0.5rem;
    border-radius: 4px;
    font-weight: 500;
    border: 1px solid #feb2b2;
}

.separador {
    color: #a0aec0;
    font-weight: 600;
}

.sin-clase {
    color: #a0aec0;
    font-size: 1rem;
    opacity: 0.7;
    font-weight: 400;
}

/* LOADING */
.loading-container {
    text-align: center;
    padding: 4rem 2rem;
}

.loading-spinner {
    width: 50px;
    height: 50px;
    border: 4px solid #e3f2fd;
    border-top: 4px solid #1F86A1;
    border-radius: 50%;
    animation: spin 1s linear infinite;
    margin: 0 auto 1.5rem;
}

@keyframes spin {
    0% { transform: rotate(0deg); }
    100% { transform: rotate(360deg); }
}

.loading-text {
    color: #7f8c8d;
    font-style: italic;
    font-size: 1.2rem;
    font-weight: 500;
}

/* RESPONSIVE */
@media (max-width: 768px) {
    .horario-container {
        padding: 1rem;
    }
    
    .titulo {
        font-size: 1.5rem;
        margin-bottom: 1rem;
    }
    
    .profesor-nombre {
        font-size: 0.875rem;
        padding: 0.5rem 1rem;
    }
    
    .celda-hora-header,
    .celda-dia-header {
        padding: 0.75rem 0.5rem;
        font-size: 0.75rem;
    }
    
    .dia-completo {
        display: none;
    }
    
    .dia-corto {
        display: block;
    }
    
    .celda-hora {
        font-size: 0.75rem;
        padding: 0.75rem 0.5rem;
    }
    
    .celda-clase {
        padding: 0.5rem 0.25rem;
        min-height: 70px;
    }
    
    .asignatura-nombre {
        font-size: 0.75rem;
    }
    
    .clase-detalles {
        font-size: 0.65rem;
        gap: 0.25rem;
    }
    
    .grupo, .aula {
        font-size: 0.65rem;
        padding: 0.2rem 0.4rem;
    }
}

@media (max-width: 480px) {
    .titulo {
        font-size: 1.25rem;
    }
    
    .profesor-nombre {
        font-size: 0.75rem;
        padding: 0.5rem 0.75rem;
    }
    
    .celda-hora-header,
    .celda-dia-header {
        padding: 0.5rem 0.25rem;
        font-size: 0.7rem;
    }
    
    .celda-hora {
        font-size: 0.7rem;
        padding: 0.5rem 0.25rem;
    }
    
    .celda-clase {
        padding: 0.25rem;
        min-height: 60px;
    }
    
    .asignatura-nombre {
        font-size: 0.7rem;
        line-height: 1.2;
    }
    
    .clase-detalles {
        font-size: 0.6rem;
        gap: 0.2rem;
    }
    
    .grupo, .aula {
        font-size: 0.6rem;
        padding: 0.15rem 0.3rem;
    }
    
    .sin-clase {
        font-size: 0.875rem;
    }
}

@media (min-width: 1024px) {
    .titulo {
        font-size: 2.25rem;
    }
    
    .profesor-nombre {
        font-size: 1.125rem;
        padding: 1rem 2rem;
    }
    
    .horario-tabla {
        min-width: auto;
    }
    
    .celda-hora-header {
        min-width: 100px;
    }
    
    .celda-dia-header {
        min-width: 180px;
    }
    
    .asignatura-nombre {
        font-size: 1rem;
    }
    
    .clase-detalles {
        font-size: 0.875rem;
    }
    
    .grupo, .aula {
        font-size: 0.75rem;
        padding: 0.3rem 0.6rem;
    }
}
</style>
