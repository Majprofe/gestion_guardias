import { createApp } from "vue";
import { createPinia } from "pinia";
import App from "./App.vue";
import router from "./router";
import { useUserStore } from "./stores/user";

import Toast, { POSITION } from "vue-toastification";
import "vue-toastification/dist/index.css";

const app = createApp(App);
const pinia = createPinia();

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

app.use(pinia);
app.use(router);
app.use(Toast, options); 

// Inicializar autenticación después de crear la app
const userStore = useUserStore();
userStore.initializeAuth();

app.mount("#app");
