import axios from 'axios';

// 🎯 CONFIGURACIÓN UNIFICADA - Solo una API
const API = import.meta.env.VITE_API_URL || 'http://localhost:8081';

// ============================================================================
// 📊 ENDPOINTS DE HORARIOS (ahora proxy desde backend principal)
// ============================================================================

export const getActividades = () => axios.get(`${API}/api/horarios/actividades`);
export const getAsignaturas = () => axios.get(`${API}/api/horarios/asignaturas`);
export const getAulas = () => axios.get(`${API}/api/horarios/aulas`);
export const getGrupos = () => axios.get(`${API}/api/horarios/grupos`);
export const getHorarioByEmailProfesor = (email) => axios.get(`${API}/api/horarios/horario/profesor/email?email=${encodeURIComponent(email)}`);
export const getTramosHorario = () => axios.get(`${API}/api/horarios/tramohorarios`);

// Nuevos endpoints específicos
export const getHorarioProfesorPorDia = (id, diaSemana) => 
    axios.get(`${API}/api/horarios/horario/profesor/${id}/dia/${diaSemana}`);

export const getGuardiasPorDiaYHora = (diaSemana, hora) => 
    axios.get(`${API}/api/horarios/horario/guardia/dia/${diaSemana}/hora/${hora}`);

export const getProfesorPorEmail = (email) => 
    axios.get(`${API}/api/horarios/horario/profesor/email?email=${encodeURIComponent(email)}`);

// ============================================================================
// 📝 ENDPOINTS DE AUSENCIAS (backend principal)
// ============================================================================

export const createAusenciaMultiple = (ausenciaMultipleDTO) => 
    axios.post(`${API}/api/ausencias/multiple`, ausenciaMultipleDTO);

export const crearAusencia = (ausenciaData) => {
    // Detectar si es FormData (con archivos) o JSON simple
    const headers = ausenciaData instanceof FormData 
        ? { 'Content-Type': 'multipart/form-data' }
        : { 'Content-Type': 'application/json' };
    
    return axios.post(`${API}/api/ausencias`, ausenciaData, { headers });
};

// ============================================================================
// 🔍 ENDPOINTS DE SALUD Y MONITOREO
// ============================================================================

export const checkBackendHealth = () => axios.get(`${API}/health`);
export const checkHorariosHealth = () => axios.get(`${API}/api/horarios/health`);