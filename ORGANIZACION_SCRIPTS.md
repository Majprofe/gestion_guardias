# 📁 Organización de Scripts - Gestión de Guardias

## 🗂️ Nueva Estructura de Archivos

```
gestion_guardias/
├── scripts/                    # 📂 Scripts de desarrollo organizados
│   ├── dev.ps1                # ⚡ Inicio rápido (implementación)
│   ├── start-dev-simple.ps1   # 📋 Inicio completo con logging
│   ├── stop-dev.ps1           # 🛑 Detener servicios
│   ├── status-simple.ps1      # 📊 Estado del sistema
│   └── ...                    # Otros scripts auxiliares
├── dev.ps1                    # 🔗 Acceso directo → scripts/dev.ps1
├── start.ps1                  # 🔗 Acceso directo → scripts/start-dev-simple.ps1
├── stop.ps1                   # 🔗 Acceso directo → scripts/stop-dev.ps1
├── status.ps1                 # 🔗 Acceso directo → scripts/status-simple.ps1
└── apps/                      # 💻 Código de las aplicaciones
    ├── frontend/              # Vue.js + Vite
    ├── backend/               # Spring Boot Guardias
    └── horarios/              # Spring Boot Horarios
```

## 🚀 Comandos Simplificados (Desde el Directorio Raíz)

### ⚡ **Uso Diario - Comandos Simples**
```powershell
# Inicio rápido de todos los servicios
.\dev.ps1

# Ver estado del sistema
.\status.ps1

# Detener todos los servicios  
.\stop.ps1

# Inicio completo (primera vez o problemas)
.\start.ps1
```

### 🔧 **Comandos Avanzados**
```powershell
# Inicio selectivo de servicios
.\start.ps1 frontend          # Solo frontend
.\start.ps1 backend           # Solo backend guardias  
.\start.ps1 horarios          # Solo backend horarios
.\start.ps1 stop              # Detener todo
.\start.ps1 help              # Ayuda
```

### 📂 **Acceso Directo a Scripts**
```powershell
# Si necesitas acceder directamente a los scripts:
.\scripts\dev.ps1
.\scripts\start-dev-simple.ps1 all
.\scripts\stop-dev.ps1
.\scripts\status-simple.ps1
```

## 🎯 **Ventajas de la Nueva Organización**

### ✅ **Beneficios:**
- **📁 Organización**: Scripts agrupados en carpeta dedicada
- **🔗 Acceso Fácil**: Scripts de acceso directo en raíz
- **🧹 Directorio Limpio**: Raíz menos saturado
- **🔧 Mantenimiento**: Más fácil gestionar scripts
- **📚 Documentación**: Estructura más clara

### 🚀 **Flujo de Trabajo Mejorado:**

1. **Trabajar desde la raíz**: Todos los comandos desde el directorio principal
2. **Comandos cortos**: `.\dev.ps1` en lugar de `.\scripts\dev.ps1`
3. **Consistencia**: Misma funcionalidad, mejor organización
4. **Escalabilidad**: Fácil añadir nuevos scripts sin saturar

## 📋 **Migración Completada**

### ✅ **Archivos Reorganizados:**
- ✅ `dev.ps1` → `scripts/dev.ps1` + acceso directo
- ✅ `start-dev-simple.ps1` → `scripts/start-dev-simple.ps1` + acceso directo  
- ✅ `stop-dev.ps1` → `scripts/stop-dev.ps1` + acceso directo
- ✅ `status-simple.ps1` → `scripts/status-simple.ps1` + acceso directo

### 🔗 **Scripts de Acceso Directo Creados:**
- ✅ `dev.ps1` - Inicio rápido
- ✅ `start.ps1` - Inicio completo con parámetros
- ✅ `stop.ps1` - Detener servicios  
- ✅ `status.ps1` - Estado del sistema

## 🌐 **URLs de Desarrollo (Sin Cambios)**

- **Frontend**: http://localhost:5500
- **API Guardias**: http://localhost:8081/swagger-ui.html
- **API Horarios**: http://localhost:8082/swagger-ui.html

## 💡 **Uso Recomendado**

```powershell
# Inicio de jornada
.\status.ps1        # Ver estado actual
.\dev.ps1           # Iniciar desarrollo rápido

# Durante desarrollo  
.\status.ps1        # Verificar servicios

# Final de jornada
.\stop.ps1          # Detener todo
```

**¡Organización completada! Ahora tienes una estructura más limpia y profesional.** 🎉
