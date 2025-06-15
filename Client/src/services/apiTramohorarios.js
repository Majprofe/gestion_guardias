import axios from 'axios';
const API = import.meta.env.API_URL;

export const getTramosHorario = () => axios.get(`${API}/tramohorarios`);