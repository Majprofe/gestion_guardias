# 🌳 Flujo de Trabajo Git - Gestión de Guardias

## 📊 Estructura de Ramas

```
main (producción) ← Solo código probado y estable
  │
  └── develop (integración) ← Rama principal de desarrollo
       │
       ├── feature/nueva-funcionalidad-1
       ├── feature/nueva-funcionalidad-2
       ├── fix/correccion-bug
       └── refactor/mejora-codigo
```

## 🎯 Descripción de Ramas

### **1. `main`**
- 🔒 **Protegida**: Solo código en producción
- ✅ **Contiene**: Versiones estables y probadas
- 🚀 **Deploy**: Automático a producción (si se configura CI/CD)
- 📝 **Merge desde**: `develop` (después de testing completo)

### **2. `develop`**
- 🔧 **Rama de integración**: Todo el desarrollo pasa por aquí
- ✅ **Contiene**: Últimas funcionalidades integradas
- 🧪 **Testing**: Se prueba antes de merge a `main`
- 📝 **Merge desde**: `feature/*`, `fix/*`, `refactor/*`

### **3. Ramas de Features**
- 🆕 **Patrón**: `feature/nombre-descriptivo`
- 📝 **Ejemplos**:
  - `feature/notificaciones-email`
  - `feature/estadisticas-avanzadas`
  - `feature/exportar-excel`
- 🔀 **Se crean desde**: `develop`
- 🔀 **Se mergen a**: `develop`

### **4. Ramas de Fixes**
- 🐛 **Patrón**: `fix/nombre-bug`
- 📝 **Ejemplos**:
  - `fix/error-login`
  - `fix/calculo-horas`
- 🔀 **Se crean desde**: `develop`
- 🔀 **Se mergen a**: `develop`

### **5. Ramas de Hotfix (Opcional)**
- 🚨 **Patrón**: `hotfix/nombre-urgente`
- 📝 **Uso**: Bugs críticos en producción
- 🔀 **Se crean desde**: `main`
- 🔀 **Se mergen a**: `main` Y `develop`

---

## 🔄 Flujo de Trabajo Completo

### **Paso 1: Crear una nueva funcionalidad**

```bash
# 1. Asegurarte de estar en develop actualizado
git checkout develop
git pull origin develop

# 2. Crear rama de feature
git checkout -b feature/nombre-funcionalidad

# 3. Desarrollar (hacer commits frecuentes)
git add .
git commit -m "feat: Descripción del cambio"

# 4. Push de la rama
git push -u origin feature/nombre-funcionalidad
```

### **Paso 2: Integrar la funcionalidad**

```bash
# 1. Actualizar develop primero
git checkout develop
git pull origin develop

# 2. Merge de tu feature a develop
git merge feature/nombre-funcionalidad

# 3. Resolver conflictos si los hay

# 4. Push de develop
git push origin develop

# 5. (Opcional) Borrar rama de feature
git branch -d feature/nombre-funcionalidad
git push origin --delete feature/nombre-funcionalidad
```

### **Paso 3: Release a producción**

```bash
# Cuando develop está estable y listo

# 1. Actualizar main
git checkout main
git pull origin main

# 2. Merge de develop a main
git merge develop

# 3. Crear tag de versión
git tag -a v1.2.0 -m "Release v1.2.0 - Sistema de archivos"

# 4. Push con tags
git push origin main --tags
```

---

## 📝 Convenciones de Commits

Usa **Conventional Commits** para mensajes claros:

### **Tipos de commits:**

- `feat:` Nueva funcionalidad
  - Ejemplo: `feat: Agregar sistema de notificaciones`

- `fix:` Corrección de bug
  - Ejemplo: `fix: Corregir error en cálculo de horas`

- `refactor:` Refactorización sin cambiar funcionalidad
  - Ejemplo: `refactor: Optimizar consultas de base de datos`

- `docs:` Cambios en documentación
  - Ejemplo: `docs: Actualizar README con instrucciones`

- `style:` Cambios de formato (sin afectar código)
  - Ejemplo: `style: Formatear código con prettier`

- `test:` Añadir o modificar tests
  - Ejemplo: `test: Agregar tests para AuthService`

- `chore:` Tareas de mantenimiento
  - Ejemplo: `chore: Actualizar dependencias`

- `perf:` Mejoras de rendimiento
  - Ejemplo: `perf: Optimizar carga de horarios`

### **Formato completo:**

```
<tipo>(<scope>): <descripción corta>

<descripción larga opcional>

<footer opcional>
```

