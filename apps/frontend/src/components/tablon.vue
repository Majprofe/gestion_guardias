<template>
    <div class="tablon-faltas-container">
        <label for="fecha">Selecciona una fecha:</label>
        <input type="date" v-model="fecha" :min="fechaHoy" @change="validarFecha" required />
        <p v-if="errorFechaFinDeSemana" class="error-fecha">⚠️ No se pueden seleccionar sábados ni domingos.</p>

        <div v-if="Object.keys(faltasPorHora).length">
            <div v-for="(faltasHora, hora) in faltasPorHora" :key="hora" class="hora-section">
                <h3 class="hora-titulo">{{ nombreHora(hora) }}</h3>
                
                <!-- Mostrar aula de convivencia primero si existe -->
                <div v-if="aulaConvivencia[hora]" class="convivencia-banner">
                    <span class="convivencia-icon">🏫</span>
                    <span class="convivencia-texto">
                        <strong>Aula de Convivencia:</strong> 
                        {{ aulaConvivencia[hora].nombre || aulaConvivencia[hora].email }}
                    </span>
                </div>

                <table class="faltas-table">
                    <thead>
                        <tr>
                            <th>Profesor Ausente</th>
                            <th>Aula - Grupo</th>
                            <th>Tarea</th>
                            <th>Profesor que Cubre</th>
                            <th>Material</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr v-for="(falta, index) in faltasHora" :key="index">
                            <td :data-label="'Profesor Ausente'">{{ falta.profesorNombre || falta.profesorEmail }}</td>
                            <td :data-label="'Aula - Grupo'">{{ falta.aula }} - {{ falta.grupo }}</td>
                            <td :data-label="'Tarea'" class="tarea-celda">
                                <span class="tarea-texto">{{ falta.tarea || 'Sin especificar' }}</span>
                            </td>
                            <td :data-label="'Profesor que Cubre'">
                                <span v-if="falta.cobertura">
                                    {{ falta.profesorCubreNombre || falta.profesorCubreEmail }}
                                </span>
                                <span v-else class="no-cubrir">No cubierta</span>
                            </td>
                            <td :data-label="'Material'" class="material-celda">
                                <div v-if="falta.archivos && falta.archivos.length > 0" class="material-archivos">
                                    <a v-for="archivo in falta.archivos" 
                                       :key="archivo.id"
                                       :href="'http://localhost:8081' + archivo.urlDescarga"
                                       download
                                       class="archivo-link"
                                       :title="archivo.nombreArchivo">
                                        📄 {{ acortarNombreArchivo(archivo.nombreArchivo) }}
                                    </a>
                                </div>
                                <span v-else class="sin-material">Sin material</span>
                            </td>
                        </tr>
                    </tbody>
                </table>
            </div>
        </div>

        <p v-if="fecha && Object.keys(faltasPorHora).length === 0">No hay faltas registradas para esta fecha.</p>
    </div>
</template>

<script setup>
import { ref, onMounted, computed } from "vue";
import axios from "axios";
import { useToast } from "vue-toastification";
import { watch } from "vue";

const toast = useToast();

const fecha = ref("");
const faltasPorHora = ref({});
const aulaConvivencia = ref({}); // Profesores de convivencia por hora {hora: {email: string, nombre: string}}
const usuarioEmail = ref(null);
const errorFechaFinDeSemana = ref(false);

const fechaHoy = new Date().toISOString().split("T")[0];
const esFechaActual = computed(() => fecha.value === fechaHoy);

const emailAcortado = (email) => {
    if (!email || typeof email !== 'string') return "No disponible";
    const indiceArroba = email.indexOf('@');
    return indiceArroba === -1 ? email.slice(0, 10) + '...' : email.slice(0, indiceArroba);
};

const acortarNombreArchivo = (nombreArchivo) => {
    if (!nombreArchivo) return "";
    // Si el nombre es muy largo, lo acortamos manteniendo la extensión
    const maxLength = 25;
    if (nombreArchivo.length <= maxLength) return nombreArchivo;
    
    const extension = nombreArchivo.substring(nombreArchivo.lastIndexOf('.'));
    const nombreSinExt = nombreArchivo.substring(0, nombreArchivo.lastIndexOf('.'));
    const nombreCorto = nombreSinExt.substring(0, maxLength - extension.length - 3) + '...';
    
    return nombreCorto + extension;
};

const nombreHora = (hora) => {
    const horas = {
        "1": "Primera",
        "2": "Segunda",
        "3": "Tercera",
        "4": "Cuarta",
        "5": "Quinta",
        "6": "Sexta",
        "7": "Séptima",
        "8": "Octava"
    };
    return horas[hora] || `Hora ${hora}`;
};

onMounted(() => {
    usuarioEmail.value = localStorage.getItem("userEmail");
});

