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

    // 🎛️ Configuración de autenticación dual (valores fijos para desarrollo)
    const authMode = 'google'; // Modo fijo para desarrollo
    const devAllowedDomain = 'iesjandula.es'; // Dominio permitido para desarrollo
    const googleWorkspaceDomain = 'g.educaand.es'; // Dominio Google Workspace

    // Getters computados
    const getUserEmail = computed(() => usuario.value?.email || '');
    const getUserName = computed(() => nombreUsuario.value || usuario.value?.email || '');
    const getIsAdmin = computed(() => isAdmin.value);
    const isEmailMode = computed(() => authMode === 'email');
    const isGoogleMode = computed(() => authMode === 'google');

    const initializeAuth = async () => {
        loading.value = true;
        try {
            if (isEmailMode.value) {
                // Modo Email: Verificar localStorage
                const savedUser = localStorage.getItem('auth_user');
                const savedProfesor = localStorage.getItem('profesor_data');
                
                if (savedUser && savedProfesor) {
                    const user = JSON.parse(savedUser);
                    const profesor = JSON.parse(savedProfesor);
                    
                    // Restaurar estado sin llamar APIs
                    usuario.value = user;
                    isAuthenticated.value = true;
                    nombreUsuario.value = profesor.nombre;
                    isAdmin.value = profesor.es_admin;
                    
                    console.log('🧪 Sesión de desarrollo restaurada:', user.email);
                }
            } else {
                // Modo Google: Verificar sesión de Supabase
                const { data: { session } } = await supabase.auth.getSession();
                
                if (session?.user) {
                    await setUser(session.user, true);
                }

                // Escuchar cambios en la autenticación de Supabase
                supabase.auth.onAuthStateChange(async (event, session) => {
                    if (event === 'SIGNED_IN' && session?.user) {
                        await setUser(session.user, true);
                    } else if (event === 'SIGNED_OUT') {
                        clearUser();
                    }
                });
            }
        } catch (error) {
            console.error('Error inicializando autenticación:', error);
        } finally {
            loading.value = false;
        }
    };

    const setUser = async (user, isSupabaseAuth = true) => {
        // Normalizar user: si es string, crear objeto con email
        const userObj = typeof user === 'string' ? { email: user } : user;
        
        // Verificar dominio según el modo
        if (!checkEmailDomain(userObj.email)) {
            if (isSupabaseAuth) {
                await logout();
            }
            const allowedDomain = isEmailMode.value ? devAllowedDomain : googleWorkspaceDomain;
            throw new Error(`Solo se permiten cuentas del dominio @${allowedDomain}`);
        }

        usuario.value = userObj;
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
                    nombre: user.user_metadata?.full_name || user.name || user.email,
                    es_admin: false,
                    activo: true
                };
                
                const profesorCreado = await profesoresAPI.upsertProfesor(nuevoProfesor);
                nombreUsuario.value = profesorCreado.nombre;
                isAdmin.value = profesorCreado.es_admin;
            }

            // En modo email, guardar en localStorage
            if (isEmailMode.value) {
                localStorage.setItem('auth_user', JSON.stringify(user));
            }
        } catch (error) {
            console.error('Error obteniendo datos del profesor:', error);
            nombreUsuario.value = user.name || user.email;
            isAdmin.value = false;
        }
    };

    const clearUser = () => {
        usuario.value = null;
        nombreUsuario.value = '';
        isAdmin.value = false;
        isAuthenticated.value = false;
        
        // Limpiar localStorage si estamos en modo email
        if (isEmailMode.value) {
            localStorage.removeItem('auth_user');
            localStorage.removeItem('profesor_data');
        }
    };

    // 📧 Login con Email/Password (Modo desarrollo)
    const loginWithEmail = async (email, password) => {
        loading.value = true;
        try {
            if (!checkEmailDomain(email)) {
                throw new Error(`Solo se permiten emails del dominio @${devAllowedDomain}`);
            }

            // ✅ MODO DESARROLLO: Autenticación completamente local
            console.log('🧪 Modo desarrollo: usando autenticación local');
            
            // Crear usuario mock basado en el email
            const mockUser = {
                id: email.split('@')[0].replace(/\./g, '_'),
                email: email,
                name: email.split('@')[0].replace(/\./g, ' ').replace(/(\b\w)/gi, w => w.toUpperCase()),
                user_metadata: {
                    full_name: email.split('@')[0].replace(/\./g, ' ').replace(/(\b\w)/gi, w => w.toUpperCase())
                },
                auth_mode: 'email_local',
                created_at: new Date().toISOString()
            };
            
            // Simular datos del profesor sin consultar la base de datos
            const mockProfesorData = {
                nombre: mockUser.name,
                email: mockUser.email,
                es_admin: email.includes('marie') ? true : false, // Marie será admin para pruebas
                activo: true
            };

            // Establecer usuario sin llamar a la API
            usuario.value = mockUser;
            isAuthenticated.value = true;
            nombreUsuario.value = mockProfesorData.nombre;
            isAdmin.value = mockProfesorData.es_admin;

            // Guardar en localStorage
            localStorage.setItem('auth_user', JSON.stringify(mockUser));
            localStorage.setItem('profesor_data', JSON.stringify(mockProfesorData));
            
            console.log('✅ Usuario de desarrollo autenticado:', mockUser.email);
            return { success: true, user: mockUser };

        } catch (error) {
            console.error('Error en login con email:', error);
            return { success: false, error: error.message };
        } finally {
            loading.value = false;
        }
    };

    // 🌐 Login con Google (Modo producción)
    const loginWithGoogle = async () => {
        loading.value = true;
        try {
            const { data, error } = await supabase.auth.signInWithOAuth({
                provider: 'google',
                options: {
                    redirectTo: `${window.location.origin}/auth/callback`,
                    queryParams: {
                        access_type: 'offline',
                        prompt: 'consent',
                        hd: googleWorkspaceDomain // Restringir al dominio del instituto
                    }
                }
            });

            if (error) throw error;

            // El usuario será manejado por el callback de onAuthStateChange
            return { success: true };
        } catch (error) {
            console.error('Error en login con Google:', error);
            return { success: false, error: error.message };
        } finally {
            loading.value = false;
        }
    };

    // 🚀 Login unificado (detecta modo automáticamente)
    const login = async (email, password) => {
        if (isEmailMode.value) {
            return await loginWithEmail(email, password);
        } else {
            return await loginWithGoogle();
        }
    };

    const checkEmailDomain = (email) => {
        // Si email es undefined, null o vacío, retornar false
        if (!email || typeof email !== 'string') {
            return false;
        }
        
        const allowedDomain = isEmailMode.value ? devAllowedDomain : googleWorkspaceDomain;
        const domain = email.split('@')[1];
        return domain === allowedDomain;
    };

    const logout = async () => {
        try {
            if (isGoogleMode.value) {
                const { error } = await supabase.auth.signOut();
                if (error) throw error;
            }
            
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
        isEmailMode,
        isGoogleMode,
        // Actions
        initializeAuth,
        setUser, 
        clearUser,
        login,
        loginWithEmail,
        loginWithGoogle,
        logout,
        resetUser,
        checkEmailDomain,
        // Configuración
        authMode,
        devAllowedDomain,
        googleWorkspaceDomain
    };
});
