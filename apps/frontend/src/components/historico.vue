<template>
    <div class="historico-container">
        <label for="fecha">Selecciona una fecha:</label>
        <input type="date" v-model="fecha" required />
        <div class="input-group">
            <label for="filtroNombre">Filtra por nombre del profesor:</label>
            <input type="text" id="filtroNombre" list="lista-profesores" v-model="nombreProfesorSeleccionado"
                placeholder="Escribe o selecciona un profesor" class="styled-input" />
            <datalist id="lista-profesores">
                <option v-for="profesor in profesores" :key="profesor" :value="profesor" />
            </datalist>
        </div>


        <button :class="{ activo: botonSeleccionado === 'mostrarTodas' }"
            @click="botonSeleccionado = 'mostrarTodas'; mostrarTodas()">
            Mostrar Todas
        </button>

        <button :class="{ activo: botonSeleccionado === 'filtrar' }"
            @click="botonSeleccionado = 'filtrar'; obtenerEmailYFiltrar()">
            Filtrar
        </button>

        <table v-if="historico.length">
            <thead>
                <tr>
                    <th>Fecha</th>
                    <th>Hora</th>
                    <th>Aula</th>
                    <th>Grupo</th>
                    <th>Profesor Ausente</th>
                    <th>Tarea</th>
                    <th>Cubierta Por</th>
                </tr>
            </thead>
            <tbody>
                <tr v-for="(falta, index) in historico" :key="index">
                    <td data-label="Fecha">{{ falta.fecha }}</td>
                    <td data-label="Profesor Ausente">{{ emailAcortado(falta.profesorAusente) }}</td>
                    <td data-label="Hora">{{ formatearHora(falta.hora) }}</td>
                    <td data-label="Aula">{{ falta.aula }}</td>
                    <td data-label="Grupo">{{ falta.grupo }}</td>
                    <td data-label="Tarea">{{ falta.tarea || 'Sin tarea asignada' }}</td>
                    <td data-label="Cubierta Por">
                        <template v-if="falta.profesorCubre">
                            <span>{{ emailAcortado(falta.profesorCubre) }}</span>
                        </template>
                        <template v-else>
                            <select v-model="profesorSeleccionado[index]">
                                <option disabled value="">Selecciona un profesor</option>
                                <option v-for="profesor in profesoresPorAusencia[falta.id] || []" :key="profesor.email"
                                    :value="profesor.email">
                                    {{ profesor.email }}
                                </option>
                            </select>
                            <button @click="asignarCoberturaHandler(falta.id, index)" class="btn btn-primary">Asignar Profesor
                            </button>
                        </template>
                    </td>
                    <td>
                        <button v-if="falta.profesorCubre"
                            @click="eliminarCobertura(falta.id, index, obtenerDiaSemana(falta.fecha), falta.hora)"
                            class="btn btn-danger">
                            Sustituir Profesor de Guardia
                        </button>
                    </td>
                </tr>
            </tbody>
        </table>

        <p v-else>No hay registros en el historial.</p>
    </div>
</template>
<script setup>
import { ref, onMounted } from "vue";
import { 
  getProfesoresByNombre, 
  getProfesorEmailByNombre, 
  getAusenciasHistorico, 
  getGuardiaByDayAndHour,
  deleteCoberturaByAusencia,
  asignarCobertura
} from '@/services/apiService'
import { useToast } from "vue-toastification";

const toast = useToast();

// Estados
const emailProfesor = ref("");
const fecha = ref('');
const historico = ref([]);
const historicoOriginal = ref([]);
const profesorSeleccionado = ref([]);
const profesoresPorAusencia = ref({});
const botonSeleccionado = ref(null);

// Para filtrado por nombre
const nombreFiltro = ref("");
const nombreProfesorSeleccionado = ref("");
const profesores = ref([]); // Todos los nombres
const profesoresFiltrados = ref([]); // Filtrados por lo que escribe el usuario

// Cargar todos los nombres
const cargarTodosLosNombres = async () => {
    try {
        const response = await getProfesoresByNombre();
        profesores.value = response.data;
        profesoresFiltrados.value = response.data;
    } catch (error) {
        toast.error("No se pudieron cargar los nombres de los profesores.");
    }
};

