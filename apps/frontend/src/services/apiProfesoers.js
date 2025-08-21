import axios from 'axios';
const API = import.meta.env.API_URL;

export const getProfesores = () => axios.get(`${API}/profesores`);
export const getProfesorById = (id) => axios.get(`${API}/profesores/${id}`);
export const getProfesorByEmail = (email) => axios.get(`${API}/profesores/email/${email}`);
