<template>
    <div class="form-container">
        <form id="formulario" @submit.prevent="registrarFalta">
            <div class="form-group">
                <label for="fecha">Fecha de Ausencia:</label>
                <input type="date" v-model="fecha" :min="fechaMinima" @change="cargarHorasDelDia" required />
                <p v-if="errorFecha" class="error-msg">No se puede seleccionar sábado ni domingo.</p>
            </div>

            <div class="form-group">
                <label>¿Falta todo el día?</label>
                <div class="toggle-group">
                    <button type="button" :class="{ active: diaCompleto === 'true' }"
                        @click="seleccionarDiaCompleto('true')">
                        Sí
                    </button>
                    <button type="button" :class="{ active: diaCompleto === 'false' }"
                        @click="seleccionarDiaCompleto('false')">
                        No
                    </button>
                </div>
            </div>

            <p v-if="horasClase.length && diaCompleto === 'false'" class="small-info">
                Marca la casilla de las horas a las que no asistirás ✓
            </p>

            <transition-group name="fade" tag="div" class="form-group" v-if="horasClase.length">
                <label class="form-label">Horas con clase ese día:</label>
                <div v-for="(hora, index) in horasClase" :key="index" class="hora-tarea-card"
                    :class="{ selected: hora.seleccionada }">
                    <div class="hora-tarea-header">
                        <template v-if="diaCompleto === 'false'">
                            <input type="checkbox" v-model="hora.seleccionada" class="hora-checkbox" />
                        </template>
                        <span class="hora-info">
                            🕒 {{ hora.nombre }} - {{ hora.asignatura }} - {{ hora.aula }}
                        </span>
                    </div>
                    <textarea v-model="hora.descripcion" placeholder="Descripción de la tarea"
                        :disabled="diaCompleto === 'false' && !hora.seleccionada" class="tarea-descripcion"></textarea>
                </div>
            </transition-group>

            <div class="form-buttons" v-if="horasClase.length">
                <button type="submit" class="btn btn-primary" :disabled="cargando">
                    <span v-if="cargando" class="spinner"></span>
                    <span v-else>Guardar Falta</span>
                </button>
                <button type="button" @click="limpiarFormulario" class="btn btn-secondary" :disabled="cargando">
                    Restablecer Datos
                </button>
            </div>
        </form>
    </div>
</template>

<script setup>
import { ref, onMounted, watch, nextTick } from "vue";
import { useToast } from "vue-toastification";
import { useUserStore } from '@/stores/user';
import { 
    getProfesorByEmailHorarios,
    getHorarioProfesorByDay,
    getGuardiaByDayAndHour,
    incrementarGuardiaNormal,
    incrementarGuardiaProblematica,
    createAusenciaMultiple
} from '@/services/apiService';

const toast = useToast();
const userStore = useUserStore();

const correo = ref("");
const fecha = ref("");
const fechaMinima = ref("");
const diaCompleto = ref("");
const horasClase = ref([]);
const errorFecha = ref(false);
const cargando = ref(false);

onMounted(async () => {
    const hoy = new Date();
    const year = hoy.getFullYear();
    const month = String(hoy.getMonth() + 1).padStart(2, "0");
    const day = String(hoy.getDate()).padStart(2, "0");
    fechaMinima.value = `${year}-${month}-${day}`;
    
    // Asegurar que el email esté disponible - usar el getter reactivo
    await nextTick();
    correo.value = userStore.getUserEmail || localStorage.getItem("userEmail") || "";
    
    // Si ya hay fecha y día completo seleccionados, cargar las horas
    if (fecha.value && diaCompleto.value !== "" && correo.value) {
        cargarHorasDelDia();
    }
});

// Watch para detectar cuando el email esté disponible - usar el getter reactivo
watch(() => userStore.getUserEmail, (newEmail) => {
    if (newEmail && !correo.value) {
        correo.value = newEmail;
        // Si ya hay fecha y día completo seleccionados, cargar las horas automáticamente
        if (fecha.value && diaCompleto.value !== "") {
            cargarHorasDelDia();
        }
    }
});

const seleccionarDiaCompleto = valor => {
    diaCompleto.value = valor;
    cargarHorasDelDia();
};

