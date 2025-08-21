import axios from 'axios';
const API = import.meta.env.API_URL;

export const getAsignaturas = () => axios.get(`${API}/asignaturas`);
