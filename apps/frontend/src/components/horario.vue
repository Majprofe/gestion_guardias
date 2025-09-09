<template>
    <div class="mi-horario">
        <div v-if="Object.keys(horarioTransformado).length">
            <table class="horario-clasico">
                <thead>
                    <tr>
                        <th>Hora</th>
                        <th v-for="dia in ['lunes', 'martes', 'miércoles', 'jueves', 'viernes']" :key="dia">
                            {{ dia.charAt(0).toUpperCase() + dia.slice(1) }}
                        </th>
                    </tr>
                </thead>
                <tbody>
                    <tr v-for="hora in Object.keys(horarioTransformado).sort((a, b) => a - b)" :key="hora">
                        <td>{{ hora }}</td>
                        <td v-for="dia in ['lunes', 'martes', 'miércoles', 'jueves', 'viernes']" :key="dia">
                            <div v-if="horarioTransformado[hora][dia]">
                                {{ horarioTransformado[hora][dia].asignatura }}<br />
                                {{ horarioTransformado[hora][dia].grupo || '—' }}<br />
                                {{ horarioTransformado[hora][dia].abreviaturaAula || '—' }}
                            </div>
                            <div v-else>—</div>
                        </td>
                    </tr>
                </tbody>
            </table>
        </div>

        <p v-else>Cargando horario...</p>
    </div>
</template>


<script setup>
import axios from 'axios'
import { ref, watch } from 'vue'
import { useUserStore } from "@/stores/user"
import { useToast } from "vue-toastification"

const toast = useToast()

const userStore = useUserStore()
const horario = ref(null)
const profesorNombre = ref('')
const horarioTransformado = ref({})

watch(
    () => userStore.nombreUsuario,
    async (email) => {
        if (email && email !== "Sin usuario") {
            try {
                const response = await axios.get(`http://localhost:8080/horario/profesor/email?email=${email}`)
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

    const diasSemana = ['lunes', 'martes', 'miércoles', 'jueves', 'viernes']
    for (const dia of diasSemana) {
        const actividades = horario.value[dia] || []
        for (const actividad of actividades) {
            const hora = actividad.hora
            if (!nuevoHorario[hora]) {
                nuevoHorario[hora] = {}
            }
            nuevoHorario[hora][dia] = actividad
        }
    }

    horarioTransformado.value = nuevoHorario
}
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Montserrat:wght@400;600&display=swap');

* {
    box-sizing: border-box;
    font-family: 'Montserrat', sans-serif;
}

.mi-horario {
    max-width: 1000px;
    margin: 0 auto;
    padding: 1rem;
    box-sizing: border-box;
    font-family: Arial, sans-serif;
    color: #333;
}

.horario-clasico {
    width: 100%;
    border-collapse: collapse;
    border-radius: 8px;
    overflow: hidden;
    box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
    background-color: #fff;
}

.horario-clasico th {
    background-color: #1F86A1;
    color: white;
    padding: 12px;
    font-size: 14px;
    text-transform: capitalize;
    text-align: center;
}

.horario-clasico td {
    border: 1px solid #e0e0e0;
    padding: 10px;
    font-size: 13px;
    text-align: center;
    vertical-align: middle;
    background-color: #f9f9f9;
}

.horario-clasico td strong {
    color: #1F86A1;
    font-weight: bold;
}

.horario-clasico td div {
    line-height: 1.4;
}

.horario-clasico td div:empty::before {
    content: "—";
    color: #bbb;
}

p {
    text-align: center;
    font-style: italic;
    color: #666;
}


@media (max-width: 768px) {

    .horario-clasico th,
    .horario-clasico td {
        padding: 8px;
        font-size: 10px;
    }

    h2 {
        font-size: 18px;
    }
}

@media (max-width: 480px) {
    .horario-clasico {
        font-size: 11px;
    }

    .horario-clasico th,
    .horario-clasico td {
        padding: 6px;
    }

    h2 {
        font-size: 16px;
    }
}
</style>
