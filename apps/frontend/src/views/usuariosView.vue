<template>
  <div class="users-management">
    <div class="users-header">
      <h1>👥 Gestión de Usuarios</h1>
      <div class="header-actions">
        <button @click="syncUsers" class="btn-primary" :disabled="syncing">
          <span v-if="syncing">🔄 Sincronizando...</span>
          <span v-else">🔄 Sincronizar</span>
        </button>
        <button @click="refreshData" class="btn-secondary">
          ↻ Actualizar
        </button>
      </div>
    </div>

    <!-- Estadísticas -->
    <div class="stats-grid">
      <div class="stat-card">
        <h3>Total Usuarios</h3>
        <p class="stat-number">{{ totalUsers }}</p>
      </div>
      <div class="stat-card">
        <h3>Administradores</h3>
        <p class="stat-number admin">{{ adminCount }}</p>
      </div>
      <div class="stat-card">
        <h3>Profesores</h3>
        <p class="stat-number profesor">{{ profesorCount }}</p>
      </div>
      <div class="stat-card">
        <h3>Activos</h3>
        <p class="stat-number active">{{ activeCount }}</p>
      </div>
    </div>

    <!-- Filtros -->
    <div class="filters">
      <input 
        v-model="searchTerm" 
        type="text" 
        placeholder="🔍 Buscar por nombre o email..."
        class="search-input"
      />
      <select v-model="filterRole" class="role-filter">
        <option value="">Todos los roles</option>
        <option value="admin">Administradores</option>
        <option value="profesor">Profesores</option>
      </select>
      <select v-model="filterStatus" class="status-filter">
        <option value="">Todos los estados</option>
        <option value="true">Activos</option>
        <option value="false">Inactivos</option>
      </select>
    </div>

    <!-- Tabla de usuarios -->
    <div class="users-table-container">
      <table class="users-table">
        <thead>
          <tr>
            <th>Email</th>
            <th>Nombre</th>
            <th>Rol</th>
            <th>Estado</th>
            <th>Fecha Registro</th>
            <th>Acciones</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="user in filteredUsers" :key="user.id" :class="{ 'inactive-row': !user.activo }">
            <td class="email-cell">{{ user.email }}</td>
            <td>
              <input 
                v-if="editingUser === user.id" 
                v-model="editForm.nombre"
                type="text"
                class="edit-input"
                @keyup.enter="saveUser(user.id)"
                @keyup.esc="cancelEdit"
              />
              <span v-else>{{ user.nombre }}</span>
            </td>
            <td>
              <select 
                v-if="editingUser === user.id"
                v-model="editForm.rol"
                class="edit-select"
              >
                <option value="profesor">Profesor</option>
                <option value="admin">Administrador</option>
              </select>
              <span v-else :class="['role-badge', user.rol]">
                {{ user.rol === 'admin' ? 'Admin' : 'Profesor' }}
              </span>
            </td>
            <td>
              <button 
                @click="toggleUserStatus(user)"
                :class="['status-btn', user.activo ? 'active' : 'inactive']"
              >
                {{ user.activo ? '✅ Activo' : '❌ Inactivo' }}
              </button>
            </td>
            <td class="date-cell">
              {{ formatDate(user.created_at) }}
            </td>
            <td class="actions-cell">
              <div class="action-buttons">
                <button 
                  v-if="editingUser !== user.id"
                  @click="startEdit(user)"
                  class="btn-edit"
                  title="Editar usuario"
                >
                  ✏️
                </button>
                <template v-else>
                  <button 
                    @click="saveUser(user.id)"
                    class="btn-save"
                    title="Guardar cambios"
                  >
                    ✅
                  </button>
                  <button 
                    @click="cancelEdit"
                    class="btn-cancel"
                    title="Cancelar"
                  >
                    ❌
                  </button>
                </template>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Estado vacío -->
    <div v-if="filteredUsers.length === 0" class="empty-state">
      <p>No se encontraron usuarios que coincidan con los filtros</p>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="loading">
      <p>Cargando usuarios...</p>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { supabase } from '@/supabaseClient';
