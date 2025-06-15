import axios from 'axios';
const API = import.meta.env.API_URL;

export const getAulas = () => axios.get(`${API}/aulas`);