const cargarHorasDelDia = async () => {
    const dia = new Date(fecha.value).getDay();
    if (dia === 0 || dia === 6) {
        errorFecha.value = true;
        fecha.value = "";
        return;
    }
    errorFecha.value = false;

    if (!fecha.value || diaCompleto.value === "") return;

    // Validar que el email esté disponible
    if (!correo.value) {
        console.warn('📧 Email no disponible, reintentando en el siguiente ciclo...');
        // Reintentar después de un pequeño delay
        setTimeout(() => {
            if (userStore.getUserEmail && !correo.value) {
                correo.value = userStore.getUserEmail;
                cargarHorasDelDia();
            }
        }, 100);
        return;
    }

    try {
        console.log('🔍 Cargando horario para:', correo.value);
        
        const profesorResponse = await getProfesorByEmailHorarios(correo.value);
        console.log('👤 Respuesta profesor:', profesorResponse.data);
        
        const id = profesorResponse.data.id;
        const diaSemana = dia === 0 ? 7 : dia;

        const horarioResponse = await getHorarioProfesorByDay(id, diaSemana);
        const actividades = horarioResponse.data.actividades || [];

        horasClase.value = actividades.map(hora => ({
            nombre: `${hora.hora}ª Hora`,
            valor: hora.hora,
            asignatura: hora.asignatura,
            aula: hora.abreviaturaAula || hora.aula,
            grupo: hora.grupo,
            seleccionada: diaCompleto.value === "true",
            descripcion: ""
        }));
    } catch (error) {
        console.error('Error cargando horario:', error);
        toast.error("Error al cargar el horario del día.");
    }
};

const registrarFalta = async () => {
    const date = new Date(fecha.value);
    const diaSemana = date.getDay() === 0 ? 7 : date.getDay();

    const horasSeleccionadas = horasClase.value
        .filter(h => diaCompleto.value === "true" || h.seleccionada);

    if (horasSeleccionadas.length === 0) {
        toast.error("Por favor, selecciona al menos una hora afectada.");
        return;
    }

    try {
        cargando.value = true;
        
        // Preparar datos para el endpoint múltiple
        const ausenciaMultipleDTO = {
            profesorAusenteEmail: correo.value,
            fecha: fecha.value,
            horas: horasSeleccionadas.map(hora => ({
                hora: parseInt(hora.valor),
                grupo: hora.grupo,
                aula: hora.aula,
                tarea: hora.descripcion.trim() || "No especificada"
            }))
        };
        
        console.log('Enviando ausencia múltiple:', ausenciaMultipleDTO);
        console.log('JSON que se enviará:', JSON.stringify(ausenciaMultipleDTO, null, 2));
        
        // Crear ausencias múltiples en una sola petición
        const response = await createAusenciaMultiple(ausenciaMultipleDTO);
        
        if (response?.data && Array.isArray(response.data)) {
            toast.success(`✅ ${response.data.length} ausencias registradas correctamente`);
            
            // Actualizar contadores de profesores de guardia (opcional, ya que el backend lo maneja)
            // Se puede omitir si el backend actualiza automáticamente
            for (const ausencia of response.data) {
                try {
                    if (ausencia.profesorEnGuardiaId) {
                        // Determinar si es grupo conflictivo
                        const horaOriginal = horasSeleccionadas.find(h => h.valor === ausencia.hora);
                        const esConflictivo = horaOriginal?.grupo?.esProblematico === true || horaOriginal?.grupo?.esProblematico === 1;
                        
                        // Incrementar contador correspondiente
                        if (esConflictivo) {
                            await incrementarGuardiaProblematica(ausencia.profesorEnGuardiaId);
                        } else {
                            await incrementarGuardiaNormal(ausencia.profesorEnGuardiaId);
                        }
                    }
                } catch (error) {
                    console.warn('Error actualizando contadores:', error);
                    // No mostramos error al usuario ya que las ausencias se guardaron correctamente
                }
            }
        }

        toast.success("Ausencia registrada correctamente");

        setTimeout(() => {
            limpiarFormulario();
            window.location.href = "/";
        }, 2000);
        
    } catch (error) {
        console.error('Error registrando ausencias:', error);
        console.error('Detalles del error:', {
            status: error.response?.status,
            data: error.response?.data,
            message: error.message
        });
        
        if (error.response?.status === 409) {
            toast.error('❌ Ya existe una ausencia para alguna de las horas seleccionadas');
        } else if (error.response?.status === 400) {
            toast.error('❌ Datos inválidos: ' + (error.response?.data?.message || 'Verifica los datos enviados'));
        } else if (error.response?.status === 500) {
            toast.error('❌ Error interno del servidor. Contacta con el administrador.');
        } else {
            toast.error('❌ Error registrando las ausencias. Inténtalo de nuevo.');
        }
    } finally {
        cargando.value = false;
    }
};

