import axios from 'axios';
const API = import.meta.env.API_URL;

export const getGrupos = () => axios.get(`${API}/grupos`);
