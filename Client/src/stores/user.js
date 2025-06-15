// stores/user.js
import { defineStore } from 'pinia';
import { ref } from 'vue';

export const useUserStore = defineStore('user', () => {
    const nombreUsuario = ref(null);
    const isAdmin = ref(false);

    const setUser = (email) => {
        nombreUsuario.value = email;
        isAdmin.value = email === "nikola.newtontesla@instituto.edu";
    };

    const resetUser = () => {
        nombreUsuario.value = 'Sin usuario';
        isAdmin.value = false;
    };

    return { nombreUsuario, isAdmin, setUser, resetUser };
});
