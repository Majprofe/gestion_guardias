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
import { ref, onMounted } from "vue";
import axios from "axios";
import { useToast } from "vue-toastification";

const toast = useToast();
const API_URL = import.meta.env.VITE_API_URL;

const correo = ref("");
const fecha = ref("");
const fechaMinima = ref("");
const diaCompleto = ref("");
const horasClase = ref([]);
const errorFecha = ref(false);
const cargando = ref(false);

onMounted(() => {
    const hoy = new Date();
    const year = hoy.getFullYear();
    const month = String(hoy.getMonth() + 1).padStart(2, "0");
    const day = String(hoy.getDate()).padStart(2, "0");
    fechaMinima.value = `${year}-${month}-${day}`;
    correo.value = localStorage.getItem("userEmail") || "";
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

    try {
        const profesorResponse = await axios.get(`http://localhost:8080/profesores/email/${correo.value}`);
        const id = profesorResponse.data.id;

        const diaSemana = dia === 0 ? 7 : dia;

        const horarioResponse = await axios.get(`http://localhost:8080/horario/profesor/${id}/dia/${diaSemana}`);
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
        for (const h of horasSeleccionadas) {
            const guardiaURL = `http://localhost:8080/horario/guardia/dia/${diaSemana}/hora/${h.valor}`;
            const guardiaResp = await axios.get(guardiaURL);
            const profesores = guardiaResp.data.profesores || [];

            if (profesores.length === 0) {
                toast.warning(`No hay profesores en guardia para la hora ${h.valor}`);
                continue;
            }

            // Determinar si es grupo conflictivo
            const esConflictivo = h.grupo?.esProblematico === true || h.grupo?.esProblematico === 1;

            // Selección basada en tipo de grupo
            const profesorSeleccionado = profesores.reduce((menor, actual) => {
                const metricaMenor = esConflictivo ? menor.guardiasProblematicas : menor.guardiasRealizadas;
                const metricaActual = esConflictivo ? actual.guardiasProblematicas : actual.guardiasRealizadas;
                return metricaActual < metricaMenor ? actual : menor;
            });

            const ausenciaDTO = {
                profesorAusenteEmail: correo.value,
                fecha: fecha.value,
                hora: parseInt(h.valor),
                tarea: h.descripcion || "No especificada",
                profesorEnGuardiaEmail: profesorSeleccionado.email,
                grupo: h.grupo,
                aula: h.aula
            };

            // Guardar la ausencia
            await axios.post("http://localhost:8081/api/ausencias", ausenciaDTO, {
                headers: { "Content-Type": "application/json" }
            });

            // Actualizar contador según tipo de grupo
            const endpoint = esConflictivo
                ? "incrementar-guardia-problematica"
                : "incrementar-guardia-normal";
            await axios.post(`http://localhost:8080/profesores/${endpoint}/${profesorSeleccionado.id}`);
        }

        toast.success("Ausencia registrada correctamente");

        setTimeout(() => {
            limpiarFormulario();
            window.location.href = "/";
        }, 2000);
    } catch (error) {
        toast.error(`Error al registrar la ausencia.`);
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
