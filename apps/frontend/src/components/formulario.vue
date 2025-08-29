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

            <!-- Sección de archivos adjuntos -->
            <div class="form-group" v-if="horasClase.length">
                <label class="form-label">📎 Archivos Adjuntos (opcional):</label>
                <div class="archivos-container">
                    <div class="drag-drop-area" 
                         @drop="onDrop" 
                         @dragover="onDragOver" 
                         @dragenter="onDragEnter" 
                         @dragleave="onDragLeave"
                         :class="{ 'drag-active': dragActive }"
                         @click="$refs.fileInput.click()">
                        <div class="drag-drop-content">
                            <div class="upload-icon">📁</div>
                            <p><strong>Arrastra archivos aquí</strong> o haz clic para seleccionar</p>
                            <p class="upload-hint">PDF, DOC, DOCX, TXT, JPG, PNG (máx. 10MB cada uno)</p>
                        </div>
                        <input 
                            ref="fileInput"
                            type="file" 
                            multiple 
                            @change="onFileSelect" 
                            accept=".pdf,.doc,.docx,.txt,.jpg,.jpeg,.png"
                            style="display: none"
                        />
                    </div>
                    
                    <!-- Lista de archivos seleccionados -->
                    <div v-if="archivosSeleccionados.length" class="archivos-lista">
                        <h4>📋 Archivos seleccionados:</h4>
                        <div v-for="(archivo, index) in archivosSeleccionados" :key="index" class="archivo-item">
                            <div class="archivo-info">
                                <span class="archivo-icon">{{ getFileIcon(archivo.name) }}</span>
                                <div class="archivo-details">
                                    <span class="archivo-nombre">{{ archivo.name }}</span>
                                    <span class="archivo-size">{{ formatFileSize(archivo.size) }}</span>
                                </div>
                            </div>
                            <button type="button" @click="removeFile(index)" class="btn-remove-file" title="Eliminar archivo">
                                ❌
                            </button>
                        </div>
                    </div>
                </div>
            </div>

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
import { 
  getProfesorByEmailHorarios, 
  getHorarioProfesorByDay, 
  getGuardiaByDayAndHour,
  createAusencia,
  incrementarGuardiaNormal,
  incrementarGuardiaProblematica,
  subirArchivoAusencia
} from '@/services/apiService'
import { useToast } from "vue-toastification";
import { useUserStore } from '@/stores/user';

const toast = useToast();
const userStore = useUserStore();
const API_URL = import.meta.env.VITE_API_URL;

const correo = ref("");
const fecha = ref("");
const fechaMinima = ref("");
const diaCompleto = ref("");
const horasClase = ref([]);
const errorFecha = ref(false);
const cargando = ref(false);
const archivosSeleccionados = ref([]);
const dragActive = ref(false);