**Ejemplo:**
```
feat(ausencias): Agregar sistema de archivos PDF

- Implementar FileStorageService con validaciones
- Crear entidad ArchivoHoraAusencia
- Añadir endpoint de descarga
- Actualizar frontend con upload de archivos

Closes #123
```

---

## 🛡️ Protección de Ramas (GitHub)

### **Recomendaciones para `main`:**

1. **Branch Protection Rules**:
   - ✅ Require pull request before merging
   - ✅ Require approvals (mínimo 1 reviewer)
   - ✅ Require status checks to pass
   - ✅ Prohibit force push
   - ✅ Require linear history

2. **Cómo configurar** (en GitHub):
   ```
   Settings → Branches → Add branch protection rule
   Branch name pattern: main
   ```

### **Recomendaciones para `develop`:**

1. **Branch Protection Rules**:
   - ✅ Require pull request before merging
   - ⚠️ Require approvals (opcional, depende del equipo)
   - ✅ Prohibit force push

---

## 🎬 Ejemplo Práctico Completo

### **Escenario: Añadir sistema de notificaciones por email**

```bash
# PASO 1: Preparación
git checkout develop
git pull origin develop

# PASO 2: Crear feature
git checkout -b feature/notificaciones-email

# PASO 3: Desarrollo
# ... (hacer cambios en el código)
git add apps/backend/src/main/java/es/iesjandula/guardias/services/EmailService.java
git commit -m "feat(notificaciones): Crear EmailService base"

# ... (más desarrollo)
git add apps/backend/src/main/java/es/iesjandula/guardias/config/EmailConfig.java
git commit -m "feat(notificaciones): Configurar servidor SMTP"

# ... (testing)
git add apps/backend/src/test/java/es/iesjandula/guardias/services/EmailServiceTest.java
git commit -m "test(notificaciones): Añadir tests para EmailService"

# PASO 4: Push de la feature
git push -u origin feature/notificaciones-email

# PASO 5: Crear Pull Request en GitHub
# Ir a: https://github.com/Majprofe/gestion_guardias/pulls
# Comparar: develop ← feature/notificaciones-email
# Añadir descripción detallada
# Solicitar revisión

# PASO 6: Después de aprobación, merge a develop
git checkout develop
git pull origin develop
git merge feature/notificaciones-email
git push origin develop

# PASO 7: Limpiar rama
git branch -d feature/notificaciones-email
git push origin --delete feature/notificaciones-email
```

---

## 🚨 Comandos de Emergencia

### **Deshacer último commit (sin perder cambios)**
```bash
git reset --soft HEAD~1
```

### **Deshacer último commit (perdiendo cambios)**
```bash
git reset --hard HEAD~1
```

### **Ver diferencias entre ramas**
```bash
git diff develop..feature/mi-feature
```

### **Actualizar feature con cambios de develop**
```bash
git checkout feature/mi-feature
git merge develop
# o mejor aún:
git rebase develop
```

### **Guardar cambios temporalmente**
```bash
git stash
# ... cambiar de rama, hacer algo
git stash pop
```

### **Ver historial gráfico**
```bash
git log --graph --oneline --all
```

---

## 📚 Estado Actual del Proyecto

### **Ramas Existentes:**
- ✅ `main` - Producción (última versión estable con sistema de archivos)
- ✅ `develop` - Desarrollo (recién creada desde main)

### **Próximos Pasos:**
1. Siempre trabajar desde `develop`
2. Crear `feature/*` para nuevas funcionalidades
3. Hacer Pull Requests a `develop`
4. Cuando `develop` esté estable → merge a `main`

---

## 🎯 Resumen Rápido

```bash
# Para nueva funcionalidad:
git checkout develop
git pull origin develop
git checkout -b feature/mi-funcionalidad
# ... desarrollar ...
git push -u origin feature/mi-funcionalidad
# ... crear PR en GitHub ...

# Para integrar:
git checkout develop
git merge feature/mi-funcionalidad
git push origin develop

# Para release:
git checkout main
git merge develop
git tag -a v1.x.x -m "Release vX.X.X"
git push origin main --tags
```

---

**🎓 Buenas Prácticas:**
- ✅ Commits pequeños y frecuentes
- ✅ Mensajes descriptivos
- ✅ Pull antes de push
- ✅ Nunca hacer force push en ramas compartidas
- ✅ Revisar código antes de merge
- ✅ Mantener `main` siempre estable
- ✅ Testing antes de merge a `main`
