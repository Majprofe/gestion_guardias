import axios from 'axios';

// URLs de las APIs desde variables de entorno
const API_GUARDIAS = import.meta.env.VITE_API_URL;
const API_HORARIOS = import.meta.env.VITE_PLATFORM_URL;

// Configuración base de axios
const guardiasAPI = axios.create({
  baseURL: API_GUARDIAS,
  timeout: 10000,
});

const horariosAPI = axios.create({
  baseURL: API_HORARIOS,
  timeout: 10000,
});

// ===============================
// API GUARDIAS (Puerto 8081)
// ===============================

// Actividades
export const getActividades = () => guardiasAPI.get('/api/actividades');

// Asignaturas
export const getAsignaturas = () => guardiasAPI.get('/api/asignaturas');

// Aulas
export const getAulas = () => guardiasAPI.get('/api/aulas');

// Grupos
export const getGrupos = () => guardiasAPI.get('/api/grupos');

// Profesores
export const getProfesores = () => guardiasAPI.get('/api/profesores');
export const getProfesorById = (id) => guardiasAPI.get(`/api/profesores/${id}`);
export const getProfesorByEmail = (email) => guardiasAPI.get(`/api/profesores/email/${email}`);

// Ausencias
export const getAusenciasByProfesor = (email) => guardiasAPI.get(`/api/ausencias/por-profesor/${encodeURIComponent(email)}`);
export const getAusenciasByFecha = (fecha) => guardiasAPI.get('/api/ausencias/por-fecha', { params: { fecha } });
export const getAusenciasHistorico = () => guardiasAPI.get('/api/ausencias/historico');
export const createAusencia = (ausenciaDTO) => guardiasAPI.post('/api/ausencias', ausenciaDTO);
export const deleteAusencia = (id) => guardiasAPI.delete(`/api/ausencias/${id}`);

// Coberturas
export const getCoberturasByProfesor = (email) => guardiasAPI.get(`/api/coberturas/profesor/${encodeURIComponent(email)}`);
export const getCoberturaByAusencia = (ausenciaId) => guardiasAPI.get(`/api/coberturas/ausencia/${ausenciaId}`);
export const getCobertura = (ausenciaId, fecha) => guardiasAPI.get(`/api/coberturas/ausencia/${ausenciaId}/fecha/${fecha}`);
export const deleteCoberturaByAusencia = (ausenciaId) => guardiasAPI.delete(`/api/coberturas/ausencia/${ausenciaId}`);
export const asignarCobertura = (ausenciaId, profesorEmail) => guardiasAPI.post(`/api/coberturas/asignarCobertura/${ausenciaId}/${profesorEmail}`);

// ===============================
// API HORARIOS (Puerto 8082)
// ===============================

// Horarios
export const getHorarioByIdProfesor = (id) => horariosAPI.get(`/horario/${id}`);
export const getHorarioByProfesorEmail = (email) => horariosAPI.get(`/horario/profesor/email`, { params: { email } });
export const getHorarioProfesorByDay = (id, diaSemana) => horariosAPI.get(`/horario/profesor/${id}/dia/${diaSemana}`);
export const getGuardiaByDayAndHour = (diaSemana, hora) => horariosAPI.get(`/horario/guardia/dia/${diaSemana}/hora/${hora}`);

// Profesores (Horarios Backend)
export const getProfesoresByNombre = (nombreParcial = '') => horariosAPI.get('/profesores/buscar-nombres', { params: { nombreParcial } });
export const getProfesorEmailByNombre = (nombre) => horariosAPI.get('/profesores/email-by-nombre', { params: { nombre } });
export const getProfesorByEmailHorarios = (email) => horariosAPI.get(`/profesores/email/${email}`);
export const activarProfesor = (id) => horariosAPI.post(`/profesores/activar/${id}`);
export const desactivarProfesor = (id) => horariosAPI.post(`/profesores/desactivar/${id}`);
export const incrementarGuardiaNormal = (id) => horariosAPI.post(`/profesores/incrementar-guardia-normal/${id}`);
export const incrementarGuardiaProblematica = (id) => horariosAPI.post(`/profesores/incrementar-guardia-problematica/${id}`);

// Tramos Horarios
export const getTramosHorario = () => horariosAPI.get('/tramohorarios');

// ===============================
// INTERCEPTORS PARA MANEJO DE ERRORES
// ===============================

// Interceptor para API de Guardias
guardiasAPI.interceptors.response.use(
  (response) => response,
  (error) => {
    console.error('Error en API Guardias:', error);
    if (error.response?.status === 401) {
      // Manejar error de autenticación si es necesario
      console.warn('No autorizado en API Guardias');
    }
    return Promise.reject(error);
  }
);

// Interceptor para API de Horarios
horariosAPI.interceptors.response.use(
  (response) => response,
  (error) => {
    console.error('Error en API Horarios:', error);
    if (error.response?.status === 401) {
      // Manejar error de autenticación si es necesario
      console.warn('No autorizado en API Horarios');
    }
    return Promise.reject(error);
  }
);

// ===============================
// EXPORTACIONES POR CATEGORÍA
// ===============================

export const guardiasService = {
  getActividades,
  getAsignaturas,
  getAulas,
  getGrupos,
  getProfesores,
  getProfesorById,
  getProfesorByEmail,
  getAusenciasByProfesor,
  getAusenciasByFecha,
  getAusenciasHistorico,
  createAusencia,
  deleteAusencia,
  getCoberturasByProfesor,
  getCoberturaByAusencia,
  getCobertura,
  deleteCoberturaByAusencia,
  asignarCobertura,
};

export const horariosService = {
  getHorarioByIdProfesor,
  getHorarioByProfesorEmail,
  getHorarioProfesorByDay,
  getGuardiaByDayAndHour,
  getProfesoresByNombre,
  getProfesorEmailByNombre,
  getProfesorByEmailHorarios,
  activarProfesor,
  desactivarProfesor,
  incrementarGuardiaNormal,
  incrementarGuardiaProblematica,
  getTramosHorario,
};

// Exportación por defecto con todo organizado
export default {
  guardias: guardiasService,
  horarios: horariosService,
};