onMounted(() => {
    const hoy = new Date();
    const year = hoy.getFullYear();
    const month = String(hoy.getMonth() + 1).padStart(2, "0");
    const day = String(hoy.getDate()).padStart(2, "0");
    fechaMinima.value = `${year}-${month}-${day}`;
    correo.value = userStore.getUserEmail || "";
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
        const profesorResponse = await getProfesorByEmailHorarios(correo.value);
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
            const guardiaResp = await getGuardiaByDayAndHour(diaSemana, h.valor);
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
            await createAusencia(ausenciaDTO);

            // Actualizar contador según tipo de grupo
            if (esConflictivo) {
                await incrementarGuardiaProblematica(profesorSeleccionado.id);
            } else {
                await incrementarGuardiaNormal(profesorSeleccionado.id);
            }
        }

        // Subir archivos si hay algunos seleccionados
        if (archivosSeleccionados.value.length > 0) {
            await subirArchivos(ausenciaResponse.data.id);
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

// Métodos para manejo de archivos
const onFileSelect = (event) => {
    const files = Array.from(event.target.files);
    addFiles(files);
};

const onDrop = (event) => {
    event.preventDefault();
    dragActive.value = false;
    const files = Array.from(event.dataTransfer.files);
    addFiles(files);
};

const onDragOver = (event) => {
    event.preventDefault();
};

const onDragEnter = (event) => {
    event.preventDefault();
    dragActive.value = true;
};

const onDragLeave = (event) => {
    event.preventDefault();
    dragActive.value = false;
};

const addFiles = (files) => {
    const allowedTypes = ['application/pdf', 'application/msword', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document', 'text/plain', 'image/jpeg', 'image/jpg', 'image/png'];
    const maxSize = 10 * 1024 * 1024; // 10MB

    for (const file of files) {
        // Validar tipo
        if (!allowedTypes.includes(file.type)) {
            toast.error(`Tipo de archivo no permitido: ${file.name}`);
            continue;
        }
        
        // Validar tamaño
        if (file.size > maxSize) {
            toast.error(`Archivo demasiado grande: ${file.name}. Máximo 10MB.`);
            continue;
        }
        
        // Verificar si ya existe
        if (archivosSeleccionados.value.some(f => f.name === file.name && f.size === file.size)) {
            toast.warning(`Archivo ya seleccionado: ${file.name}`);
            continue;
        }
        
        archivosSeleccionados.value.push(file);
    }
};

const removeFile = (index) => {
    archivosSeleccionados.value.splice(index, 1);
};

const getFileIcon = (fileName) => {
    const ext = fileName.split('.').pop().toLowerCase();
    const icons = {
        'pdf': '📄',
        'doc': '📝',
        'docx': '📝',
        'txt': '📃',
        'jpg': '🖼️',
        'jpeg': '🖼️',
        'png': '🖼️'
    };
    return icons[ext] || '📎';
};

const formatFileSize = (bytes) => {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
};

const subirArchivos = async (ausenciaId) => {
    try {
        for (const archivo of archivosSeleccionados.value) {
            const formData = new FormData();
            formData.append('archivo', archivo);
            formData.append('ausenciaId', ausenciaId);
            
            await subirArchivoAusencia(formData);
        }
        toast.success(`${archivosSeleccionados.value.length} archivo(s) subido(s) correctamente`);
    } catch (error) {
        console.error('Error subiendo archivos:', error);
        toast.warning('Ausencia creada, pero hubo errores subiendo algunos archivos');
    }
};


const limpiarFormulario = () => {
    fecha.value = "";
    diaCompleto.value = "";
    horasClase.value = [];
    archivosSeleccionados.value = [];
    dragActive.value = false;
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

/* Estilos para archivos adjuntos */
.archivos-container {
    margin-top: 10px;
}

.drag-drop-area {
    border: 2px dashed #ccc;
    border-radius: 8px;
    padding: 30px;
    text-align: center;
    background: #fafafa;
    cursor: pointer;
    transition: all 0.3s ease;
}

.drag-drop-area:hover,
.drag-drop-area.drag-active {
    border-color: #1f86a1;
    background: #f0f8ff;
}

.drag-drop-content .upload-icon {
    font-size: 3em;
    margin-bottom: 10px;
}

.drag-drop-content p {
    margin: 5px 0;
    color: #666;
}

.upload-hint {
    font-size: 12px;
    color: #999;
}

.archivos-lista {
    margin-top: 15px;
    padding: 15px;
    background: #f8f9fa;
    border-radius: 8px;
}

.archivos-lista h4 {
    margin-top: 0;
    color: #333;
}

.archivo-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 10px;
    margin: 8px 0;
    background: white;
    border-radius: 6px;
    box-shadow: 0 1px 3px rgba(0,0,0,0.1);
}

.archivo-info {
    display: flex;
    align-items: center;
    gap: 12px;
}

.archivo-icon {
    font-size: 1.5em;
}

.archivo-details {
    display: flex;
    flex-direction: column;
}

.archivo-nombre {
    font-weight: 600;
    color: #333;
}

.archivo-size {
    font-size: 12px;
    color: #666;
}

.btn-remove-file {
    background: none;
    border: none;
    cursor: pointer;
    padding: 5px;
    border-radius: 50%;
    transition: background 0.2s;
}

.btn-remove-file:hover {
    background: rgba(255, 0, 0, 0.1);
}
</style>
