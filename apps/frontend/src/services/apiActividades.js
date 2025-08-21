import axios from 'axios';
const API = import.meta.env.API_URL;

export const getActividades = () => axios.get(`${API}/actividades`);