// Filtrar dinámicamente las opciones del select
const filtrarOpciones = () => {
    const filtro = nombreFiltro.value.toLowerCase();
    profesoresFiltrados.value = profesores.value.filter(nombre =>
        nombre.toLowerCase().includes(filtro)
    );
};

// Obtener email desde nombre y luego filtrar
const obtenerEmailYFiltrar = async () => {
    if (!nombreProfesorSeleccionado.value && !fecha.value) {
        toast.error("Introduce al menos un filtro (nombre o fecha).");
        return;
    }

    try {
        if (nombreProfesorSeleccionado.value) {
            const response = await getProfesorEmailByNombre(nombreProfesorSeleccionado.value);
            emailProfesor.value = response.data;
        } else {
            emailProfesor.value = "";
        }

        filtrarPorProfesor();
    } catch (error) {
        toast.error("No se pudo obtener el email del profesor seleccionado.");
    }
};

// Filtrar ausencias por email y/o fecha
const filtrarPorProfesor = () => {
    historico.value = historicoOriginal.value.filter((falta) => {
        const emailCoincide = emailProfesor.value
            ? falta.profesorAusente.toLowerCase().includes(emailProfesor.value.toLowerCase())
            : true;

        const fechaCoincide = fecha.value
            ? falta.fecha === fecha.value
            : true;

        return emailCoincide && fechaCoincide;
    });

    if (historico.value.length === 0) {
        toast.info("No se encontraron coincidencias con los filtros seleccionados.");
    }
};

// Cargar todas las ausencias históricas
const mostrarTodas = async () => {
    try {
        const response = await getAusenciasHistorico();
        const datos = [];

        for (const [fecha, horas] of Object.entries(response.data)) {
            for (const [hora, ausencias] of Object.entries(horas)) {
                for (const ausencia of ausencias) {
                    datos.push({
                        id: ausencia.id,
                        fecha,
                        hora,
                        aula: ausencia.aula,
                        grupo: ausencia.grupo,
                        profesorAusente: ausencia.profesorAusenteEmail,
                        tarea: ausencia.tarea || "Sin tarea asignada",
                        profesorCubre: ausencia.profesorEnGuardiaEmail || null,
                    });
                }
            }
        }

        historicoOriginal.value = datos;
        historico.value = [...datos];
    } catch (error) {
        toast.error("No se pudieron cargar todas las ausencias.");
    }
};

// Asignar cobertura
const asignarCoberturaHandler = async (ausenciaId, index) => {
    const profesorEmail = profesorSeleccionado.value[index];
    if (!profesorEmail) {
        toast.error("Por favor, selecciona un profesor para la cobertura.");
        return;
    }

    try {
        await asignarCobertura(ausenciaId, profesorEmail);
        toast.success("Profesor de guardia asignado exitosamente.");
        await mostrarTodas();
    } catch (error) {
        toast.error("No se pudo asignar la cobertura.");
    }
};

// Eliminar cobertura
const eliminarCobertura = async (ausenciaId, index, diaSemana, hora) => {
    if (!ausenciaId) {
        toast.error("Error: ID de ausencia no válido.");
        return;
    }

    try {
        await deleteCoberturaByAusencia(ausenciaId);
        historico.value[index].profesorCubre = null;
        profesorSeleccionado.value[index] = '';

        const response = await getGuardiaByDayAndHour(diaSemana, hora);
        profesoresPorAusencia.value[ausenciaId] = response.data.profesores;

        toast.success("Profesor que cubre eliminado. Ahora puedes asignar otro.");
    } catch (error) {
        toast.error("No se pudo eliminar la cobertura.");
    }
};

// Formatear hora y otros utilitarios
const obtenerDiaSemana = (fechaStr) => {
    const date = new Date(fechaStr);
    const dia = date.getDay();
    return dia === 0 ? 7 : dia; // Domingo = 7
};

