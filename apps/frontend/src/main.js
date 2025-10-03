import { createApp } from "vue";
import { createPinia } from "pinia";
import App from "./App.vue";
import router from "./router";
import { useAuth } from "@/composables/useAuth";

import Toast, { POSITION } from "vue-toastification";
import "vue-toastification/dist/index.css";

const app = createApp(App);

// Configuración opcional del toast
const options = {
  position: POSITION.BOTTOM_CENTER,
  timeout: 2000,
  closeOnClick: true,
  pauseOnHover: true,
  draggable: true,
  showCloseButtonOnHover: false,
  hideProgressBar: false,
  closeButton: "button",
  icon: true,
};

app.use(router);
app.use(createPinia());
app.use(Toast, options); 

// Inicializar autenticación antes de montar la aplicación
const { initializeAuth } = useAuth();
initializeAuth().then(() => {
  app.mount("#app");
});
