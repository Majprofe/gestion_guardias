<template>
  <div class="admin-panel">
    <div class="admin-header">
      <h1>Panel de Administración</h1>
      <p class="admin-welcome">Bienvenido {{ userName }}, tienes permisos de administrador</p>
    </div>

    <div class="admin-grid">
      <!-- Gestión de Usuarios -->
      <div class="admin-card">
        <div class="card-header">
          <h2>👥 Gestión de Usuarios</h2>
        </div>
        <div class="card-content">
          <p>Administra los usuarios del sistema, roles y permisos</p>
          <div class="card-actions">
            <button @click="navigateTo('/usuarios')" class="btn-primary">
              Ver Usuarios
            </button>
            <button @click="refreshUsers" class="btn-secondary">
              Sincronizar
            </button>
          </div>
        </div>
      </div>

      <!-- Histórico de Ausencias -->
      <div class="admin-card">
        <div class="card-header">
          <h2>📊 Histórico de Ausencias</h2>
        </div>
        <div class="card-content">
          <p>Consulta el histórico completo de ausencias y guardias</p>
          <div class="card-actions">
            <button @click="navigateTo('/historico')" class="btn-primary">
              Ver Histórico
            </button>
          </div>
        </div>
      </div>

      <!-- Estadísticas -->
      <div class="admin-card">
        <div class="card-header">
          <h2>📈 Estadísticas</h2>
        </div>
        <div class="card-content">
          <p>Análisis y reportes del sistema de guardias</p>
          <div class="card-actions">
            <button @click="navigateTo('/estadisticas')" class="btn-primary">
              Ver Estadísticas
            </button>
          </div>
        </div>
      </div>

      <!-- Configuración del Sistema -->
      <div class="admin-card">
        <div class="card-header">
          <h2>⚙️ Configuración</h2>
        </div>
        <div class="card-content">
          <p>Configuración general del sistema</p>
          <div class="card-actions">
            <button @click="openConfigModal" class="btn-primary">
              Configurar
            </button>
          </div>
        </div>
      </div>

      <!-- Estado del Sistema -->
      <div class="admin-card">
        <div class="card-header">
          <h2>🔍 Estado del Sistema</h2>
        </div>
        <div class="card-content">
          <div class="system-status">
            <div class="status-item">
              <span class="status-label">API Backend:</span>
              <span :class="['status-indicator', backendStatus]">
                {{ backendStatus === 'online' ? 'Online' : 'Offline' }}
              </span>
            </div>
            <div class="status-item">
              <span class="status-label">API Horarios:</span>
              <span :class="['status-indicator', horariosStatus]">
                {{ horariosStatus === 'online' ? 'Online' : 'Offline' }}
              </span>
            </div>
            <div class="status-item">
              <span class="status-label">Base de Datos:</span>
              <span :class="['status-indicator', dbStatus]">
                {{ dbStatus === 'online' ? 'Conectada' : 'Desconectada' }}
              </span>
            </div>
          </div>
          <div class="card-actions">
            <button @click="checkSystemStatus" class="btn-secondary">
              Verificar Estado
            </button>
          </div>
        </div>
      </div>

      <!-- Accesos Rápidos -->
      <div class="admin-card">
        <div class="card-header">
          <h2>🚀 Accesos Rápidos</h2>
        </div>
        <div class="card-content">
          <div class="quick-actions">
            <button @click="navigateTo('/formulario')" class="quick-btn">
              ➕ Nueva Ausencia
            </button>
            <button @click="navigateTo('/gestion-faltas')" class="quick-btn">
              📋 Tablón de Faltas
            </button>
            <button @click="navigateTo('/horario')" class="quick-btn">
              📅 Ver Horarios
            </button>
            <button @click="exportData" class="quick-btn">
              📥 Exportar Datos
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Modal de Configuración -->
    <div v-if="showConfigModal" class="modal-overlay" @click="closeConfigModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>Configuración del Sistema</h3>
          <button @click="closeConfigModal" class="close-btn">&times;</button>
        </div>
        <div class="modal-body">
          <div class="config-section">
            <label>URL API Backend:</label>
            <input v-model="config.apiUrl" type="text" />
          </div>
          <div class="config-section">
            <label>URL API Horarios:</label>
            <input v-model="config.horariosUrl" type="text" />
          </div>
          <div class="config-section">
            <label>Timeout de sesión (minutos):</label>
            <input v-model="config.sessionTimeout" type="number" />
          </div>
        </div>
        <div class="modal-footer">
          <button @click="saveConfig" class="btn-primary">Guardar</button>
          <button @click="closeConfigModal" class="btn-secondary">Cancelar</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useAuth } from '@/composables/useAuth';
