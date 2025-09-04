<template>
  <div class="login-container">
    <img src="../assets/logo.jpg" alt="Logo" class="logo" />
    <div class="form-container">
      <h2>Gestión Guardias Profesorado</h2>
      
      <!-- MODO EMAIL: Formulario Email/Password -->
      <div v-if="userStore.isEmailMode" class="email-auth">
        <div class="auth-mode-badge dev">
          🧪 MODO DESARROLLO
        </div>
        
        <form @submit.prevent="loginWithEmail" class="email-form">
          <div class="form-group">
            <label for="email">Email de prueba:</label>
            <input
              id="email"
              v-model="email"
              type="email"
              placeholder="usuario@example.com"
              :disabled="loading"
              required
            />
          </div>
          
          <div class="form-group">
            <label for="password">Contraseña:</label>
            <input
              id="password"
              v-model="password"
              type="password"
              placeholder="Cualquier contraseña para prueba"
              :disabled="loading"
              required
            />
          </div>

          <button 
            type="submit" 
            :disabled="loading"
            class="email-btn"
          >
            <span v-if="!loading">Acceder</span>
            <span v-else>Iniciando sesión...</span>
          </button>
        </form>
        
        <!-- Lista de emails de prueba -->
        <div class="test-emails">
          <h4>📧 Usuarios de prueba disponibles:</h4>
          <div class="email-buttons">
            <button 
              v-for="testUser in testUsers" 
              :key="testUser.email"
              @click="selectTestUser(testUser)"
              class="test-email-btn"
              :disabled="loading"
            >
              <div class="user-info">
                <strong>{{ testUser.abreviatura }}</strong>
                <span>{{ testUser.nombre }}</span>
                <small v-if="testUser.isAdmin" class="admin-badge">👑 Admin</small>
              </div>
            </button>
          </div>
          
          <div class="dev-info">
            <p>💡 <strong>Tip:</strong> Selecciona un usuario y haz clic en "Acceder"</p>
            <p>🔑 Cualquier contraseña es válida en modo desarrollo</p>
          </div>
        </div>
      </div>

      <!-- MODO GOOGLE: OAuth -->
      <div v-else class="google-auth">
        <div class="auth-mode-badge prod">
          🚀 MODO PRODUCCIÓN
        </div>
        
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
          <span v-else>Conectando con Google...</span>
        </button>

        <div class="domain-info">
          <p>Solo usuarios de <strong>@{{ userStore.googleWorkspaceDomain }}</strong></p>
        </div>
      </div>

      <p v-if="error" class="error-message">{{ error }}</p>
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
const password = ref("123456"); // Contraseña por defecto para desarrollo
const error = ref("");
const loading = ref(false);

// Datos de prueba de usuarios
const testUsers = [
  {
    id: 2,
    nombre: "Curie Mendel, Marie",
    abreviatura: "CME",
    email: "marie.curiemendel@instituto.edu",
    isAdmin: true
  },
  {
    id: 3,
    nombre: "Newton Tesla, Nikola",
    abreviatura: "NTN",
    email: "nikola.newtontesla@instituto.edu",
    isAdmin: false
  },
  {
    id: 4,
    nombre: "Darwin Hawking, Stephen",
    abreviatura: "DHS",
    email: "stephen.darwinhawking@instituto.edu",
    isAdmin: false
  }
];

const selectTestUser = (testUser) => {
  email.value = testUser.email;
  password.value = "123456"; // Contraseña por defecto
};

const loginWithEmail = async () => {
  try {
    loading.value = true;
    error.value = "";

    const result = await userStore.loginWithEmail(email.value, password.value);
    
    if (result.success) {
      toast.success("¡Sesión iniciada correctamente!");
      router.push("/");
    } else {
      error.value = result.error;
      toast.error(`Error al iniciar sesión: ${result.error}`);
    }
  } catch (err) {
    error.value = err.message;
    toast.error(`Error: ${err.message}`);
  } finally {
    loading.value = false;
  }
};

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
  max-width: 450px;
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

/* Badge de modo */
.auth-mode-badge {
  text-align: center;
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  margin-bottom: 20px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.auth-mode-badge.dev {
  background: linear-gradient(135deg, #ff9800, #f57c00);
  color: white;
}

.auth-mode-badge.prod {
  background: linear-gradient(135deg, #4caf50, #2e7d32);
  color: white;
}

/* Formulario Email */
.email-form {
  margin-bottom: 25px;
}

.form-group {
  margin-bottom: 15px;
}

.form-group label {
  display: block;
  margin-bottom: 5px;
  color: #333;
  font-weight: 500;
  font-size: 14px;
}

input {
  width: 100%;
  padding: 12px;
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

.email-btn {
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
}

.email-btn:hover:not(:disabled) {
  background: linear-gradient(90deg, #197a90, #3fa7be);
  box-shadow: 0px 4px 8px rgba(31, 134, 161, 0.3);
}

.email-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* Emails de prueba */
.test-emails {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #e1e5e9;
}

.test-emails h4 {
  color: #666;
  font-size: 14px;
  margin-bottom: 12px;
  text-align: center;
}

.email-buttons {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.test-email-btn {
  width: 100%;
  padding: 12px;
  background: #f8f9fa;
  border: 1px solid #e1e5e9;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s ease;
  text-align: left;
}

.test-email-btn:hover:not(:disabled) {
  background: #e9ecef;
  border-color: #1F86A1;
}

.test-email-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.user-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.user-info strong {
  color: #1F86A1;
  font-size: 14px;
}

.user-info span {
  color: #666;
  font-size: 12px;
}

.admin-badge {
  color: #ff9800 !important;
  font-weight: 600;
  font-size: 11px !important;
}

.dev-info {
  margin-top: 15px;
  padding: 12px;
  background: #f0f9ff;
  border: 1px solid #bae6fd;
  border-radius: 6px;
}

.dev-info p {
  color: #0369a1;
  font-size: 12px;
  margin: 4px 0;
}

.dev-info p:first-child {
  margin-top: 0;
}

.dev-info p:last-child {
  margin-bottom: 0;
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
  margin-bottom: 15px;
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

.domain-info {
  text-align: center;
}

.domain-info p {
  color: #666;
  font-size: 12px;
  background: #f8f9fa;
  padding: 8px 12px;
  border-radius: 6px;
  border: 1px solid #e9ecef;
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

@media (max-width: 480px) {
  .login-container {
    padding: 10px;
  }

  .form-container {
    padding: 25px;
    max-width: 380px;
  }

  h2 {
    font-size: 18px;
  }

  .google-btn,
  .email-btn {
    font-size: 14px;
    padding: 10px;
  }

  input {
    font-size: 14px;
    padding: 10px;
  }
}
</style>
