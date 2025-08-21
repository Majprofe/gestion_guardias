import axios from 'axios';
const API = import.meta.env.API_URL;

export const getHorarioByIdProfesor = (id) => axios.get(`${API}/horario/${id}`);