import { useToast } from 'vue-toastification';
import { supabase } from '@/supabaseClient';

const router = useRouter();
const toast = useToast();
const { userName } = useAuth();

// Estados reactivos
const backendStatus = ref('checking');
const horariosStatus = ref('checking');
const dbStatus = ref('checking');
const showConfigModal = ref(false);

// Configuración del sistema
const config = ref({
  apiUrl: import.meta.env.VITE_API_URL || 'http://localhost:8081',
  horariosUrl: 'http://localhost:8082', // Solo para referencia, ya no se usa directamente
  sessionTimeout: 60
});

// Métodos de navegación
const navigateTo = (path) => {
  router.push(path);
};

// Verificar estado del sistema
const checkSystemStatus = async () => {
  toast.info('Verificando estado del sistema...');
  
  // Verificar API Backend principal
  try {
    const response = await fetch(`${config.value.apiUrl}/health`, { 
      method: 'GET',
      timeout: 5000 
    });
    backendStatus.value = response.ok ? 'online' : 'offline';
  } catch (error) {
    backendStatus.value = 'offline';
  }

  // Verificar API Horarios (a través del proxy)
  try {
    const response = await fetch(`${config.value.apiUrl}/api/horarios/health`, { 
      method: 'GET',
      timeout: 5000 
    });
    horariosStatus.value = response.ok ? 'online' : 'offline';
  } catch (error) {
    horariosStatus.value = 'offline';
  }

  // Verificar Base de Datos (Supabase)
  try {
    const { data, error } = await supabase.from('usuarios').select('count').limit(1);
    dbStatus.value = error ? 'offline' : 'online';
  } catch (error) {
    dbStatus.value = 'offline';
  }

  toast.success('Estado del sistema actualizado');
};

// Sincronizar usuarios
const refreshUsers = async () => {
  try {
    toast.info('Sincronizando usuarios...');
    
    // Opción 1: Usar la función SQL directamente
    const { data: syncResult, error: syncError } = await supabase.rpc('sync_auth_users');
    
    if (syncError) {
      // Si la función RPC no existe, usar el método manual
      await manualSyncUsers();
    } else {
      toast.success(`✅ ${syncResult || 0} usuarios sincronizados correctamente`);
    }
    
  } catch (error) {
    toast.error(`Error al sincronizar usuarios: ${error.message}`);
    console.error('Error:', error);
  }
};

// Método manual de sincronización como respaldo
const manualSyncUsers = async () => {
  try {
    // Obtener todos los usuarios de auth
    const { data: { users: authUsers }, error: authError } = await supabase.auth.admin.listUsers();
    
    if (authError) {
      throw new Error(`Error obteniendo usuarios de auth: ${authError.message}`);
    }

    // Obtener usuarios existentes en la tabla usuarios
    const { data: existingUsers, error: existingError } = await supabase
      .from('usuarios')
      .select('email');
    
    if (existingError) {
      throw new Error(`Error obteniendo usuarios existentes: ${existingError.message}`);
    }

    const existingEmails = existingUsers.map(u => u.email);
    const usersToSync = authUsers.filter(user => 
      user.email && !existingEmails.includes(user.email)
    );

    if (usersToSync.length === 0) {
      toast.info('ℹ️ Todos los usuarios ya están sincronizados');
      return;
    }

    // Crear lista de usuarios para insertar
    const newUsers = usersToSync.map(user => ({
      auth_id: user.id,
      email: user.email,
      nombre: user.user_metadata?.full_name || 
              user.user_metadata?.name || 
              user.email.split('@')[0],
      rol: 'profesor', // Por defecto profesor
      activo: true
    }));

    // Insertar en lotes para evitar errores
    const { error: insertError } = await supabase
      .from('usuarios')
      .insert(newUsers);

    if (insertError) {
      throw new Error(`Error insertando usuarios: ${insertError.message}`);
    }

    toast.success(`✅ ${usersToSync.length} usuarios sincronizados correctamente`);
    
    // Mostrar detalles
    const userList = usersToSync.map(u => u.email).join(', ');
    console.log('Usuarios sincronizados:', userList);
    
  } catch (error) {
    throw error;
  }
};