const validarFecha = () => {
    if (!fecha.value) {
        errorFechaFinDeSemana.value = false;
        return;
    }
    
    // Corregir problema de zona horaria añadiendo 'T00:00:00' para forzar hora local
    const fechaLocal = new Date(fecha.value + 'T00:00:00');
    const dia = fechaLocal.getDay();
    
    // Validar que no sea sábado (6) ni domingo (0)
    if (dia === 0 || dia === 6) {
        errorFechaFinDeSemana.value = true;
        faltasPorHora.value = {};
        aulaConvivencia.value = {};
        toast.warning("No se pueden consultar ausencias en fin de semana.");
        return;
    }
    
    errorFechaFinDeSemana.value = false;
};

watch(fecha, (nuevaFecha) => {
    if (nuevaFecha) {
        validarFecha();
        if (!errorFechaFinDeSemana.value) {
            cargarFaltas();
        }
    }
});

const cargarFaltas = async () => {
    if (!fecha.value) {
        toast.error("Por favor, selecciona una fecha.");
        return;
    }

    try {
        const response = await axios.get(`http://localhost:8081/api/ausencias/fecha/${fecha.value}`);

        const ausenciasPorHora = response.data;
        const agrupadas = {};

        // El backend ya nos devuelve las ausencias agrupadas por hora
        for (const [hora, listaAusencias] of Object.entries(ausenciasPorHora)) {
            agrupadas[hora] = [];
            
            for (const ausencia of listaAusencias) {
                // Cada ausencia tiene un array de horas, tomamos la primera (solo hay una por la agrupación del backend)
                const horaData = ausencia.horas[0];
                
                // NOTA: Ya NO se persiste convivencia en BD, se calcula dinámicamente
                // Los nombres vienen directamente del backend, no hace falta llamarlos por separado
                
                const emailAusente = ausencia.profesorAusenteEmail?.toLowerCase() || "desconocido@dominio.com";
                const emailCubre = horaData.cobertura?.profesorCubreEmail?.toLowerCase() || null;
                
                agrupadas[hora].push({
                    id: ausencia.id,
                    profesorEmail: emailAusente,
                    // Usar el nombre que viene del backend
                    profesorNombre: ausencia.profesorAusenteNombre || emailAusente,
                    aula: horaData.aula,
                    grupo: horaData.grupo,
                    tarea: horaData.tarea,
                    profesorCubreEmail: emailCubre,
                    // Usar el nombre que viene del backend
                    profesorCubreNombre: horaData.cobertura?.profesorCubreNombre || emailCubre,
                    cobertura: horaData.cobertura !== null,
                    archivos: horaData.archivos || [],
                    hora
                });
            }
        }

        faltasPorHora.value = agrupadas;
        // Ya no usamos convivenciaPorHora de las ausencias
        
        // Calcular convivencia dinámicamente para TODAS las horas (1-8)
        // Esto funciona igual con o sin ausencias
        await calcularConvivenciaParaHorasVacias();
        
    } catch (error) {
        console.error("Error al obtener las faltas:", error);
        toast.error("Error al obtener las faltas.");
    }
};

/**
 * Calcula dinámicamente el profesor de convivencia para TODAS las horas lectivas (1-6).
 * NOTA: Las horas son: 1, 2, 3 (RECREO 11:15-11:45), 4, 5, 6. Termina a las 14:45.
 * Llama al backend para obtener el profesor con menor contador de convivencia,
 * excluyendo automáticamente a profesores que ya están cubriendo otras guardias.
 */
const calcularConvivenciaParaHorasVacias = async () => {
    const horasLectivas = [1, 2, 3, 4, 5, 6];
    
    for (const hora of horasLectivas) {
        // Calcular convivencia dinámicamente para TODAS las horas (con o sin ausencias)
        // NOTA: Frontend solo se comunica con backend guardias (8081), NO con horarios (8082)
        try {
            const response = await axios.get(`http://localhost:8081/api/coberturas/convivencia-calculada`, {
                params: {
                    fecha: fecha.value,
                    hora: hora
                }
            });
            
            if (response.data.success) {
                aulaConvivencia.value[hora] = {
                    email: response.data.profesorEmail?.toLowerCase(),
                    nombre: response.data.profesorNombre || response.data.profesorEmail
                };
                console.log(`✅ Convivencia hora ${hora}: ${response.data.profesorNombre || response.data.profesorEmail}`);
            }
        } catch (error) {
            // Si es fin de semana o no hay profesores de guardia, ignorar silenciosamente
            if (error.response?.status === 400 || error.response?.status === 404) {
                console.debug(`No se puede calcular convivencia para hora ${hora}: ${error.response?.data?.error}`);
            } else {
                console.error(`❌ Error calculando convivencia para hora ${hora}:`, error);
            }
        }
    }
};

const eliminarAusencia = async (id, hora, index) => {
    try {
        await axios.delete(`http://localhost:8081/api/ausencias/${id}`);
        toast.success("Falta eliminada correctamente.");
        faltasPorHora.value[hora].splice(index, 1);
        if (faltasPorHora.value[hora].length === 0) {
            delete faltasPorHora.value[hora];
        }
    } catch (error) {
        toast.error("No se pudo eliminar la falta.");
    }
};
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Montserrat:wght@300;400;600&display=swap');

* {
    font-family: 'Montserrat', sans-serif;
    box-sizing: border-box;
}

