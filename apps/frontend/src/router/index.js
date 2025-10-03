import { createRouter, createWebHistory } from 'vue-router';
import { supabase } from '@/supabaseClient';
import HomeView from '@/views/homeView.vue';
import HistoricoView from '@/views/historicoView.vue';
import GestionFaltas from '@/views/gestionFaltasView.vue';
import FormularioView from '@/views/formularioView.vue';
import LoginView from '@/views/loginView.vue';
import MisGuardiasView from "@/views/misGuardiasView.vue";
import horarioView from '@/views/horarioView.vue';
import adminPanelView from '@/views/adminPanelView.vue';
import NotFoundView from '@/views/NotFoundView.vue';
import authCallbackView from '@/views/authCallbackView.vue';
import estadisticasView from '@/views/estadisticasView.vue';
import usuariosView from '@/views/usuariosView.vue';

const routes = [
  // Rutas públicas
  { path: '/login', name: "login", component: LoginView },
  { path: '/auth/callback', name: "auth-callback", component: authCallbackView },
  
  // Rutas para profesores (requieren autenticación)
  { path: '/', name: "home", component: HomeView, meta: { requiresAuth: true } },
  { path: '/formulario', name: "formulario", component: FormularioView, meta: { requiresAuth: true } },
  { path: '/mis-guardias', name: "MisGuardias", component: MisGuardiasView, meta: { requiresAuth: true } },
  { path: '/horario', name: "horario", component: horarioView, meta: { requiresAuth: true } },
  { path: '/gestion-faltas', name: "gestion-faltas", component: GestionFaltas, meta: { requiresAuth: true } },
  
  // Rutas para administradores (requieren rol admin)
  { path: '/admin', name: "admin-panel", component: adminPanelView, meta: { requiresAuth: true, adminOnly: true } },
  { path: '/historico', name: "historico", component: HistoricoView, meta: { requiresAuth: true, adminOnly: true } },
  { path: '/estadisticas', name: "estadisticas", component: estadisticasView, meta: { requiresAuth: true, adminOnly: true } },
  { path: '/usuarios', name: "usuarios", component: usuariosView, meta: { requiresAuth: true, adminOnly: true } },
  
  // Ruta de error 404
  { path: '/:pathMatch(.*)*', name: "NotFound", component: NotFoundView },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

router.beforeEach(async (to, from, next) => {
  const { data: { session } } = await supabase.auth.getSession();
  const isAuthenticated = !!session;
  
  // Si no está autenticado y la ruta requiere autenticación
  if (to.meta.requiresAuth && !isAuthenticated) {
    return next('/login');
  }

  // Si está autenticado y intenta acceder al login, redirigir según su rol
  if (to.path === '/login' && isAuthenticated) {
    const userRole = localStorage.getItem("userRole");
    if (userRole === 'admin') {
      return next('/admin');
    } else {
      return next('/');
    }
  }

  // Si la ruta requiere autenticación, verificar validez del usuario
  if (to.meta.requiresAuth && isAuthenticated) {
    const userRole = localStorage.getItem("userRole");
    const userEmail = localStorage.getItem("userEmail");
    
    // Si no hay información de rol en localStorage, intentar obtenerla de la base de datos
    if (!userRole && session?.user?.email) {
      try {
        const { data: userData, error } = await supabase
          .from("usuarios")
          .select("rol, nombre, activo")
          .eq("email", session.user.email)
          .eq("activo", true)
          .single();

        if (userData) {
          // Actualizar localStorage con la información del usuario
          localStorage.setItem("userRole", userData.rol);
          localStorage.setItem("userName", userData.nombre);
          localStorage.setItem("userEmail", session.user.email);
          
          // Verificar si la ruta requiere admin
          if (to.meta.adminOnly && userData.rol !== 'admin') {
            return next('/');
          }
        } else {
          // Usuario no válido, cerrar sesión y redirigir al login
          await supabase.auth.signOut();
          localStorage.clear();
          return next('/login');
        }
      } catch (error) {
        console.error("Error verificando usuario:", error);
        return next('/login');
      }
    } else if (to.meta.adminOnly && userRole !== 'admin') {
      // Verificar si la ruta requiere admin y el usuario no es admin
      return next('/');
    }
  }

  next(); // Permitir la navegación
});


export default router;
