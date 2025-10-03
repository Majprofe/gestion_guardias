<template>
  <div class="auth-callback-container">
    <div class="callback-content">
      <div class="loading-spinner">
        <div class="spinner"></div>
      </div>
      
      <h2>{{ statusMessage }}</h2>
      <p class="status-description">{{ statusDescription }}</p>

      <div v-if="error" class="error-section">
        <div class="error-icon">⚠️</div>
        <h3>Error de Autenticación</h3>
        <p>{{ error }}</p>
        <button @click="retryLogin" class="retry-btn">
          Intentar de nuevo
        </button>
      </div>

      <div v-if="success" class="success-section">
        <div class="success-icon">✅</div>
        <h3>¡Autenticación exitosa!</h3>
        <p>Redirigiendo...</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { supabase } from '@/supabaseClient';
import { useToast } from 'vue-toastification';

const router = useRouter();
const toast = useToast();

const statusMessage = ref('Procesando autenticación...');
const statusDescription = ref('Por favor espera mientras verificamos tus credenciales.');
const error = ref('');
const success = ref(false);

const handleAuthCallback = async () => {
  try {
    // Obtener la sesión después del callback
    const { data: { session }, error: sessionError } = await supabase.auth.getSession();
    
    if (sessionError) {
      throw new Error(sessionError.message);
    }

    if (!session) {
      throw new Error('No se pudo establecer la sesión');
    }

    statusMessage.value = 'Verificando usuario...';
    statusDescription.value = 'Comprobando permisos y configuración.';

    // Verificar que el usuario existe en nuestra tabla usuarios
    const { data: userData, error: userError } = await supabase
      .from("usuarios")
      .select("rol, nombre, activo")
      .eq("email", session.user.email)
      .eq("activo", true)
      .single();

    if (userError || !userData) {
      // Si el usuario no existe, intentar crearlo automáticamente
      if (session.user.email && session.user.email.includes('@iesjandula.es')) {
        statusMessage.value = 'Configurando cuenta...';
        statusDescription.value = 'Creando tu perfil en el sistema.';

        const { error: insertError } = await supabase
          .from("usuarios")
          .insert({
            auth_id: session.user.id,
            email: session.user.email,
            nombre: session.user.user_metadata?.full_name || session.user.email,
            rol: 'profesor',
            activo: true
          });

        if (insertError) {
          throw new Error('Error al crear el perfil de usuario');
        }

        // Obtener los datos del usuario recién creado
        const { data: newUserData, error: newUserError } = await supabase
          .from("usuarios")
          .select("rol, nombre, activo")
          .eq("email", session.user.email)
          .single();

        if (newUserError || !newUserData) {
          throw new Error('Error al obtener datos del usuario');
        }

        userData = newUserData;
      } else {
        throw new Error('Usuario no autorizado. Solo se permiten cuentas de @iesjandula.es');
      }
    }

    // Guardar información del usuario en localStorage
    localStorage.setItem("userRole", userData.rol);
    localStorage.setItem("userName", userData.nombre);
    localStorage.setItem("userEmail", session.user.email);

    statusMessage.value = '¡Autenticación completada!';
    statusDescription.value = `Bienvenido ${userData.nombre}`;
    success.value = true;

    // Mostrar mensaje de bienvenida
    toast.success(`¡Bienvenido ${userData.nombre}!`);

    // Redirigir según el rol después de un breve delay
    setTimeout(() => {
      if (userData.rol === 'admin') {
        router.push("/admin");
      } else {
        router.push("/");
      }
    }, 2000);

  } catch (err) {
    console.error('Error en callback de autenticación:', err);
    error.value = err.message || 'Error desconocido durante la autenticación';
    statusMessage.value = 'Error de autenticación';
    statusDescription.value = 'Ha ocurrido un problema durante el proceso de autenticación.';
    
    // Limpiar sesión si hay error
    await supabase.auth.signOut();
    localStorage.clear();
    
    toast.error('Error en la autenticación');
  }
};

const retryLogin = () => {
  router.push('/login');
};

onMounted(() => {
  // Procesar el callback de autenticación
  handleAuthCallback();
});
</script>

<style scoped>
.auth-callback-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f5fafa, #d2eef3);
  font-family: 'Montserrat', sans-serif;
  padding: 20px;
}

.callback-content {
  max-width: 500px;
  width: 100%;
  text-align: center;
  background: white;
  padding: 40px;
  border-radius: 20px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
}

.loading-spinner {
  margin-bottom: 30px;
}

.spinner {
  width: 50px;
  height: 50px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #1F86A1;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

h2 {
  color: #333;
  font-size: 1.8rem;
  margin-bottom: 15px;
  font-weight: 600;
}

.status-description {
  color: #666;
  font-size: 1rem;
  margin-bottom: 30px;
  line-height: 1.6;
}

.error-section {
  background: #fff5f5;
  border: 2px solid #fed7d7;
  border-radius: 12px;
  padding: 30px;
  margin-top: 20px;
}

.error-icon {
  font-size: 3rem;
  margin-bottom: 15px;
}

.error-section h3 {
  color: #c53030;
  margin-bottom: 10px;
}

.error-section p {
  color: #744210;
  margin-bottom: 20px;
}

.retry-btn {
  background: linear-gradient(90deg, #1F86A1, #4DB8D0);
  color: white;
  border: none;
  padding: 12px 24px;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 16px;
}

.retry-btn:hover {
  background: linear-gradient(90deg, #197a90, #3fa7be);
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(31, 134, 161, 0.3);
}

.success-section {
  background: #f0fff4;
  border: 2px solid #9ae6b4;
  border-radius: 12px;
  padding: 30px;
  margin-top: 20px;
}

.success-icon {
  font-size: 3rem;
  margin-bottom: 15px;
}

.success-section h3 {
  color: #38a169;
  margin-bottom: 10px;
}

.success-section p {
  color: #2f855a;
}

/* Responsive */
@media (max-width: 768px) {
  .callback-content {
    padding: 30px 20px;
  }

  h2 {
    font-size: 1.5rem;
  }

  .error-section, .success-section {
    padding: 20px;
  }
}
</style>
