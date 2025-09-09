<template>
  <div class="home-container">
      <div class="buttons-container">
        <router-link to="/formulario" class="button animated-button" style="animation-delay: 0s">
          <img src="../assets/falta.png" class="img" />Registrar Ausencia
        </router-link>

        <router-link to="/gestion-faltas" class="button animated-button" style="animation-delay: 0.1s">
          <img src="../assets/tablon.png" class="img" />Tablón de Faltas
        </router-link>

        <router-link v-if="isAdmin" to="/historico" class="button animated-button" style="animation-delay: 0.2s">
          <img src="../assets/historico.png" class="img" />Histórico
        </router-link>

        <router-link to="/mis-guardias" class="button animated-button" style="animation-delay: 0.3s">
          <img src="../assets/cobertura.png" class="img" />Mis Guardias
        </router-link>
        <router-link to="/horario" class="button animated-button" style="animation-delay: 0.4s">
          <img src="../assets/horario.png" class="img" />Mi Horario
        </router-link>
      </div>
  </div>
</template>


<script setup>
import { ref, onMounted } from "vue";
import { supabase } from "@/supabaseClient"; 

const isAdmin = ref(false);

onMounted(async () => {
  const { data: { session } } = await supabase.auth.getSession();

  if (session?.user) {
    const userRole = localStorage.getItem("userRole");

    if (userRole === 'admin') {
      isAdmin.value = true;
    }
  }
});
</script>

<style scoped>
@keyframes fadeInUp {
  0% {
    opacity: 0;
    transform: translateY(20px);
  }
  100% {
    opacity: 1;
    transform: translateY(0);
  }
}

.animated-button {
  opacity: 0;
  animation: fadeInUp 0.5s ease-out forwards;
}

.home-container {
  display: flex;
  flex-direction: column;
  text-align: center;
  padding: 20px;
  min-height: 100vh;
}

.buttons-container {
  display: grid;
  grid-template-columns: repeat(1, 1fr);
  gap: 15px;
  justify-content: center;
  max-width: 500px;
  min-width: 300px;
  margin: 20px auto;
}

.img {
  width: 40px;
  margin-right: 10px;
}

.button {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  padding: 14px 20px;
  background-color: #1F86A1;
  color: white;
  text-decoration: none;
  border-radius: 16px;
  font-weight: 600;
  transition: background 0.3s;
  box-shadow: 0 4px 10px rgba(26, 35, 126, 0.3);
  font-size: 14px;
  gap: 20px;
  width: 100%;
  text-align: left;
}

.button:hover {
  background-color: #1b758c
;
}

@media (max-width: 480px) and (max-height: 940px) {
  .buttons-container {
    grid-template-columns: 1fr;
    gap: 10px;
    padding: 0 10px;
  }


  .button {
    font-size: 13px;
    padding: 12px;
  }

  .img {
    width: 32px;
    margin-right: 8px;
  }
}
</style>