import { useToast } from 'vue-toastification';

const toast = useToast();

// Estados reactivos
const users = ref([]);
const loading = ref(true);
const syncing = ref(false);
const editingUser = ref(null);
const editForm = ref({ nombre: '', rol: '' });

// Filtros
const searchTerm = ref('');
const filterRole = ref('');
const filterStatus = ref('');

// Estadísticas computadas
const totalUsers = computed(() => users.value.length);
const adminCount = computed(() => users.value.filter(u => u.rol === 'admin').length);
const profesorCount = computed(() => users.value.filter(u => u.rol === 'profesor').length);
const activeCount = computed(() => users.value.filter(u => u.activo).length);

// Usuarios filtrados
const filteredUsers = computed(() => {
  let filtered = users.value;

  // Filtro por término de búsqueda
  if (searchTerm.value) {
    const term = searchTerm.value.toLowerCase();
    filtered = filtered.filter(user => 
      user.email.toLowerCase().includes(term) ||
      user.nombre.toLowerCase().includes(term)
    );
  }

  // Filtro por rol
  if (filterRole.value) {
    filtered = filtered.filter(user => user.rol === filterRole.value);
  }

  // Filtro por estado
  if (filterStatus.value !== '') {
    const isActive = filterStatus.value === 'true';
    filtered = filtered.filter(user => user.activo === isActive);
  }

  return filtered;
});

// Cargar usuarios
const loadUsers = async () => {
  try {
    loading.value = true;
    const { data, error } = await supabase
      .from('usuarios')
      .select('*')
      .order('created_at', { ascending: false });

    if (error) throw error;

    users.value = data || [];
  } catch (error) {
    toast.error('Error cargando usuarios: ' + error.message);
  } finally {
    loading.value = false;
  }
};

// Sincronizar usuarios
const syncUsers = async () => {
  try {
    syncing.value = true;
    toast.info('Sincronizando usuarios...');
    
    // Obtener usuarios de auth.users
    const { data: authUsers, error: authError } = await supabase.auth.admin.listUsers();
    
    if (authError) {
      throw new Error('Error obteniendo usuarios de autenticación');
    }

    // Obtener usuarios existentes
    const existingEmails = users.value.map(u => u.email);
    const usersToSync = authUsers.users.filter(user => 
      user.email && !existingEmails.includes(user.email)
    );

    if (usersToSync.length === 0) {
      toast.info('Todos los usuarios ya están sincronizados');
      return;
    }

    // Insertar usuarios faltantes
    const newUsers = usersToSync.map(user => ({
      auth_id: user.id,
      email: user.email,
      nombre: user.user_metadata?.full_name || user.email.split('@')[0],
      rol: 'profesor',
      activo: true
    }));

    const { error: insertError } = await supabase
      .from('usuarios')
      .insert(newUsers);

    if (insertError) throw insertError;

    toast.success(`${usersToSync.length} usuarios sincronizados`);
    await loadUsers(); // Recargar la lista
    
  } catch (error) {
    toast.error(`Error sincronizando: ${error.message}`);
  } finally {
    syncing.value = false;
  }
};

// Edición de usuarios
const startEdit = (user) => {
  editingUser.value = user.id;
  editForm.value = {
    nombre: user.nombre,
    rol: user.rol
  };
};

const cancelEdit = () => {
  editingUser.value = null;
  editForm.value = { nombre: '', rol: '' };
};

const saveUser = async (userId) => {
  try {
    const { error } = await supabase
      .from('usuarios')
      .update({
        nombre: editForm.value.nombre,
        rol: editForm.value.rol
      })
      .eq('id', userId);

    if (error) throw error;

    toast.success('Usuario actualizado correctamente');
    cancelEdit();
    await loadUsers();
  } catch (error) {
    toast.error('Error actualizando usuario: ' + error.message);
  }
};

