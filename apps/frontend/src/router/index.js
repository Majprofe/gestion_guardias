import { createRouter, createWebHistory } from 'vue-router';
import { useUserStore } from '@/stores/user';
import HomeView from '@/views/homeView.vue';
import HistoricoView from '@/views/historicoView.vue';
import GestionFaltas from '@/views/gestionFaltasView.vue';
import FormularioView from '@/views/formularioView.vue';
import LoginView from '@/views/loginView.vue';
import MisGuardiasView from "@/views/misGuardiasView.vue";
import horarioView from '@/views/horarioView.vue';
import AuthCallbackView from '@/views/authCallbackView.vue';

const routes = [
  { path: '/', name: "home", component: HomeView, meta: { requiresAuth: true } },
  { path: '/historico', name: "historico", component: HistoricoView, meta: { requiresAuth: true, adminOnly: true } },
  { path: '/gestion-faltas', name: "gestion-faltas", component: GestionFaltas, meta: { requiresAuth: true} },
  { path: '/formulario', name: "formulario", component: FormularioView, meta: { requiresAuth: true } },
  { path: '/mis-guardias', name: "MisGuardias", component: MisGuardiasView, meta: { requiresAuth: true } },
  { path: '/login', name: "login", component: LoginView },
  { path: '/horario', name: "horario", component: horarioView, meta: { requiresAuth: true } },
  { path: '/auth/callback', name: "auth-callback", component: AuthCallbackView },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

router.beforeEach(async (to, from, next) => {
  const userStore = useUserStore();
  
  // Inicializar autenticación si no se ha hecho
  if (!userStore.loading) {
    await userStore.initializeAuth();
  }

  const isAuthenticated = userStore.isAuthenticated;
  const isAdmin = userStore.getIsAdmin;

  // Permitir acceso a la página de callback sin verificaciones adicionales
  if (to.path === '/auth/callback') {
    return next();
  }

  // Redirigir al login si no tiene sesión y la ruta requiere autenticación
  if (to.meta.requiresAuth && !isAuthenticated) {
    return next('/login');
  }

  // Redirigir a la página de inicio si ya está autenticado y se intenta acceder a login
  if (to.path === '/login' && isAuthenticated) {
    return next('/');
  }

  // Verificar si la ruta requiere rol de admin y si el usuario no es admin
  if (to.meta.adminOnly && !isAdmin) {
    return next('/'); // Redirigir a la página de inicio
  }

  next(); // Permitir la navegación si pasa todas las verificaciones
});


export default router;
