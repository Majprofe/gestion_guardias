# 🏫 Sistema de Gestión de Guardias

Sistema integral para la gestión de guardias escolares con autenticación Google Workspace.

## 🚀 Estructura del Proyecto

```
📦 gestion_guardias/
├── 📁 apps/                 # Aplicaciones principales
│   ├── 📱 frontend/         # Vue.js + Vite (Puerto 5500)
│   ├── 🚀 backend/          # Spring Boot API Guardias (Puerto 8081)
│   └── ⏰ horarios/         # Microservicio horarios (Puerto 8082)
├── 📁 infrastructure/       # Configuración infraestructura
├── 📄 .env                  # ⚡ Variables de entorno centralizadas
├── 📄 start-service.ps1     # 🛠️ Script de inicio automático
└── 📄 docker-compose.yml    # Orquestación contenedores
```

## 🛠️ Tecnologías

### Frontend
- **Vue.js 3** + **Vite** + **Pinia**
- **Supabase** para autenticación y BD
- **Google OAuth 2.0** para login
- **Element Plus** + **Tailwind CSS**

### Backend
- **Spring Boot 3.4.4** + **Java 21**
- **PostgreSQL** + **Supabase**
- **SpringDoc OpenAPI** (Swagger)
- **Spring Security** (deshabilitado para desarrollo)

### Base de Datos
- **Supabase PostgreSQL**
- **Row Level Security (RLS)**
- **Autenticación Google Workspace**

## 🚀 Inicio Rápido

### Prerrequisitos
- **Node.js 18+**
- **Java 21+**
- **Maven 3.6+**
- **PowerShell** (para script de inicio)

### ⚡ Instalación y Ejecución

#### 1️⃣ Configuración Inicial
```bash
# Clonar repositorio
git clone https://github.com/Majprofe/gestion_guardias.git
cd gestion_guardias

# Configurar variables de entorno
cp .env.example .env
# ✏️ Edita .env con tus credenciales reales
```

#### 2️⃣ Variables de Entorno (.env)
```env
# 🌐 SUPABASE
VITE_SUPABASE_URL=tu_supabase_url
VITE_SUPABASE_ANON_KEY=tu_supabase_anon_key

# 🗄️ DATABASE  
DATABASE_URL=jdbc:postgresql://...
DATABASE_USERNAME=postgres.xxx
DATABASE_PASSWORD=tu_password

# 🚀 API ENDPOINTS
VITE_API_URL=http://localhost:8081
VITE_PLATFORM_URL=http://localhost:8082

# 🔐 AUTENTICACIÓN
VITE_ALLOWED_DOMAIN=g.educaand.es
```

#### 3️⃣ Ejecución con Script Automático
```powershell
# 🚀 Iniciar todos los servicios
.\start-service.ps1

# 🎯 Iniciar servicios específicos
.\start-service.ps1 frontend    # Solo frontend
.\start-service.ps1 backend     # Solo backend guardias  
.\start-service.ps1 horarios    # Solo backend horarios
```

#### 4️⃣ Ejecución Manual
```bash
# Frontend (Terminal 1)
cd apps/frontend
npm install
npm run dev

# Backend Guardias (Terminal 2)  
cd apps/backend
./mvnw.cmd spring-boot:run

# Backend Horarios (Terminal 3)
cd apps/horarios
./mvnw.cmd spring-boot:run
```

## 🌐 URLs de Acceso

| Servicio | URL | Descripción |
|----------|-----|-------------|
| 🌐 **Frontend** | https://localhost:5500 | Aplicación Vue.js |
| 🔧 **API Guardias** | http://localhost:8081/swagger-ui.html | Documentación Swagger |
| ⏰ **API Horarios** | http://localhost:8082/swagger-ui.html | Documentación Swagger |

## 🔐 Autenticación

- **Google OAuth 2.0** con dominio `@g.educaand.es`
- **Supabase Auth** para gestión de sesiones
- **Autenticación automática** de profesores
- **Control de roles** (admin/profesor)

## 📁 Configuración Centralizada

### ✅ Ventajas del .env centralizado:
- 🎯 **Una sola fuente** de verdad para variables
- 🔄 **Sincronización automática** entre servicios  
- 🛡️ **Seguridad mejorada** (un solo archivo a proteger)
- 🚀 **Fácil despliegue** y configuración

### 📋 Variables por Servicio:
- **Frontend**: Variables con prefijo `VITE_`
- **Backend**: Variables sin prefijo
- **Ambos**: Comparten credenciales de Supabase

## 🛠️ Desarrollo

### Scripts Disponibles
```json
{
  "dev:frontend": "cd apps/frontend && npm run dev",
  "dev:backend": "cd apps/backend && ./mvnw.cmd spring-boot:run", 
  "dev:horarios": "cd apps/horarios && ./mvnw.cmd spring-boot:run",
  "build:frontend": "cd apps/frontend && npm run build"
}
```

### Estructura de Carpetas
```
frontend/
├── src/
│   ├── components/     # Componentes reutilizables
│   ├── views/         # Páginas principales  
│   ├── stores/        # Estado global (Pinia)
│   ├── services/      # APIs y servicios
│   └── router/        # Enrutamiento
└── vite.config.js     # ⚡ Lee .env desde raíz

backend/
├── src/main/java/
│   ├── controller/    # Controladores REST
│   ├── service/       # Lógica de negocio
│   ├── model/         # Entidades JPA
│   └── config/        # Configuración CORS
```

## 📚 Documentación API

- **Swagger UI Guardias**: http://localhost:8081/swagger-ui.html
- **Swagger UI Horarios**: http://localhost:8082/swagger-ui.html  
- **OpenAPI JSON**: http://localhost:8081/v3/api-docs

## 🐛 Solución de Problemas

### ❌ Problemas Comunes:

1. **Puerto ocupado**: `taskkill /F /IM java.exe`
2. **Variables no cargadas**: Verificar `.env` en raíz
3. **CORS errors**: Verificar `WebConfig.java`
4. **Auth Google**: Configurar redirect URI en Google Console

### � Logs y Debug:
```bash
# Ver logs del backend
tail -f apps/backend/logs/application.log

# Debug del frontend  
# Abrir DevTools en https://localhost:5500
```

## �👥 Contribución

1. **Fork** del proyecto
2. Crear **rama feature**: `git checkout -b feature/nueva-funcionalidad`
3. **Commit** cambios: `git commit -m 'Add nueva funcionalidad'`
4. **Push** a la rama: `git push origin feature/nueva-funcionalidad`  
5. Crear **Pull Request**

## 📄 Licencia

MIT License - ver [LICENSE](LICENSE) para detalles.

---

💡 **Tip**: Usa `.\start-service.ps1` para una experiencia de desarrollo óptima!