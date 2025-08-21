// stores/user.js
import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { supabase } from '@/supabaseClient';
import { profesoresAPI } from '@/services/supabaseProfesores';

export const useUserStore = defineStore('user', () => {
    const usuario = ref(null);
    const nombreUsuario = ref('');
    const isAdmin = ref(false);
    const isAuthenticated = ref(false);
    const loading = ref(false);

    // Getters computados
    const getUserEmail = computed(() => usuario.value?.email || '');
    const getUserName = computed(() => nombreUsuario.value || usuario.value?.email || '');
    const getIsAdmin = computed(() => isAdmin.value);

    const initializeAuth = async () => {
        loading.value = true;
        try {
            // Obtener sesión actual
            const { data: { session } } = await supabase.auth.getSession();
            
            if (session?.user) {
                await setUser(session.user);
            }

            // Escuchar cambios en la autenticación
            supabase.auth.onAuthStateChange(async (event, session) => {
                if (event === 'SIGNED_IN' && session?.user) {
                    await setUser(session.user);
                } else if (event === 'SIGNED_OUT') {
                    clearUser();
                }
            });
        } catch (error) {
            console.error('Error inicializando autenticación:', error);
        } finally {
            loading.value = false;
        }
    };

    const setUser = async (user) => {
        usuario.value = user;
        isAuthenticated.value = true;
        
        try {
            // Obtener información adicional del profesor desde la base de datos
            const profesor = await profesoresAPI.getProfesorPorEmail(user.email);
            
            if (profesor) {
                nombreUsuario.value = profesor.nombre;
                isAdmin.value = profesor.es_admin;
            } else {
                // Si no existe el profesor, crearlo con datos básicos
                const nuevoProfesor = {
                    email: user.email,
                    nombre: user.user_metadata?.full_name || user.email,
                    es_admin: false,
                    activo: true
                };
                
                const profesorCreado = await profesoresAPI.upsertProfesor(nuevoProfesor);
                nombreUsuario.value = profesorCreado.nombre;
                isAdmin.value = profesorCreado.es_admin;
            }
        } catch (error) {
            console.error('Error obteniendo datos del profesor:', error);
            nombreUsuario.value = user.email;
            isAdmin.value = false;
        }
    };

    const clearUser = () => {
        usuario.value = null;
        nombreUsuario.value = '';
        isAdmin.value = false;
        isAuthenticated.value = false;
    };

    const login = async (email, password) => {
        loading.value = true;
        try {
            const { data, error } = await supabase.auth.signInWithPassword({
                email,
                password
            });

            if (error) throw error;

            await setUser(data.user);
            return { success: true, user: data.user };
        } catch (error) {
            console.error('Error en login:', error);
            return { success: false, error: error.message };
        } finally {
            loading.value = false;
        }
    };

    const logout = async () => {
        try {
            const { error } = await supabase.auth.signOut();
            if (error) throw error;
            
            clearUser();
            return { success: true };
        } catch (error) {
            console.error('Error en logout:', error);
            return { success: false, error: error.message };
        }
    };

    const resetUser = () => {
        clearUser();
    };

    return { 
        // Estado
        usuario,
        nombreUsuario, 
        isAdmin, 
        isAuthenticated,
        loading,
        // Getters
        getUserEmail,
        getUserName,
        getIsAdmin,
        // Actions
        initializeAuth,
        setUser, 
        clearUser,
        login,
        logout,
        resetUser 
    };
});
