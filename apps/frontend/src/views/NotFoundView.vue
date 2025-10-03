<template>
  <div class="not-found-container">
    <div class="not-found-content">
      <div class="error-icon">
        <span class="error-number">404</span>
      </div>
      
      <div class="error-message">
        <h1>¡Oops! Página no encontrada</h1>
        <p>La página que estás buscando no existe o ha sido movida.</p>
      </div>

      <div class="error-actions">
        <button @click="goHome" class="btn-primary">
          🏠 Ir al Inicio
        </button>
        <button @click="goBack" class="btn-secondary">
          ← Volver Atrás
        </button>
      </div>

      <div class="helpful-links">
        <h3>Enlaces útiles:</h3>
        <div class="links-grid">
          <router-link to="/" class="help-link">
            🏠 Página Principal
          </router-link>
          <router-link to="/formulario" class="help-link">
            📝 Registrar Ausencia
          </router-link>
          <router-link to="/gestion-faltas" class="help-link">
            📋 Tablón de Faltas
          </router-link>
          <router-link to="/mis-guardias" class="help-link">
            👥 Mis Guardias
          </router-link>
        </div>
      </div>

      <div class="error-details" v-if="showDetails">
        <h4>Detalles técnicos:</h4>
        <p><strong>URL solicitada:</strong> {{ $route.fullPath }}</p>
        <p><strong>Hora:</strong> {{ currentTime }}</p>
      </div>

      <button @click="toggleDetails" class="toggle-details">
        {{ showDetails ? 'Ocultar' : 'Mostrar' }} detalles técnicos
      </button>
    </div>

    <!-- Ilustración decorativa -->
    <div class="illustration">
      <div class="floating-shapes">
        <div class="shape shape-1"></div>
        <div class="shape shape-2"></div>
        <div class="shape shape-3"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();
const showDetails = ref(false);
const currentTime = ref('');

const goHome = () => {
  router.push('/');
};

const goBack = () => {
  if (window.history.length > 1) {
    router.go(-1);
  } else {
    router.push('/');
  }
};

const toggleDetails = () => {
  showDetails.value = !showDetails.value;
};

onMounted(() => {
  currentTime.value = new Date().toLocaleString();
});
</script>

<style scoped>
.not-found-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f5fafa, #d2eef3);
  font-family: 'Montserrat', sans-serif;
  padding: 20px;
  position: relative;
  overflow: hidden;
}

.not-found-content {
  max-width: 600px;
  text-align: center;
  background: white;
  padding: 40px;
  border-radius: 20px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
  position: relative;
  z-index: 2;
}

.error-icon {
  margin-bottom: 30px;
}

.error-number {
  font-size: 8rem;
  font-weight: 800;
  background: linear-gradient(45deg, #1F86A1, #4DB8D0);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  line-height: 1;
}

.error-message h1 {
  color: #333;
  font-size: 2rem;
  margin-bottom: 15px;
  font-weight: 600;
}

.error-message p {
  color: #666;
  font-size: 1.1rem;
  margin-bottom: 30px;
  line-height: 1.6;
}

.error-actions {
  display: flex;
  gap: 15px;
  justify-content: center;
  margin-bottom: 40px;
  flex-wrap: wrap;
}

.btn-primary, .btn-secondary {
  padding: 12px 24px;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 16px;
  text-decoration: none;
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.btn-primary {
  background: linear-gradient(90deg, #1F86A1, #4DB8D0);
  color: white;
}

.btn-primary:hover {
  background: linear-gradient(90deg, #197a90, #3fa7be);
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(31, 134, 161, 0.3);
}

.btn-secondary {
  background: #f8f9fa;
  color: #495057;
  border: 2px solid #dee2e6;
}

.btn-secondary:hover {
  background: #e9ecef;
  border-color: #adb5bd;
  transform: translateY(-2px);
}

.helpful-links {
  margin-bottom: 30px;
  text-align: left;
}

.helpful-links h3 {
  color: #333;
  margin-bottom: 15px;
  text-align: center;
}

.links-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 10px;
}

.help-link {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: #f8f9fa;
  border-radius: 8px;
  text-decoration: none;
  color: #495057;
  transition: all 0.2s ease;
  border: 1px solid #e9ecef;
}

.help-link:hover {
  background: #e9ecef;
  transform: translateX(5px);
  border-color: #1F86A1;
}

.error-details {
  background: #f8f9fa;
  padding: 20px;
  border-radius: 8px;
  margin: 20px 0;
  text-align: left;
  border-left: 4px solid #1F86A1;
}

.error-details h4 {
  color: #333;
  margin-bottom: 10px;
}

.error-details p {
  color: #666;
  margin: 5px 0;
  font-family: monospace;
  font-size: 14px;
}

.toggle-details {
  background: none;
  border: none;
  color: #1F86A1;
  cursor: pointer;
  text-decoration: underline;
  font-size: 14px;
}

.illustration {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 1;
  pointer-events: none;
}

.floating-shapes {
  position: relative;
  width: 100%;
  height: 100%;
}

.shape {
  position: absolute;
  border-radius: 50%;
  opacity: 0.1;
  animation: float 6s ease-in-out infinite;
}

.shape-1 {
  width: 80px;
  height: 80px;
  background: #1F86A1;
  top: 20%;
  left: 10%;
  animation-delay: 0s;
}

.shape-2 {
  width: 120px;
  height: 120px;
  background: #4DB8D0;
  top: 60%;
  right: 15%;
  animation-delay: 2s;
}

.shape-3 {
  width: 60px;
  height: 60px;
  background: #1F86A1;
  bottom: 20%;
  left: 20%;
  animation-delay: 4s;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0px) rotate(0deg);
  }
  50% {
    transform: translateY(-20px) rotate(180deg);
  }
}

/* Responsive */
@media (max-width: 768px) {
  .not-found-content {
    padding: 30px 20px;
  }

  .error-number {
    font-size: 6rem;
  }

  .error-message h1 {
    font-size: 1.5rem;
  }

  .error-actions {
    flex-direction: column;
    align-items: center;
  }

  .btn-primary, .btn-secondary {
    width: 100%;
    max-width: 250px;
  }

  .links-grid {
    grid-template-columns: 1fr;
  }
}
</style>
