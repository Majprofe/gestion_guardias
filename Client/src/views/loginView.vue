<template>
  <div class="login-container">
    <img src="../assets/logo.jpg" alt="Logo" class="logo" />
    <form @submit.prevent="login">
      <h2>Gestion Horarios Profesorado</h2>
      <input type="email" v-model="email" required placeholder="Correo corporativo" />
      <input type="password" v-model="password" required placeholder="Tu contraseña" />
      <button type="submit">Entrar</button>
    </form>
    <p v-if="error" class="error-message">{{ error }}</p>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import { supabase } from "@/supabaseClient";
import { useToast } from "vue-toastification";

const toast = useToast();

const email = ref("");
const password = ref("");
const error = ref("");
const router = useRouter();

const login = async () => {
  error.value = ""; 

  const { data, error: loginError } = await supabase.auth.signInWithPassword({
    email: email.value,
    password: password.value,
  });

  if (loginError) {
    toast.error("Error al iniciar sesión. Verifica tus credenciales.");
    return;
  }
  const userEmail = email.value;
  
  const { data: roleData, error: roleError } = await supabase
    .from("usuarios") 
    .select("rol")
    .eq("email", userEmail)
    .single(); 

  if (roleError) {
    toast.error("Error al obtener el rol del usuario.");
    return;
  }

  localStorage.setItem("userRole", roleData?.rol);
  localStorage.setItem("userEmail", userEmail);

  router.push("/");
};

//Esto sirve para conservar la sesion al recargar la pagina
onMounted(async () => {
  const { data: { session } } = await supabase.auth.getSession();

  if (session?.user) {
    router.push("/");
  }
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

html,
body {
  height: 100%;
  background: #e6f7fa;
}

.login-container {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  height: 100vh;
  padding: 20px;
  background: linear-gradient(135deg, #f5fafa, #d2eef3);
}

form {
  width: 100%;
  max-width: 350px;
  background-color: white;
  padding: 25px;
  border-radius: 12px;
  box-shadow: 0px 4px 10px rgba(0, 0, 0, 0.1);
}

.logo {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  margin: 0 auto 15px auto;
  display: block;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
}

h2 {
  text-align: center;
  color: #1F86A1;
  margin-bottom: 20px;
  font-size: 20px;
  font-weight: 600;
}

label {
  font-size: 14px;
  color: #333;
  margin-top: 10px;
  display: block;
}

input {
  width: 100%;
  padding: 10px 12px;
  margin-top: 5px;
  margin-bottom: 15px;
  border: 2px solid #ccc;
  border-radius: 6px;
  font-size: 14px;
  transition: border-color 0.3s;
}

input:focus {
  border-color: #1F86A1;
  outline: none;
}

button {
  width: 50%;
  padding: 12px;
  background: linear-gradient(90deg, #1F86A1, #4DB8D0);
  color: white;
  border: none;
  border-radius: 25px;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.3s ease, box-shadow 0.3s ease;
  margin-top: 5px;
  margin-left: 25%;
}

button:hover {
  background: linear-gradient(90deg, #197a90, #3fa7be);
  box-shadow: 0px 5px 10px rgba(31, 134, 161, 0.4);
}

.error-message {
  color: #d9534f;
  text-align: center;
  margin-top: 10px;
  font-size: 14px;
  font-weight: 500;
}

@media (max-width: 480px) {
  .login-container {
    padding: 10px;
  }

  form {
    padding: 20px;
  }

  h2 {
    font-size: 18px;
  }

  label {
    font-size: 13px;
  }

  input {
    font-size: 13px;
    padding: 10px;
  }

  button {
    font-size: 14px;
    padding: 10px;
  }
}
</style>
