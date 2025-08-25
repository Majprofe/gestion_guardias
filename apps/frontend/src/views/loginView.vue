<template>
  <div class="login-container">
    <img src="../assets/logo.jpg" alt="Logo" class="logo" />
    <div class="form-container">
      <h2>Gestión Guardias Profesorado</h2>
      
      <!-- Botón de Google Login (principal) -->
      <button 
        @click="loginWithGoogle" 
        :disabled="loading"
        class="google-btn"
        type="button"
      >
        <svg class="google-icon" viewBox="0 0 24 24">
          <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"/>
          <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/>
          <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"/>
          <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"/>
        </svg>
        <span v-if="!loading">Iniciar sesión con Google</span>
        <span v-else>Cargando...</span>
      </button>

      <p v-if="error" class="error-message">{{ error }}</p>

      <div class="info-message">
        <p>Solo se permiten cuentas del dominio @g.educaand.es</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import { useUserStore } from "@/stores/user";
import { useToast } from "vue-toastification";

const toast = useToast();
const router = useRouter();
const userStore = useUserStore();

const email = ref("");
const password = ref("");
const error = ref("");
const loading = ref(false);

const loginWithGoogle = async () => {
  try {
    loading.value = true;
    error.value = "";
    
    const result = await userStore.loginWithGoogle();
    
    if (!result.success) {
      error.value = result.error;
      toast.error(`Error al iniciar sesión: ${result.error}`);
    } else {
      toast.success("Redirigiendo a Google...");
    }
  } catch (err) {
    error.value = err.message;
    toast.error(`Error: ${err.message}`);
  } finally {
    loading.value = false;
  }
};

const login = async () => {
  try {
    loading.value = true;
    error.value = "";

    // Verificar dominio antes del login
    if (!userStore.checkEmailDomain(email.value)) {
      error.value = "Solo se permiten cuentas del dominio @g.educaand.es";
      toast.error(error.value);
      return;
    }

    const result = await userStore.login(email.value, password.value);
    
    if (result.success) {
      toast.success("¡Sesión iniciada correctamente!");
      router.push("/");
    } else {
      error.value = result.error;
      toast.error("Error al iniciar sesión. Verifica tus credenciales.");
    }
  } catch (err) {
    error.value = err.message;
    toast.error(`Error: ${err.message}`);
  } finally {
    loading.value = false;
  }
};

// Verificar si ya está autenticado
onMounted(async () => {
  if (userStore.isAuthenticated) {
    router.push("/");
  }
});
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Montserrat:wght@300;400;600&display=swap');

* {
  font-family: 'Montserrat', sans-serif;
  box-sizing: border-box;
  margin: 0;
  padding: 0;
}

html,
body {
  height: 100%;
  background: #e6f7fa;
}

.login-container {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  height: 100vh;
  padding: 20px;
  background: linear-gradient(135deg, #f5fafa, #d2eef3);
}

.form-container {
  width: 100%;
  max-width: 400px;
  background-color: white;
  padding: 30px;
  border-radius: 12px;
  box-shadow: 0px 4px 10px rgba(0, 0, 0, 0.1);
}

.logo {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  margin: 0 auto 20px auto;
  display: block;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
}

h2 {
  text-align: center;
  color: #1F86A1;
  margin-bottom: 25px;
  font-size: 20px;
  font-weight: 600;
}

/* Botón de Google */
.google-btn {
  width: 100%;
  padding: 12px 16px;
  background: white;
  border: 2px solid #dadce0;
  border-radius: 8px;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  margin-bottom: 20px;
  color: #3c4043;
}

.google-btn:hover:not(:disabled) {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  border-color: #c5c7ca;
}

.google-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.google-icon {
  width: 20px;
  height: 20px;
}

/* Divider */
.divider {
  text-align: center;
  margin: 20px 0;
  position: relative;
}

.divider::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 0;
  right: 0;
  height: 1px;
  background: #e0e0e0;
}

.divider span {
  background: white;
  padding: 0 15px;
  color: #666;
  font-size: 14px;
}

/* Formulario tradicional */
.traditional-form {
  margin-top: 0;
}

input {
  width: 100%;
  padding: 12px;
  margin-bottom: 15px;
  border: 2px solid #e1e5e9;
  border-radius: 6px;
  font-size: 14px;
  transition: border-color 0.3s;
}

input:focus {
  border-color: #1F86A1;
  outline: none;
}

input:disabled {
  background-color: #f5f5f5;
  cursor: not-allowed;
}

.traditional-btn {
  width: 100%;
  padding: 12px;
  background: linear-gradient(90deg, #1F86A1, #4DB8D0);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  margin-top: 5px;
}

.traditional-btn:hover:not(:disabled) {
  background: linear-gradient(90deg, #197a90, #3fa7be);
  box-shadow: 0px 4px 8px rgba(31, 134, 161, 0.3);
}

.traditional-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.error-message {
  color: #d9534f;
  text-align: center;
  margin-top: 15px;
  padding: 10px;
  background: #f8d7da;
  border: 1px solid #f5c6cb;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
}

.info-message {
  margin-top: 20px;
  text-align: center;
}

.info-message p {
  color: #666;
  font-size: 12px;
  background: #f8f9fa;
  padding: 8px 12px;
  border-radius: 6px;
  border: 1px solid #e9ecef;
}

@media (max-width: 480px) {
  .login-container {
    padding: 10px;
  }

  .form-container {
    padding: 25px;
    max-width: 350px;
  }

  h2 {
    font-size: 18px;
  }

  .google-btn,
  .traditional-btn {
    font-size: 14px;
    padding: 10px;
  }

  input {
    font-size: 14px;
    padding: 10px;
  }
}
</style>