const formatearHora = (hora) => {
    const horasTexto = [
        "Primera", "Segunda", "Tercera", "Cuarta",
        "Quinta", "Sexta", "Séptima", "Octava"
    ];
    return horasTexto[hora - 1] || `Hora ${hora}`;
};

const emailAcortado = (email) => {
    const indiceArroba = email.indexOf('@');
    return indiceArroba === -1
        ? email.length > 10 ? email.slice(0, 10) + '...' : email
        : email.slice(0, indiceArroba);
};

onMounted(() => {
    mostrarTodas();
    cargarTodosLosNombres();
});
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Montserrat:wght@300;500;600&display=swap');

* {
    font-family: 'Montserrat', sans-serif;
    box-sizing: border-box;
}

.historico-container {
    max-width: 1000px;
    margin: 10px auto 0 auto;
    padding: 10px;
}


label {
    display: block;
    font-weight: 500;
    margin: 10px 0 5px;
    color: #333;
}

input[type="date"],
input[type="email"],
select {
    width: 100%;
    padding: 12px 14px;
    border: 2px solid #ccc;
    border-radius: 12px;
    font-size: 14px;
    transition: border 0.3s, box-shadow 0.3s;
    background-color: #fafafa;
}

input:focus,
select:focus {
    border-color: #1F86A1;
    box-shadow: 0 0 0 3px rgba(31, 134, 161, 0.15);
    outline: none;
}

button {
    padding: 12px 20px;
    margin-top: 15px;
    margin-right: 10px;
    border: none;
    border-radius: 30px;
    background-color: #1F86A1;
    color: #fff;
    font-weight: 500;
    font-size: 14px;
    cursor: pointer;
    transition: background-color 0.3s, transform 0.2s;
}

button:hover,
button.activo {
    background-color: #166b7a;
    transform: scale(1.02);
}

button {
    transition: background-color 0.3s ease, transform 0.3s ease;
}


table {
    width: 100%;
    border-collapse: collapse;
    margin-top: 25px;
    border-radius: 10px;
    overflow: hidden;
    box-shadow: 0 2px 6px rgba(0, 0, 0, 0.04);
}

th,
td {
    padding: 14px 12px;
    text-align: left;
    border-bottom: 1px solid #eaeaea;
    font-size: 13.5px;
}

th {
    background-color: #e8f6f9;
    color: #1F86A1;
    font-weight: 600;
}

tr:hover {
    background-color: #f5fafd;
}

.btn-danger {
    background-color: #dc3545;
    border-radius: 30px;
    font-size: 13px;
    padding: 10px 14px;
    color: #fff;
    margin-top: 6px;
}

.btn-danger:hover {
    background-color: #c82333;
}

p {
    margin-top: 20px;
    font-style: italic;
    color: #666;
    text-align: center;
}
.input-group {
    display: flex;
    flex-direction: column;
    gap: 6px;
    margin-bottom: 16px;
}

input.styled-input {
    padding: 12px 16px;
    border: 2px solid #ccc;
    border-radius: 12px;
    font-size: 14px;
    background-color: #fafafa;
    transition: border-color 0.3s ease, box-shadow 0.3s ease;
}

input.styled-input:focus {
    border-color: #1F86A1;
    box-shadow: 0 0 0 3px rgba(31, 134, 161, 0.15);
    outline: none;
}


/* ✅ Responsive Design */
@media (max-width: 768px) {

    table,
    thead,
    tbody,
    th,
    td,
    tr {
        display: block;
    }

    thead {
        display: none;
    }

    tr {
        background: #fff;
        margin-bottom: 15px;
        padding: 12px;
        border: 1px solid #eee;
        border-radius: 12px;
        box-shadow: 0 2px 6px rgba(0, 0, 0, 0.03);
    }

    td {
        padding: 10px 0;
        text-align: left;
        position: relative;
        font-size: 14px;
    }

    td::before {
        content: attr(data-label);
        font-weight: 600;
        color: #1F86A1;
        display: block;
        margin-bottom: 4px;
    }

    button,
    input,
    select {
        width: 100%;
        margin: 8px 0;
    }

    .btn-danger {
        margin-top: 10px;
    }
}
</style>