// Cambiar estado del usuario
const toggleUserStatus = async (user) => {
  try {
    const newStatus = !user.activo;
    const { error } = await supabase
      .from('usuarios')
      .update({ activo: newStatus })
      .eq('id', user.id);

    if (error) throw error;

    toast.success(`Usuario ${newStatus ? 'activado' : 'desactivado'}`);
    await loadUsers();
  } catch (error) {
    toast.error('Error cambiando estado: ' + error.message);
  }
};

// Actualizar datos
const refreshData = () => {
  loadUsers();
};

// Formatear fecha
const formatDate = (dateString) => {
  if (!dateString) return '-';
  return new Date(dateString).toLocaleDateString('es-ES', {
    year: 'numeric',
    month: 'short',
    day: 'numeric'
  });
};

// Inicialización
onMounted(() => {
  loadUsers();
});
</script>

<style scoped>
.users-management {
  max-width: 1400px;
  margin: 0 auto;
  padding: 20px;
  font-family: 'Montserrat', sans-serif;
}

.users-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

.users-header h1 {
  color: #1F86A1;
  font-size: 2rem;
  margin: 0;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.stat-card {
  background: white;
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  text-align: center;
}

.stat-card h3 {
  margin: 0 0 10px 0;
  color: #666;
  font-size: 14px;
  text-transform: uppercase;
}

.stat-number {
  font-size: 2rem;
  font-weight: 600;
  margin: 0;
  color: #1F86A1;
}

.stat-number.admin { color: #ff6b35; }
.stat-number.profesor { color: #4DB8D0; }
.stat-number.active { color: #28a745; }

.filters {
  display: flex;
  gap: 15px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.search-input, .role-filter, .status-filter {
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
}

.search-input {
  flex: 1;
  min-width: 250px;
}

.users-table-container {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.users-table {
  width: 100%;
  border-collapse: collapse;
}

.users-table th {
  background: #f8f9fa;
  padding: 15px;
  text-align: left;
  font-weight: 600;
  color: #333;
  border-bottom: 1px solid #dee2e6;
}

.users-table td {
  padding: 15px;
  border-bottom: 1px solid #eee;
}

.inactive-row {
  opacity: 0.6;
  background: #f8f9fa;
}

.email-cell {
  font-family: monospace;
  font-size: 13px;
}

.role-badge {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  text-transform: uppercase;
}

.role-badge.admin {
  background: #ff6b35;
  color: white;
}

.role-badge.profesor {
  background: #4DB8D0;
  color: white;
}

.status-btn {
  padding: 6px 12px;
  border: none;
  border-radius: 6px;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s;
}

.status-btn.active {
  background: #d4edda;
  color: #155724;
}

.status-btn.inactive {
  background: #f8d7da;
  color: #721c24;
}

.action-buttons {
  display: flex;
  gap: 5px;
}

.btn-edit, .btn-save, .btn-cancel {
  padding: 5px 8px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
}

.btn-edit { background: #e9ecef; }
.btn-save { background: #d4edda; color: #155724; }
.btn-cancel { background: #f8d7da; color: #721c24; }

.edit-input, .edit-select {
  width: 100%;
  padding: 5px;
  border: 1px solid #ddd;
  border-radius: 4px;
}

.empty-state, .loading {
  text-align: center;
  padding: 40px;
  color: #666;
}

.btn-primary, .btn-secondary {
  padding: 10px 20px;
  border: none;
  border-radius: 6px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-primary {
  background: linear-gradient(90deg, #1F86A1, #4DB8D0);
  color: white;
}

.btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-secondary {
  background: #f8f9fa;
  color: #495057;
  border: 1px solid #dee2e6;
}

.date-cell {
  font-size: 13px;
  color: #666;
}

@media (max-width: 768px) {
  .users-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }

  .filters {
    flex-direction: column;
  }

  .search-input {
    min-width: auto;
  }

  .users-table-container {
    overflow-x: auto;
  }

  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
