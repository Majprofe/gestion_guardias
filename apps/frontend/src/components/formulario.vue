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
                <label key="label-horas" class="form-label">Horas con clase ese día:</label>
                <div v-for="(hora, index) in horasClase" :key="`hora-${index}`" class="hora-tarea-card"
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
                    
                    <!-- Sección de archivos PDF -->
                    <div class="archivos-section" v-if="hora.seleccionada || diaCompleto === 'true'">
                        <label class="archivos-label">
                            📎 Material adicional (opcional, máx. 3 archivos PDF de 30MB cada uno):
                        </label>
                        <input 
                            type="file" 
                            accept="application/pdf,.pdf"
                            multiple
                            :disabled="diaCompleto === 'false' && !hora.seleccionada"
                            @change="handleFileChange($event, index)"
                            class="file-input"
                            :ref="el => fileInputs[index] = el"
                        />
                        
                        <!-- Preview de archivos seleccionados -->
                        <div v-if="hora.archivos && hora.archivos.length > 0" class="archivos-preview">
                            <div v-for="(archivo, fileIndex) in hora.archivos" :key="`archivo-${index}-${fileIndex}`" 
                                 class="archivo-item">
                                <span class="archivo-icon">📄</span>
                                <span class="archivo-nombre">{{ archivo.name }}</span>
                                <span class="archivo-tamano">({{ formatFileSize(archivo.size) }})</span>
                                <button type="button" @click="removeFile(index, fileIndex)" class="btn-remove-file">
                                    ✕
                                </button>
                            </div>
                        </div>
                    </div>
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
import { getHorarioByEmailProfesor, getHorarioProfesorPorDia, crearAusencia } from "@/services/api";

const toast = useToast();

const correo = ref("");
const fecha = ref("");
const fechaMinima = ref("");
const diaCompleto = ref("");
const horasClase = ref([]);
const errorFecha = ref(false);
const cargando = ref(false);
const fileInputs = ref([]);

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
    if (!fecha.value) return;

    // Corregir problema de zona horaria añadiendo 'T00:00:00' para forzar hora local
    const fechaLocal = new Date(fecha.value + 'T00:00:00');
    const dia = fechaLocal.getDay();
    
    // Validar que no sea sábado (6) ni domingo (0)
    if (dia === 0 || dia === 6) {
        errorFecha.value = true;
        horasClase.value = [];
        toast.warning("No se puede seleccionar sábado ni domingo.");
        return;
    }
    errorFecha.value = false;

    // Verificar que se haya seleccionado si falta todo el día o no
    if (diaCompleto.value === "") {
        horasClase.value = [];
        return;
    }

    // Validar que haya un correo
    if (!correo.value) {
        toast.error("No se encontró el correo del usuario. Por favor, inicia sesión nuevamente.");
        return;
    }

    try {
        const profesorResponse = await getHorarioByEmailProfesor(correo.value);
        const id = profesorResponse.data.profesorId;
        
        if (!id) {
            toast.error("No se pudo obtener la información del profesor.");
            return;
        }

        // Convertir día de la semana (lunes=1, martes=2, ..., viernes=5)
        // getDay() devuelve: domingo=0, lunes=1, ..., sábado=6
        const diaSemana = dia === 0 ? 7 : dia;

        const horarioResponse = await getHorarioProfesorPorDia(id, diaSemana);
        const actividades = horarioResponse.data.actividades || [];

        if (actividades.length === 0) {
            toast.info("No tienes clases ese día.");
            horasClase.value = [];
            return;
        }

        horasClase.value = actividades.map(hora => ({
            nombre: `${hora.hora}ª Hora`,
            valor: hora.hora,
            asignatura: hora.asignatura,
            aula: hora.abreviaturaAula || hora.aula,
            grupo: hora.grupo,
            seleccionada: diaCompleto.value === "true",
            descripcion: "",
            archivos: [] // Array para almacenar archivos PDF
        }));

        toast.success(`Horario cargado: ${actividades.length} clase(s) encontrada(s)`);
    } catch (error) {
        toast.error(`Error al cargar el horario: ${error.response?.data?.message || error.message}`);
        horasClase.value = [];
    }
};

// Formatear tamaño de archivo
const formatFileSize = (bytes) => {
    if (bytes < 1024) return bytes + ' B';
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(2) + ' KB';
    return (bytes / (1024 * 1024)).toFixed(2) + ' MB';
};

// Manejar cambio de archivos
const handleFileChange = (event, horaIndex) => {
    const files = Array.from(event.target.files);
    const maxFiles = 3;
    const maxSize = 30 * 1024 * 1024; // 30MB

    // Validar número de archivos
    if (files.length > maxFiles) {
        toast.error(`Solo puedes subir un máximo de ${maxFiles} archivos por hora`);
        event.target.value = ''; // Limpiar input
        return;
    }

    // Validar tamaño y tipo de cada archivo
    const invalidFiles = files.filter(file => {
        if (file.size > maxSize) {
            toast.error(`El archivo "${file.name}" excede el tamaño máximo de 30MB`);
            return true;
        }
        if (file.type !== 'application/pdf') {
            toast.error(`El archivo "${file.name}" no es un PDF válido`);
            return true;
        }
        return false;
    });

    if (invalidFiles.length > 0) {
        event.target.value = ''; // Limpiar input
        return;
    }

    // Asignar archivos válidos
    horasClase.value[horaIndex].archivos = files;
    toast.success(`${files.length} archivo(s) añadido(s) a ${horasClase.value[horaIndex].nombre}`);
};