.tablon-faltas-container {
    max-width: 900px;
    margin: auto;
    padding: 10px;
    text-align: center;
}

label {
    display: block;
    margin-top: 15px;
    font-weight: 500;
    color: #333;
    text-align: left;
}

input[type="date"] {
    width: 100%;
    padding: 10px;
    margin-top: 8px;
    border: 2px solid #ccc;
    border-radius: 8px;
    font-size: 14px;
    transition: border-color 0.3s ease;
}

input[type="date"]:focus {
    border-color: #1F86A1;
    outline: none;
}

.error-fecha {
    color: #dc3545;
    font-size: 14px;
    font-weight: 500;
    margin-top: 8px;
    padding: 8px 12px;
    background-color: #f8d7da;
    border: 1px solid #f5c6cb;
    border-radius: 6px;
    text-align: left;
}

button {
    margin-top: 15px;
    padding: 10px 20px;
    background-color: #1F86A1;
    color: white;
    border: none;
    border-radius: 25px;
    font-size: 14px;
    font-weight: 500;
    cursor: pointer;
    transition: background-color 0.3s ease;
}

button:hover {
    background-color: #166b7a;
    transform: scale(1.02);
}

.hora-section {
    margin-top: 30px;
}

.hora-titulo {
    font-size: 1.4em;
    font-weight: 600;
    color: #1F86A1;
    text-align: left;
    margin: 20px 0 10px;
    border-left: 5px solid #1F86A1;
    padding-left: 10px;
}

.convivencia-banner {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 12px 16px;
    background: linear-gradient(135deg, #ffeaa7 0%, #fdcb6e 100%);
    border-left: 4px solid #e17055;
    border-radius: 8px;
    margin: 15px 0;
    box-shadow: 0 2px 8px rgba(225, 112, 85, 0.2);
    transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.convivencia-banner:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(225, 112, 85, 0.3);
}

.convivencia-icon {
    font-size: 1.8em;
    filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.1));
}

.convivencia-texto {
    font-size: 14px;
    color: #2d3436;
    font-weight: 500;
}

.convivencia-texto strong {
    color: #d63031;
    font-weight: 600;
}

.faltas-table {
    width: 100%;
    border-collapse: collapse;
    box-shadow: 0px 2px 6px rgba(0, 0, 0, 0.05);
    margin-bottom: 20px;
}

.faltas-table th,
.faltas-table td {
    padding: 12px 10px;
    font-size: 14px;
    text-align: left;
    border-bottom: 1px solid #eee;
}

.faltas-table th {
    background-color: #e8f6f9;
    color: #1F86A1;
    font-weight: 600;
}

.no-cubrir {
    color: red;
    font-weight: bold;
    font-size: 13px;
}

.tarea-celda {
    max-width: 250px;
}

.tarea-texto {
    display: block;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    font-size: 13px;
    color: #555;
}

.material-celda {
    min-width: 120px;
    max-width: 250px;
}

.material-archivos {
    display: flex;
    flex-direction: column;
    gap: 6px;
    align-items: flex-start;
}

.archivo-link {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    padding: 4px 8px;
    background-color: #f8f9fa;
    border: 1px solid #dee2e6;
    border-radius: 4px;
    color: #1F86A1;
    text-decoration: none;
    font-size: 12px;
    font-weight: 500;
    transition: all 0.2s ease;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    max-width: 100%;
}

.archivo-link:hover {
    background-color: #1F86A1;
    color: white;
    border-color: #1F86A1;
    transform: translateY(-1px);
    box-shadow: 0 2px 4px rgba(31, 134, 161, 0.2);
}

.sin-material {
    color: #999;
    font-style: italic;
    font-size: 13px;
}

p {
    margin-top: 20px;
    font-style: italic;
    color: #666;
    text-align: center;
}

@media (max-width: 768px) {
    .hora-titulo {
        font-size: 1.2em;
    }

    .convivencia-banner {
        flex-direction: column;
        align-items: flex-start;
        gap: 8px;
        padding: 10px 12px;
    }

    .convivencia-icon {
        font-size: 1.5em;
    }

    .convivencia-texto {
        font-size: 13px;
    }

    .faltas-table,
    thead,
    tbody,
    tr,
    td,
    th {
        display: block;
    }

    .faltas-table thead {
        display: none;
    }

    .faltas-table tr {
        margin-bottom: 15px;
        background: #fff;
        border: 1px solid #eee;
        border-radius: 10px;
        padding: 10px;
        box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
    }

    .faltas-table td {
        text-align: left;
        padding: 8px;
        position: relative;
        font-size: 14px;
    }

    .faltas-table td::before {
        content: attr(data-label);
        font-weight: 600;
        color: #1F86A1;
        display: block;
        margin-bottom: 4px;
    }

    .tarea-celda,
    .material-celda {
        max-width: 100%;
    }

    .tarea-texto {
        white-space: normal;
        word-wrap: break-word;
    }

    .archivo-link {
        width: 100%;
        justify-content: flex-start;
    }

    button,
    input,
    select {
        width: 100%;
        margin: 8px 0;
    }

    h2 {
        font-size: 1.5em;
    }
}
</style>
