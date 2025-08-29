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
import EstadisticasView from '@/views/estadisticasView.vue';
import AdminPanelView from '@/views/adminPanelView.vue';

const routes = [
  { 
    path: '/', 
    name: "home", 
    component: HomeView, 
    meta: { 
      requiresAuth: true,
      title: '🏠 Inicio - Sistema de Gestión de Guardias'
    } 
  },
  { 
    path: '/historico', 
    name: "historico", 
    component: HistoricoView, 
    meta: { 
      requiresAuth: true, 
      adminOnly: true,
      title: '📈 Histórico de Guardias'
    } 
  },
  { 
    path: '/gestion-faltas', 
    name: "gestion-faltas", 
    component: GestionFaltas, 
    meta: { 
      requiresAuth: true,
      title: '📋 Gestión de Faltas'
    } 
  },
  { 
    path: '/formulario', 
    name: "formulario", 
    component: FormularioView, 
    meta: { 
      requiresAuth: true,
      title: '📝 Solicitar Ausencia'
    } 
  },
  { 
    path: '/mis-guardias', 
    name: "MisGuardias", 
    component: MisGuardiasView, 
    meta: { 
      requiresAuth: true,
      title: '🛡️ Mis Guardias'
    } 
  },
  { 
    path: '/login', 
    name: "login", 
    component: LoginView,
    meta: {
      title: '🔐 Iniciar Sesión'
    }
  },
  { 
    path: '/horario', 
    name: "horario", 
    component: horarioView, 
    meta: { 
      requiresAuth: true,
      title: '📅 Mi Horario'
    } 
  },
  { 
    path: '/estadisticas', 
    name: "estadisticas", 
    component: EstadisticasView, 
    meta: { 
      requiresAuth: true, 
      adminOnly: true,
      title: '📊 Dashboard de Estadísticas'
    } 
  },
  { 
    path: '/admin', 
    name: "admin-panel", 
    component: AdminPanelView, 
    meta: { 
      requiresAuth: true, 
      adminOnly: true,
      title: '🛠️ Panel de Administración'
    } 
  },
  { 
    path: '/auth/callback', 
    name: "auth-callback", 
    component: AuthCallbackView,
    meta: {
      title: '🔄 Verificando autenticación...'
    }
  },
  // Ruta de captura para 404
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/NotFoundView.vue'),
    meta: { 
      requiresAuth: false,
      title: '❌ Página no encontrada'
    }
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(to, from, savedPosition) {
    // Siempre scroll al top al cambiar de página
    if (savedPosition) {
      return savedPosition
    } else {
      return { top: 0 }
    }
  }
});

router.beforeEach(async (to, from, next) => {
  const userStore = useUserStore();
  
  // Actualizar título de la página
  if (to.meta.title) {
    document.title = to.meta.title
  }
  
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
    console.warn('Acceso denegado: se requieren permisos de administrador')
    return next('/'); // Redirigir a la página de inicio
  }

  next(); // Permitir la navegación si pasa todas las verificaciones
});

// Guard de navegación después de cada ruta
router.afterEach((to, from) => {
  // Log de navegación para debugging
  if (process.env.NODE_ENV === 'development') {
    console.log(`Navegando de ${from.name || 'desconocido'} a ${to.name || 'desconocido'}`)
  }
});

export default router;
