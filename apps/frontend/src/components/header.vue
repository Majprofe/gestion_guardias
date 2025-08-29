<template>
  <section v-if="!isLoginPage" class="header-menu-container">
    <header class="header">
      <div class="user-info">
        <img src="../assets/logo.jpg" alt="Logo" class="logo" />
        <span class="usuario" v-if="nombreUsuario">{{ nombreUsuario }}</span>
      </div>
      <button class="logout" @click="logout" title="Cerrar sesión">
        <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24">
          <path fill="currentColor"
            d="M10 16l5-4-5-4v3H3v2h7v3zm9-11h-7V3h7c1.1 0 2 .9 2 2v14c0 1.1-.9 2-2 2h-7v-2h7V5z" />
        </svg>
      </button>
    </header>


    <nav class="menu">
      <router-link to="/" class="link" title="Inicio">
        <img class="link-icon" src="../assets/casa.png" alt="Inicio" />
        <span class="link-title">Inicio</span>
      </router-link>
      <router-link to="/formulario" class="link" title="Ausencia">
        <img class="link-icon" src="../assets/falta.png" alt="Registrar Ausencia" />
        <span class="link-title">Ausencia</span>
      </router-link>
      <router-link to="/gestion-faltas" class="link" title="Tablón">
        <img class="link-icon" src="../assets/tablon.png" alt="Tablón de Faltas" />
        <span class="link-title">Tablón</span>
      </router-link>
      <router-link v-if="isAdmin" to="/historico" class="link" title="Histórico">
        <img class="link-icon" src="../assets/historico.png" alt="Histórico" />
        <span class="link-title">Histórico</span>
      </router-link>
      <router-link v-if="isAdmin" to="/estadisticas" class="link" title="Estadísticas">
        <div class="link-icon emoji-icon">📊</div>
        <span class="link-title">Estadísticas</span>
      </router-link>
      <router-link v-if="isAdmin" to="/admin" class="link" title="Admin Panel">
        <div class="link-icon emoji-icon">🛠️</div>
        <span class="link-title">Admin</span>
      </router-link>
      <router-link to="/mis-guardias" class="link" title="Guardias">
        <img class="link-icon" src="../assets/cobertura.png" alt="Mis Guardias" />
        <span class="link-title">Guardias</span>
      </router-link>
      <router-link to="/horario" class="link" title="Horario">
        <img class="link-icon" src="../assets/horario.png" alt="Mi Horario" />
        <span class="link-title">Horario</span>
      </router-link>
    </nav>
  </section>
</template>

<script setup>
import { onMounted, computed } from "vue";
import { useRoute, useRouter } from "vue-router";
import { supabase } from "@/supabaseClient";
import { useUserStore } from "@/stores/user";

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();

const nombreUsuario = computed(() => userStore.nombreUsuario);
const isAdmin = computed(() => userStore.isAdmin);
const isLoginPage = computed(() => route.name === "login");

const logout = async () => {
  const { error } = await supabase.auth.signOut();
  if (error) {
    console.error("Error al cerrar sesión:", error.message);
  } else {
    userStore.resetUser();
    router.push("/login");
  }
};

onMounted(async () => {
  const { data: { session } } = await supabase.auth.getSession();
  if (session?.user) {
    userStore.setUser(session.user.email);
  }

  supabase.auth.onAuthStateChange((_, session) => {
    if (session?.user) {
      userStore.setUser(session.user.email);
    } else {
      userStore.resetUser();
    }
  });
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

.header-menu-container {
  display: flex;
  flex-direction: column;
  gap: 0;
  max-width: 500px;
  margin: 0 auto;
  overflow: hidden;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: #1F86A1;
  padding: 4px 12px;
  font-size: 12px;
  height: 36px;
  margin-bottom: 6%;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  max-width: 80%;
  overflow: hidden;
}

.logo {
  width: 24px;
  height: 24px;
  border-radius: 4px;
  object-fit: contain;
}


.usuario {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 80%;
  text-transform: uppercase;
  color: white;
  font-size: 12px;
}

.logout {
  background: none;
  border: none;
  cursor: pointer;
  padding: 4px;
  display: flex;
  align-items: center;
  color: #ffffff;
}

.menu {
  display: flex;
  justify-content: center;
  flex-wrap: wrap;
  gap: 8px;
  padding: 0.5rem 0.75rem;
}

.link {
  display: inline-flex;
  align-items: center;
  justify-content: flex-start;
  position: relative;
  z-index: 1;
  width: 70px;
  height: 50px;
  padding-left: 12px;
  overflow: hidden;
  border-radius: 8px;
  background-color: transparent;
  color: inherit;
  text-decoration: none;
  transition: width 0.2s ease-in;
}

.link::before {
  content: "";
  position: absolute;
  z-index: -1;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  border-radius: 8px;
  background-color: #e0e0e0;
  transform: translateX(100%);
  transition: transform 0.2s ease-in;
  transform-origin: center right;
}

.link:hover,
.link:focus,
.link.router-link-exact-active {
  outline: 0;
  width: 110px;
}

.link:hover::before,
.link:focus::before,
.link.router-link-exact-active::before {
  transform: translateX(0);
}

.link-title {
  display: block;
  text-align: left;
  text-indent: 10px;
  white-space: nowrap;
  transform: translateX(100%);
  opacity: 0;
  z-index: 1;
  font-size: 12px;
  transition: transform 0.2s ease-in, opacity 0.2s ease-in;
  transform-origin: center right;
}

.link:hover .link-title,
.link:focus .link-title,
.link.router-link-exact-active .link-title {
  transform: translateX(0);
  opacity: 1;
}

.link-icon {
  width: 28px;
  height: 28px;
  flex-shrink: 0;
  z-index: 1;
}

/* Móviles */
@media (max-width: 480px) {
  .header {
    font-size: 11px;
    height: 32px;
    padding: 2px 6px;
  }

  .usuario {
    font-size: 10px;
    max-width: 70%;
  }

  .logout svg {
    width: 18px;
    height: 18px;
  }

  .menu {
    gap: 4px;
    padding: 6px;
  }

  .link {
    width: 40px;
    height: 40px;
    padding-left: 8px;
  }

  .link:hover {
    width: 100px;
  }

  .link-icon {
    width: 24px;
    height: 24px;
  }

  .emoji-icon {
    width: 24px !important;
    height: 24px !important;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 20px;
  }

  .link-title {
    font-size: 10px;
  }
  .logo {
    width: 20px;
    height: 20px;
  }
}

@media (min-width: 480px) {
  .header-menu-container {
    max-width: 100%;
  }

  .header {
    font-size: 15px;
    height: 48px;
    padding: 8px 20px;
  }

  .usuario {
    font-size: 14px;
  }

  .menu {
    gap: 16px;
    padding: 12px 24px;
  }

  .link {
    width: 90px;
    height: 60px;
  }

  .link:hover {
    width: 160px;
  }

  .link-icon {
    width: 32px;
    height: 32px;
  }

  .emoji-icon {
    width: 32px !important;
    height: 32px !important;
    font-size: 24px;
  }

  .link-title {
    font-size: 14px;
  }
}
</style>
