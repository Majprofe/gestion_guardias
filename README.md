# ✨ Gestión de Guardias Escolares

![Banner del Proyecto](https://github.com/IESJandula/gestion_guardias/raw/main/Client/src/assets/iesjandula.png)

Sistema completo de **Gestión de Faltas y Coberturas Docentes** desarrollado como proyecto final del ciclo de Desarrollo de Aplicaciones Web. Esta aplicación resuelve la necesidad de organizar de manera eficiente las ausencias del profesorado y la cobertura de esas ausencias por parte de otros docentes disponibles.

Este sistema está orientado a su uso en **instituciones educativas** y permite:
- Registrar de forma intuitiva las ausencias de los docentes.
- Asignar automáticamente coberturas según el profesorado disponible en horario de guardia.
- Permitir la asignación manual desde el panel de administrador o responsable.
- Visualizar tanto las faltas como las coberturas desde diferentes vistas según el rol del usuario.

---

## 🌐 Tecnologías y Arquitectura

- **Frontend:** Vue 3 con Vite, Pinia, Vue Router y Toastification.
- **Backend:** Spring Boot + Spring Data JPA.
- **Base de datos:** MySQL.
- **Persistencia:** Hibernate ORM.
- **Control de acceso:** Vía identificación por email (en localStorage).
- **Estilo:** CSS con soporte a Tailwind (opcional).

---

## 📊 Características del Proyecto

### Registro de Ausencias
Los profesores pueden acceder a un formulario en el que seleccionan:
- Fecha de la ausencia.
- Si afecta a todo el día o solo a determinadas horas.
- Aula, grupo, hora y tarea para cada hora afectada.

El sistema permite guardar varias ausencias en una sola operación.

### Asignación Automática de Coberturas
Cuando se registra una ausencia, el sistema:
- Consulta qué profesores están en guardia esa hora y día.
- De entre ellos, elige el que menos guardias ha realizado.
- Asigna automáticamente esa cobertura.

### Panel de Faltas del Día
Desde esta vista:
- Se consultan las ausencias por día.
- Se muestra qué profesor la cubre (si hay cobertura).
- Se permite asignar manualmente una cobertura.
- Los profesores sólo pueden eliminar sus propias ausencias.

### Histórico de Ausencias
Desde el panel histórico:
- Se puede consultar cualquier ausencia anterior.
- Se permite eliminar una cobertura y reasignarla.
- Se muestra toda la información contextual (hora, grupo, aula, tarea).

### Mis Guardias / Ausencias
Cada profesor puede consultar:
- Las ausencias que él mismo ha registrado.
- Las coberturas en las que ha participado.

---

## 🚀 Despliegue y Uso en Local

### 1. Requisitos Previos

- Node.js 18+
- npm 9+
- JDK 17
- Maven 3.8+
- MySQL Server 8+

---

### 2. Configurar Base de Datos MySQL

```sql
CREATE DATABASE gestion_guardias;
```

---

### 3. Backend (Spring Boot)

#### a. Acceder a la carpeta del backend:
```bash
cd backend
```

#### b. Crear archivo de configuración:
Crea `src/main/resources/application.properties` con:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/gestion_guardias
spring.datasource.username=usuario
spring.datasource.password=contraseña
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

#### c. Ejecutar el backend
```bash
./mvnw spring-boot:run
```
Se iniciará en `http://localhost:8081`

---

### 4. Frontend (Vue 3)

#### a. Accede al directorio del frontend
```bash
cd ../frontend
```

#### b. Instala dependencias
```bash
npm install
```

#### c. Ejecuta la aplicación
```bash
npm run dev
```

El frontend estará disponible en:

```
http://localhost:5500
```

---

## 📸 Capturas Recomendadas

> En la carpeta `assets` se encuentran las siguientes imagenes necesarias para la visualización de la web:

1. `formulario-ausencia.png` — vista del formulario de alta.
2. `gestion-faltas.png` — vista de faltas del día.
3. `historico.png` — gestión manual de coberturas.
4. `mis-guardias.png` — vista personalizada del profesor.

---

## 📈 Estructura del Repositorio

```
gestion-guardias/
├── backend/
│   ├── src/
│   ├── pom.xml
│   └── application.properties
├── frontend/
│   ├── src/
│   ├── package.json
│   └── vite.config.js
├── docs/
│   └── [capturas y banner]
├── README.md
└── .gitignore
```

---

## 🎓 Créditos y Autor

**Autor:** Álvaro Manuel Solís Martínez, Pau Barón Jiménez, Francisco Javier García Pedrajas
**Email:** asolmar680@g.educaand.es

---


## ⭐ Contribuciones

Para ayudar a la mejora futura de la aplicación como:
- Reportar errores
- Proponer mejoras
- Hacer un fork y enviar un PR

Clona el repositorio:

```bash
git clone https://github.com/tuusuario/gestion-guardias.git