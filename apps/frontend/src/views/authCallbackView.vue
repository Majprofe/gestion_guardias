<template>
  <div class="callback-container">
    <div class="loading-content">
      <div class="spinner"></div>
      <h2>Procesando autenticación...</h2>
      <p>{{ message }}</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '@/stores/user';
import { useToast } from 'vue-toastification';

const router = useRouter();
const userStore = useUserStore();
const toast = useToast();

const message = ref('Verificando credenciales...');

onMounted(async () => {
  try {
    message.value = 'Obteniendo información del usuario...';
    
    // Esperar a que Supabase procese la autenticación
    await new Promise(resolve => setTimeout(resolve, 1000));
    
    // El store ya maneja la autenticación a través del listener onAuthStateChange
    if (userStore.isAuthenticated) {
      message.value = '¡Autenticación exitosa! Redirigiendo...';
      toast.success('¡Bienvenido al sistema!');
      
      setTimeout(() => {
        router.push('/');
      }, 1500);
    } else {
      // Si no está autenticado después de un tiempo, hay un error
      setTimeout(() => {
        if (!userStore.isAuthenticated) {
          message.value = 'Error en la autenticación';
          toast.error('Error al procesar la autenticación. Inténtalo de nuevo.');
          
          setTimeout(() => {
            router.push('/login');
          }, 2000);
        }
      }, 3000);
    }
  } catch (error) {
    console.error('Error en callback de autenticación:', error);
    message.value = 'Error al procesar la autenticación';
    toast.error('Error al procesar la autenticación');
    
    setTimeout(() => {
      router.push('/login');
    }, 2000);
  }
});
</script>

<style scoped>
.callback-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: linear-gradient(135deg, #f5fafa, #d2eef3);
  font-family: 'Montserrat', sans-serif;
}

.loading-content {
  text-align: center;
  background: white;
  padding: 40px;
  border-radius: 12px;
  box-shadow: 0px 4px 10px rgba(0, 0, 0, 0.1);
  max-width: 400px;
  width: 90%;
}

.spinner {
  width: 50px;
  height: 50px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #1F86A1;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 20px auto;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

h2 {
  color: #1F86A1;
  margin-bottom: 15px;
  font-size: 18px;
  font-weight: 600;
}

p {
  color: #666;
  font-size: 14px;
  margin: 0;
}
</style>
