<template>
  <div class="not-found-container">
    <div class="not-found-content">
      <div class="error-animation">
        <div class="error-number">4</div>
        <div class="error-number middle">0</div>
        <div class="error-number">4</div>
      </div>
      
      <h1 class="error-title">¡Oops! Página no encontrada</h1>
      
      <p class="error-description">
        La página que estás buscando no existe o ha sido movida.
        Puede que hayas introducido mal la URL o que el enlace esté roto.
      </p>
      
      <div class="error-actions">
        <router-link to="/" class="btn btn-primary">
          🏠 Volver al Inicio
        </router-link>
        
        <button @click="goBack" class="btn btn-secondary">
          ⬅️ Página Anterior
        </button>
        
        <router-link to="/formulario" class="btn btn-outline">
          📝 Solicitar Ausencia
        </router-link>
      </div>
      
      <div class="helpful-links">
        <h3>Enlaces útiles:</h3>
        <div class="links-grid">
          <router-link to="/mis-guardias" class="quick-link">
            🛡️ Mis Guardias
          </router-link>
          
          <router-link to="/horario" class="quick-link">
            📅 Mi Horario
          </router-link>
          
          <router-link to="/historico" class="quick-link" v-if="isAdmin">
            📈 Histórico
          </router-link>
          
          <router-link to="/estadisticas" class="quick-link" v-if="isAdmin">
            📊 Estadísticas
          </router-link>
        </div>
      </div>
      
      <div class="error-details">
        <details>
          <summary>Información técnica</summary>
          <div class="tech-info">
            <p><strong>Ruta solicitada:</strong> {{ $route.fullPath }}</p>
            <p><strong>Fecha:</strong> {{ new Date().toLocaleString('es-ES') }}</p>
            <p><strong>User Agent:</strong> {{ navigator.userAgent.substring(0, 50) }}...</p>
          </div>
        </details>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const isAdmin = computed(() => userStore.getIsAdmin)

const goBack = () => {
  if (window.history.length > 1) {
    router.go(-1)
  } else {
    router.push('/')
  }
}
</script>

<style scoped>
.not-found-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  font-family: 'Montserrat', sans-serif;
  padding: 20px;
}

.not-found-content {
  text-align: center;
  background: white;
  border-radius: 20px;
  padding: 40px;
  box-shadow: 0 20px 60px rgba(0,0,0,0.2);
  max-width: 600px;
  width: 100%;
  animation: slideIn 0.6s ease-out;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.error-animation {
  display: flex;
  justify-content: center;
  gap: 10px;
  margin-bottom: 30px;
}

.error-number {
  font-size: 8rem;
  font-weight: 900;
  color: #1f86a1;
  text-shadow: 3px 3px 0px #ddd;
  animation: bounce 2s infinite;
}

.error-number.middle {
  animation-delay: 0.2s;
  color: #ff4757;
}

.error-number:last-child {
  animation-delay: 0.4s;
}

@keyframes bounce {
  0%, 20%, 50%, 80%, 100% {
    transform: translateY(0);
  }
  40% {
    transform: translateY(-10px);
  }
  60% {
    transform: translateY(-5px);
  }
}

.error-title {
  font-size: 2.5rem;
  color: #333;
  margin: 20px 0;
  font-weight: 700;
}

.error-description {
  font-size: 1.1rem;
  color: #666;
  line-height: 1.6;
  margin-bottom: 30px;
}

.error-actions {
  display: flex;
  gap: 15px;
  justify-content: center;
  flex-wrap: wrap;
  margin-bottom: 40px;
}

.btn {
  padding: 12px 24px;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  text-decoration: none;
  cursor: pointer;
  transition: all 0.3s ease;
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.btn-primary {
  background: #1f86a1;
  color: white;
}

.btn-primary:hover {
  background: #176a82;
  transform: translateY(-2px);
}

.btn-secondary {
  background: #8e8e93;
  color: white;
}

.btn-secondary:hover {
  background: #6d6d78;
  transform: translateY(-2px);
}

.btn-outline {
  background: transparent;
  color: #1f86a1;
  border: 2px solid #1f86a1;
}

.btn-outline:hover {
  background: #1f86a1;
  color: white;
  transform: translateY(-2px);
}

.helpful-links {
  margin-bottom: 30px;
}

.helpful-links h3 {
  color: #333;
  margin-bottom: 15px;
  font-weight: 600;
}

.links-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 10px;
}

.quick-link {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 15px;
  background: #f8f9fa;
  border-radius: 8px;
  color: #495057;
  text-decoration: none;
  font-weight: 500;
  transition: all 0.3s ease;
}

.quick-link:hover {
  background: #e9ecef;
  color: #1f86a1;
  transform: translateY(-2px);
}

.error-details {
  border-top: 1px solid #eee;
  padding-top: 20px;
}

.error-details summary {
  color: #666;
  cursor: pointer;
  font-weight: 500;
  margin-bottom: 10px;
}

.tech-info {
  background: #f8f9fa;
  padding: 15px;
  border-radius: 8px;
  text-align: left;
  font-family: 'Courier New', monospace;
  font-size: 0.9rem;
  color: #495057;
}

.tech-info p {
  margin: 5px 0;
  word-break: break-all;
}

/* Responsive Design */
@media (max-width: 768px) {
  .not-found-content {
    padding: 30px 20px;
  }
  
  .error-number {
    font-size: 5rem;
  }
  
  .error-title {
    font-size: 2rem;
  }
  
  .error-actions {
    flex-direction: column;
    align-items: center;
  }
  
  .btn {
    width: 100%;
    max-width: 250px;
    justify-content: center;
  }
  
  .links-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 480px) {
  .error-animation {
    gap: 5px;
  }
  
  .error-number {
    font-size: 4rem;
  }
  
  .error-title {
    font-size: 1.8rem;
  }
  
  .error-description {
    font-size: 1rem;
  }
}
</style>
