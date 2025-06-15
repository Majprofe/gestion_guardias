<template>
    <div class="tablon-faltas-container">
        <label for="fecha">Selecciona una fecha:</label>
        <input type="date" v-model="fecha" :min="fechaHoy" required />

        <div v-if="Object.keys(faltasPorHora).length">
            <div v-for="(faltasHora, hora) in faltasPorHora" :key="hora" class="hora-section">
                <h3 class="hora-titulo">{{ nombreHora(hora) }}</h3>
                <table class="faltas-table">
                    <thead>
                        <tr>
                            <th>Profesor Ausente</th>
                            <th>Aula - Grupo</th>
                            <th>Profesor que Cubre</th>
                            <th class="acciones-columna" v-if="mostrarAcciones(faltasHora)">Acciones</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr v-for="(falta, index) in faltasHora" :key="index">
                            <td :data-label="'Profesor Ausente'">{{ emailAcortado(falta.profesorEmail) }}</td>
                            <td :data-label="'Aula - Grupo'">{{ falta.aula }} - {{ falta.grupo }}</td>
                            <td :data-label="'Profesor que Cubre'">
                                <span v-if="falta.cobertura">
                                    {{ emailAcortado(falta.profesorCubreEmail) }}
                                </span>
                                <span v-else class="no-cubrir">No cubierta</span>
                            </td>
                            <td v-if="falta.profesorEmail === usuarioEmail && !esFechaActual" class="acciones-celda">
                                <button @click="eliminarAusencia(falta.id, hora, index)" class="btn-eliminar-falta">
                                    🗑️ Eliminar falta
                                </button>
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
const usuarioEmail = ref(null);

const fechaHoy = new Date().toISOString().split("T")[0];
const esFechaActual = computed(() => fecha.value === fechaHoy);

const emailAcortado = (email) => {
    if (!email || typeof email !== 'string') return "No disponible";
    const indiceArroba = email.indexOf('@');
    return indiceArroba === -1 ? email.slice(0, 10) + '...' : email.slice(0, indiceArroba);
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

const mostrarAcciones = (faltas) => {
    return faltas.some(falta => falta.profesorEmail === usuarioEmail.value);
};

onMounted(() => {
    usuarioEmail.value = localStorage.getItem("userEmail");
});

watch(fecha, (nuevaFecha) => {
    if (nuevaFecha) {
        cargarFaltas();
    }
});

const cargarFaltas = async () => {
    if (!fecha.value) {
        toast.error("Por favor, selecciona una fecha.");
        return;
    }

    try {
        const response = await axios.get("http://localhost:8081/api/ausencias/por-fecha", {
            params: { fecha: fecha.value }
        });

        const ausencias = response.data;
        const agrupadas = {};

        const faltasArray = await Promise.all(
            Object.entries(ausencias).flatMap(([hora, listaAusencias]) =>
                listaAusencias.map(async (ausencia) => {
                    if (!ausencia.id) return null;

                    let profesorEmail = ausencia.profesorAusenteEmail?.toLowerCase() || "desconocido@dominio.com";
                    let profesorCubreEmail = null;
                    let cobertura = false;

                    try {
                        const coberturaResponse = await axios.get(
                            `http://localhost:8081/api/coberturas/ausencia/${ausencia.id}`
                        );

                        if (coberturaResponse.data?.profesorCubreEmail) {
                            profesorCubreEmail = coberturaResponse.data.profesorCubreEmail.toLowerCase();
                            cobertura = true;
                        }
                    } catch { }

                    const falta = {
                        id: ausencia.id,
                        profesorEmail,
                        aula: ausencia.aula,
                        grupo: ausencia.grupo,
                        profesorCubreEmail,
                        cobertura,
                        hora
                    };

                    if (!agrupadas[hora]) agrupadas[hora] = [];
                    agrupadas[hora].push(falta);
                    return falta;
                })
            )
        );

        faltasPorHora.value = agrupadas;
    } catch (error) {
        toast.error("Error al obtener las faltas.");
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

.acciones-columna {
    width: 40px;
    text-align: center;
}

.acciones-celda {
    text-align: center;
}

.btn-eliminar-falta {
    background-color: #dc3545;
    color: white;
    border: none;
    padding: 6px 12px;
    font-size: 13px;
    border-radius: 20px;
    font-weight: 500;
    cursor: pointer;
    transition: background-color 0.3s ease, transform 0.2s ease;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    gap: 6px;
    white-space: nowrap;
    width: auto;
}

.btn-eliminar-falta:hover {
    background-color: #c82333;
    transform: scale(1.03);
}

.no-cubrir {
    color: red;
    font-weight: bold;
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

    .acciones-columna,
    .acciones-celda {
        text-align: left;
    }

    .btn-eliminar-pequeno {
        margin-top: 8px;
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
