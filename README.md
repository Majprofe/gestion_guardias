# 🏫 Sistema de Gestión de Guardias

Sistema integral para la gestión de guardias escolares del IES Jándula.

## 🚀 Estructura del Proyecto

```
📦 gestion_guardias/
├── 📁 apps/                 # Aplicaciones principales
│   ├── 📱 frontend/         # Vue.js + Vite
│   ├── 🚀 backend/          # Spring Boot API
│   └── ⏰ horarios/         # Microservicio horarios
├── 📁 infrastructure/       # Configuración
├── 📄 .env                  # Variables de entorno
└── 📄 docker-compose.yml    # Orquestación
```

## 🛠️ Tecnologías

- **Frontend**: Vue.js 3 + Vite + Supabase
- **Backend**: Spring Boot 3 + PostgreSQL
- **Base de Datos**: Supabase PostgreSQL
- **Infraestructura**: Docker + Railway

## 🚀 Inicio Rápido

### Prerrequisitos
- Node.js 18+
- Java 17+
- Maven 3.6+

### Instalación
```bash
# Clonar repositorio
git clone https://github.com/Majprofe/gestion_guardias.git
cd gestion_guardias

# Configurar variables de entorno
cp .env.example .env
# Edita .env con tus credenciales

# Ejecutar aplicaciones
npm run dev:frontend    # Puerto 5500
npm run dev:backend     # Puerto 8081
npm run dev:horarios    # Puerto 8082
```

## 📚 Documentación

- **API Backend**: http://localhost:8081/swagger-ui.html
- **API Horarios**: http://localhost:8082/swagger-ui.html

## 👥 Contribución

1. Fork del proyecto
2. Crear rama feature
3. Commit cambios
4. Push a la rama
5. Crear Pull Request

## 📄 Licencia

MIT License - ver [LICENSE](LICENSE) para detalles.