const limpiarFormulario = () => {
    fecha.value = "";
    diaCompleto.value = "";
    horasClase.value = [];
};
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Montserrat:wght@400;600&display=swap');

* {
    box-sizing: border-box;
    font-family: 'Montserrat', sans-serif;
}

.form-container {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 10px;
    min-height: 100vh;
}

form {
    background: #fff;
    padding: 25px;
    border-radius: 12px;
    box-shadow: 0 5px 20px rgba(0, 0, 0, 0.1);
    width: 100%;
    max-width: 540px;
}

.form-group {
    margin-bottom: 20px;
}

label {
    font-weight: 600;
    margin-bottom: 8px;
    display: block;
}

input[type="date"],
textarea {
    width: 100%;
    padding: 10px;
    border-radius: 6px;
    border: 1px solid #ccc;
    font-size: 14px;
    background: #f1f1f1;
}

textarea {
    resize: none;
    height: 60px;
}

.toggle-group {
    display: flex;
    gap: 10px;
}

.toggle-group button {
    flex: 1;
    padding: 10px;
    border: 1px solid #ccc;
    background: #eee;
    cursor: pointer;
    font-weight: 600;
    border-radius: 6px;
    transition: all 0.3s ease;
}

.toggle-group button.active {
    background: #1f86a1;
    color: white;
    border-color: #1f86a1;
}

.hora-tarea-card {
    background: #f9f9f9;
    border: 1px solid #ddd;
    border-radius: 10px;
    padding: 10px;
    margin-bottom: 12px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
    transition: background 0.3s ease;
}

.hora-tarea-card.selected {
    background-color: #e6f7ff;
}

.hora-tarea-header {
    display: flex;
    align-items: center;
    gap: 10px;
}

.hora-checkbox {
    transform: scale(1.3);
}

.hora-info {
    font-weight: 500;
    color: #333;
}

.form-buttons {
    display: flex;
    justify-content: space-between;
    margin-top: 20px;
}

button {
    padding: 10px 20px;
    border-radius: 6px;
    font-weight: 600;
    cursor: pointer;
    transition: background 0.3s ease;
    font-size: 14px;
    border: none;
}

.btn-primary {
    background: #1f86a1;
    color: #fff;
}

.btn-primary:hover {
    background: #15657a;
}

.btn-secondary {
    background: #08222b;
    color: white;
}

.btn-secondary:hover {
    background: #0c3a4a;
}

.error-msg {
    color: red;
    margin-top: 5px;
    font-size: 13px;
}

.spinner {
    border: 3px solid #f3f3f3;
    border-top: 3px solid #ffffff;
    border-radius: 50%;
    width: 16px;
    height: 16px;
    animation: spin 0.8s linear infinite;
    display: inline-block;
    margin-right: 8px;
    vertical-align: middle;
}

.small-info {
    font-size: 12px;
    color: #555;
    margin-bottom: 8px;
    font-style: italic;
    margin-left: 5%;
}

@keyframes spin {
    to {
        transform: rotate(360deg);
    }
}

/* Animaciones */
.fade-enter-active,
.fade-leave-active {
    transition: all 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
    opacity: 0;
    transform: translateY(-10px);
}

/* Responsive fino para móviles */
@media (max-width: 480px) {
    form {
        padding: 20px;
    }

    .form-buttons {
        flex-direction: column;
        gap: 10px;
    }

    .toggle-group {
        flex-direction: column;
    }
}
</style>