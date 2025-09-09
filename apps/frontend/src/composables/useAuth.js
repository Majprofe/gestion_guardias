import { ref, computed } from 'vue';
import { supabase } from '@/supabaseClient';

// Estado global reactivo para el usuario
const user = ref({
  isAuthenticated: false,
  email: null,
  name: null,
  role: null,
  loading: true
});

export function useAuth() {
  // Getters computados
  const isAuthenticated = computed(() => user.value.isAuthenticated);
  const userEmail = computed(() => user.value.email);
  const userName = computed(() => user.value.name);
  const userRole = computed(() => user.value.role);
  const isAdmin = computed(() => user.value.role === 'admin');
  const isProfesor = computed(() => user.value.role === 'profesor');
  const isLoading = computed(() => user.value.loading);

  // Función para inicializar el estado del usuario
  const initializeAuth = async () => {
    try {
      user.value.loading = true;
      
      // Verificar sesión actual
      const { data: { session } } = await supabase.auth.getSession();
      
      if (session?.user) {
        await updateUserFromDatabase(session.user.email);
      } else {
        clearUserData();
      }
    } catch (error) {
      console.error('Error inicializando autenticación:', error);
      clearUserData();
    } finally {
      user.value.loading = false;
    }
  };

  // Función para actualizar datos del usuario desde la base de datos
  const updateUserFromDatabase = async (email) => {
    try {
      const { data: userData, error } = await supabase
        .from("usuarios")
        .select("rol, nombre, activo")
        .eq("email", email)
        .eq("activo", true)
        .single();

      if (userData && !error) {
        user.value = {
          isAuthenticated: true,
          email: email,
          name: userData.nombre,
          role: userData.rol,
          loading: false
        };

        // Actualizar localStorage
        localStorage.setItem("userRole", userData.rol);
        localStorage.setItem("userName", userData.nombre);
        localStorage.setItem("userEmail", email);
        
        return true;
      } else {
        clearUserData();
        return false;
      }
    } catch (error) {
      console.error('Error obteniendo datos del usuario:', error);
      clearUserData();
      return false;
    }
  };

  // Función para limpiar datos del usuario
  const clearUserData = () => {
    user.value = {
      isAuthenticated: false,
      email: null,
      name: null,
      role: null,
      loading: false
    };

    // Limpiar localStorage
    localStorage.removeItem("userRole");
    localStorage.removeItem("userName");
    localStorage.removeItem("userEmail");
  };

  // Función para cerrar sesión
  const logout = async () => {
    try {
      await supabase.auth.signOut();
      clearUserData();
      return true;
    } catch (error) {
      console.error('Error cerrando sesión:', error);
      return false;
    }
  };

  // Función para verificar permisos
  const hasPermission = (requiredRole) => {
    if (!user.value.isAuthenticated) return false;
    
    if (requiredRole === 'admin') {
      return user.value.role === 'admin';
    }
    
    if (requiredRole === 'profesor') {
      return user.value.role === 'profesor' || user.value.role === 'admin';
    }
    
    return true; // Para cualquier usuario autenticado
  };

  // Función para refrescar datos del usuario
  const refreshUser = async () => {
    const { data: { session } } = await supabase.auth.getSession();
    if (session?.user) {
      return await updateUserFromDatabase(session.user.email);
    }
    return false;
  };

  return {
    // Estado
    user: user.value,
    
    // Getters computados
    isAuthenticated,
    userEmail,
    userName,
    userRole,
    isAdmin,
    isProfesor,
    isLoading,
    
    // Métodos
    initializeAuth,
    updateUserFromDatabase,
    clearUserData,
    logout,
    hasPermission,
    refreshUser
  };
}
