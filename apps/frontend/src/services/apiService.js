import axios from 'axios';

// URLs de las APIs (directas, sin variables de entorno)
const API_GUARDIAS = 'http://localhost:8081';
const API_HORARIOS = 'http://localhost:8082';

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

// Archivos de ausencias
export const subirArchivoAusencia = (formData) => guardiasAPI.post('/api/archivos/upload', formData, {
  headers: { 'Content-Type': 'multipart/form-data' }
});
export const getArchivosByAusencia = (ausenciaId) => guardiasAPI.get(`/api/archivos/ausencia/${ausenciaId}`);
export const downloadArchivo = (archivoId) => guardiasAPI.get(`/api/archivos/download/${archivoId}`, {
  responseType: 'blob'
});

// Estadísticas y dashboards
export const getDashboardEquidad = () => guardiasAPI.get('/api/estadisticas/dashboard-equidad');
export const getReporteDesbalance = () => guardiasAPI.get('/api/estadisticas/reporte-desbalance');
export const getContadoresProfesor = (profesorEmail) => guardiasAPI.get(`/api/estadisticas/profesor/${profesorEmail}/contadores`);
export const getContadoresPorDiaHora = (dia, hora) => {
  let url = '/api/estadisticas/contadores';
  if (dia && hora) {
    url += `/${dia}/${hora}`;
  } else if (dia) {
    url += `?dia=${dia}`;
  } else if (hora) {
    url += `?hora=${hora}`;
  }
  return guardiasAPI.get(url);
};

// ===============================
// API HORARIOS (Puerto 8082) - NUEVAS FUNCIONES
// ===============================

// Profesores con guardias detalladas
export const getProfesoresConGuardias = () => horariosAPI.get('/profesores/guardias');
export const getProfesorGuardiasDetalle = (email) => horariosAPI.get(`/profesores/${email}/guardias-detalle`);

// Actualización de contadores
export const actualizarContadoresProfesor = (email, contadorData) => horariosAPI.put(`/profesores/${email}/contadores`, contadorData);
export const actualizarContadoresLote = (contadoresData) => horariosAPI.post('/profesores/contadores/batch', contadoresData);

// Contadores específicos
export const getContadorEspecifico = (email, dia, hora) => horariosAPI.get(`/profesores/${email}/contadores/${dia}/${hora}`);
export const resetearContadoresProfesor = (email) => horariosAPI.delete(`/profesores/${email}/contadores`);

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
// 🔄 INTEGRACIÓN AVANZADA Y SINCRONIZACIÓN
// ===============================

// === IMPORTACIÓN DE XML HORARIOS ===
export const importarXmlHorarios = (archivo) => {
  const formData = new FormData();
  formData.append('archivo', archivo);
  return horariosAPI.post('/horarios/importar-xml', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  });
};

// === SINCRONIZACIÓN BIDIRECCIONAL ===
export const sincronizarGuardiasConHorarios = () => guardiasAPI.post('/api/sincronizacion/sincronizar-horarios');
export const verificarCoherenciaHorarios = () => guardiasAPI.get('/api/sincronizacion/verificar-coherencia');
export const aplicarCambiosHorarios = (cambios) => guardiasAPI.post('/api/sincronizacion/aplicar-cambios', cambios);
export const getEstadoSincronizacion = () => guardiasAPI.get('/api/sincronizacion/estado');

// === MONITOREO EN TIEMPO REAL ===
export const getMonitoreoTiempoReal = () => guardiasAPI.get('/api/monitoreo/tiempo-real');
export const actualizarEstadoProfesor = (email, estado) => guardiasAPI.post(`/api/profesores/${email}/estado`, { estado });
export const getNotificacionesPendientes = () => guardiasAPI.get('/api/notificaciones/pendientes');
export const marcarNotificacionLeida = (id) => guardiasAPI.post(`/api/notificaciones/${id}/leida`);

// === CONTADORES DINÁMICOS ===
export const recalcularTodosContadores = () => guardiasAPI.post('/api/contadores/recalcular-todos');
export const getContadoresPorDia = (dia) => guardiasAPI.get(`/api/contadores/dia/${dia}`);
export const getContadoresPorHora = (hora) => guardiasAPI.get(`/api/contadores/hora/${hora}`);

// === VALIDACIÓN Y COHERENCIA ===
export const validarAsignacionCoherencia = (ausenciaId, profesorEmail) => 
  guardiasAPI.post('/api/validacion/asignacion-coherencia', { ausenciaId, profesorEmail });
export const getConflictosHorarios = () => guardiasAPI.get('/api/validacion/conflictos-horarios');
export const resolverConflictoHorario = (conflictoId, solucion) => 
  guardiasAPI.post(`/api/validacion/conflictos/${conflictoId}/resolver`, solucion);

// === REPORTES AVANZADOS ===
export const getReporteCobertura = (fechaInicio, fechaFin) => 
  guardiasAPI.get('/api/reportes/cobertura', { params: { fechaInicio, fechaFin } });
export const getReporteDistribucion = (mes) => 
  guardiasAPI.get('/api/reportes/distribucion', { params: { mes } });
export const getReporteRendimiento = () => guardiasAPI.get('/api/reportes/rendimiento');
export const exportarReporte = (tipoReporte, formato) => 
  guardiasAPI.get(`/api/reportes/${tipoReporte}/exportar/${formato}`, { responseType: 'blob' });

// === GESTIÓN AVANZADA DE COBERTURAS ===
export const getCoberturas = (filtros = {}) => guardiasAPI.get('/api/coberturas', { params: filtros });
export const actualizarEstadoCobertura = (id, estado) => 
  guardiasAPI.put(`/api/coberturas/${id}/estado`, { estado });
export const redistribuirCoberturasDia = (fecha) => 
  guardiasAPI.post('/api/coberturas/redistribuir-dia', { fecha });

// === NUEVAS FUNCIONES ESTADÍSTICAS ===
export const getProfesorMasGuardias = () => guardiasAPI.get('/api/estadisticas/profesor-mas-guardias');
export const getProfesorMenosGuardias = () => guardiasAPI.get('/api/estadisticas/profesor-menos-guardias');
export const getGuardiasPorDia = () => guardiasAPI.get('/api/estadisticas/guardias-por-dia');
export const getGuardiasPorHora = () => guardiasAPI.get('/api/estadisticas/guardias-por-hora');
export const getDiaMasGuardias = () => guardiasAPI.get('/api/estadisticas/dia-mas-guardias');
export const getHoraMasGuardias = () => guardiasAPI.get('/api/estadisticas/hora-mas-guardias');
export const getProblemasSistema = () => guardiasAPI.get('/api/estadisticas/problemas-sistema');
export const getMensajeAlerta = () => guardiasAPI.get('/api/estadisticas/mensaje-alerta');
export const getMetricasGenerales = () => guardiasAPI.get('/api/estadisticas/metricas-generales');
export const getDistribucionTipoGuardias = () => guardiasAPI.get('/api/estadisticas/tipos-guardias');
export const getEvolucionMensual = () => guardiasAPI.get('/api/estadisticas/evolucion-mensual');

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