// Eliminar un archivo específico
const removeFile = (horaIndex, fileIndex) => {
    horasClase.value[horaIndex].archivos.splice(fileIndex, 1);
    // Limpiar el input de archivo si no quedan archivos
    if (horasClase.value[horaIndex].archivos.length === 0 && fileInputs.value[horaIndex]) {
        fileInputs.value[horaIndex].value = '';
    }
    toast.info('Archivo eliminado');
};

const registrarFalta = async () => {
    // Filtrar horas seleccionadas
    const horasSeleccionadas = horasClase.value
        .filter(h => diaCompleto.value === "true" || h.seleccionada);

    if (horasSeleccionadas.length === 0) {
        toast.error("Por favor, selecciona al menos una hora afectada.");
        return;
    }

    // Validar que todas las horas seleccionadas tengan tarea
    const horasSinTarea = horasSeleccionadas.filter(h => !h.descripcion || h.descripcion.trim() === "");
    
    if (horasSinTarea.length > 0) {
        const horasTexto = horasSinTarea.map(h => h.nombre).join(", ");
        toast.error(`Debes especificar una tarea para: ${horasTexto}`);
        return;
    }

    // Preparar el DTO con el nuevo formato
    const ausenciaDTO = {
        profesorAusenteEmail: correo.value,
        fecha: fecha.value,
        horas: horasSeleccionadas.map(h => ({
            hora: parseInt(h.valor),
            grupo: h.grupo,
            aula: h.aula,
            tarea: h.descripcion.trim()
        }))
    };

    try {
        cargando.value = true;
        
        // Crear FormData para enviar archivos
        const formData = new FormData();
        
        // Añadir el JSON de la ausencia
        formData.append('ausenciaData', JSON.stringify(ausenciaDTO));
        
        // Añadir archivos por cada hora (si existen)
        horasSeleccionadas.forEach((hora, index) => {
            if (hora.archivos && hora.archivos.length > 0) {
                hora.archivos.forEach(archivo => {
                    formData.append(`archivos_hora_${index}`, archivo);
                });
            }
        });
        
        // Enviar ausencia con archivos al backend usando api.js
        const response = await crearAusencia(formData);

        const totalArchivos = horasSeleccionadas.reduce((acc, h) => acc + (h.archivos?.length || 0), 0);
        if (totalArchivos > 0) {
            toast.success(`Ausencia y ${totalArchivos} archivo(s) registrados correctamente`);
        } else {
            toast.success("Ausencia registrada correctamente");
        }

        setTimeout(() => {
            limpiarFormulario();
            window.location.href = "/";
        }, 2000);
    } catch (error) {
        console.error("Error al registrar ausencia:", error);
        const errorMsg = error.response?.data?.message || error.message || "Error desconocido";
        toast.error(`Error al registrar la ausencia: ${errorMsg}`);
    } finally {
        cargando.value = false;
    }
};


const limpiarFormulario = () => {
    fecha.value = "";
    diaCompleto.value = "";
    horasClase.value = [];
    // Limpiar inputs de archivos
    fileInputs.value.forEach(input => {
        if (input) input.value = '';
    });
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

/* Estilos para sección de archivos */
.archivos-section {
    margin-top: 12px;
    padding: 12px;
    background: #f5f5f5;
    border-radius: 8px;
    border: 1px dashed #ccc;
}

.archivos-label {
    font-size: 13px;
    color: #555;
    margin-bottom: 8px;
    display: block;
    font-weight: 500;
}

.file-input {
    width: 100%;
    padding: 8px;
    background: white;
    border: 1px solid #ddd;
    border-radius: 6px;
    font-size: 13px;
    cursor: pointer;
}

.file-input:disabled {
    background: #e9ecef;
    cursor: not-allowed;
}

.archivos-preview {
    margin-top: 10px;
    display: flex;
    flex-direction: column;
    gap: 8px;
}

.archivo-item {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 8px;
    background: white;
    border: 1px solid #ddd;
    border-radius: 6px;
    font-size: 13px;
}

.archivo-icon {
    font-size: 18px;
}

.archivo-nombre {
    flex: 1;
    color: #333;
    font-weight: 500;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.archivo-tamano {
    color: #666;
    font-size: 12px;
}

.btn-remove-file {
    background: #dc3545;
    color: white;
    border: none;
    border-radius: 4px;
    padding: 4px 8px;
    cursor: pointer;
    font-size: 14px;
    line-height: 1;
    transition: background 0.2s ease;
}

.btn-remove-file:hover {
    background: #c82333;
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

    .archivo-nombre {
        max-width: 150px;
    }
}
</style>