// Modal de configuración
const openConfigModal = () => {
  showConfigModal.value = true;
};

const closeConfigModal = () => {
  showConfigModal.value = false;
};

const saveConfig = () => {
  // Guardar configuración en localStorage o en la base de datos
  localStorage.setItem('adminConfig', JSON.stringify(config.value));
  toast.success('Configuración guardada');
  closeConfigModal();
};

// Exportar datos
const exportData = () => {
  toast.info('Función de exportación en desarrollo');
  // Aquí podrías implementar la exportación de datos
};

// Inicialización
onMounted(() => {
  // Cargar configuración guardada
  const savedConfig = localStorage.getItem('adminConfig');
  if (savedConfig) {
    config.value = { ...config.value, ...JSON.parse(savedConfig) };
  }
  
  // Verificar estado inicial del sistema
  checkSystemStatus();
});
</script>

<style scoped>
.admin-panel {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  font-family: 'Montserrat', sans-serif;
}

.admin-header {
  text-align: center;
  margin-bottom: 30px;
}

.admin-header h1 {
  color: #1F86A1;
  font-size: 2.5rem;
  margin-bottom: 10px;
  font-weight: 600;
}

.admin-welcome {
  color: #666;
  font-size: 1.1rem;
  margin: 0;
}

.admin-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(350px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.admin-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.admin-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

.card-header {
  background: linear-gradient(135deg, #1F86A1, #4DB8D0);
  color: white;
  padding: 20px;
}

.card-header h2 {
  margin: 0;
  font-size: 1.3rem;
  font-weight: 600;
}

.card-content {
  padding: 20px;
}

.card-content p {
  color: #666;
  margin-bottom: 20px;
  line-height: 1.6;
}

.card-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.btn-primary, .btn-secondary {
  padding: 10px 20px;
  border: none;
  border-radius: 6px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  font-size: 14px;
}

.btn-primary {
  background: linear-gradient(90deg, #1F86A1, #4DB8D0);
  color: white;
}

.btn-primary:hover {
  background: linear-gradient(90deg, #197a90, #3fa7be);
  transform: translateY(-1px);
}

.btn-secondary {
  background: #f8f9fa;
  color: #495057;
  border: 1px solid #dee2e6;
}

.btn-secondary:hover {
  background: #e9ecef;
}

.system-status {
  margin-bottom: 20px;
}

.status-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #eee;
}

.status-item:last-child {
  border-bottom: none;
}

.status-label {
  font-weight: 500;
  color: #333;
}

.status-indicator {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  text-transform: uppercase;
}

.status-indicator.online {
  background: #d4edda;
  color: #155724;
}

.status-indicator.offline {
  background: #f8d7da;
  color: #721c24;
}

.status-indicator.checking {
  background: #fff3cd;
  color: #856404;
}

.quick-actions {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}

.quick-btn {
  padding: 12px;
  background: #f8f9fa;
  border: 1px solid #dee2e6;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s ease;
  font-size: 14px;
  text-align: center;
}

.quick-btn:hover {
  background: #e9ecef;
  transform: translateY(-1px);
}

/* Modal */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 12px;
  max-width: 500px;
  width: 90%;
  max-height: 80vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #eee;
}

.modal-header h3 {
  margin: 0;
  color: #1F86A1;
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #999;
}

.modal-body {
  padding: 20px;
}

.config-section {
  margin-bottom: 20px;
}

.config-section label {
  display: block;
  margin-bottom: 5px;
  font-weight: 500;
  color: #333;
}

.config-section input {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
}

.modal-footer {
  padding: 20px;
  border-top: 1px solid #eee;
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}

/* Responsive */
@media (max-width: 768px) {
  .admin-panel {
    padding: 15px;
  }

  .admin-header h1 {
    font-size: 2rem;
  }

  .admin-grid {
    grid-template-columns: 1fr;
    gap: 15px;
  }

  .card-header, .card-content {
    padding: 15px;
  }

  .quick-actions {
    grid-template-columns: 1fr;
  }

  .modal-content {
    width: 95%;
  }
}
</style>
