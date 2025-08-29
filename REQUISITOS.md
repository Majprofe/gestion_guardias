# 📘 Sistema de Gestión de Guardias y Sustituciones

## Descripción General

En el instituto, cuando un profesor va a faltar por cualquier motivo, debe indicarlo previamente y dejar tareas para que los alumnos las realicen durante su ausencia.

El profesor debe señalar:
- El/los día/s que faltará.
- Las horas de cada día (de 1ª a 6ª hora).
- Las tareas obligatorias que deben realizar sus alumnos durante la ausencia.

El sistema debe coordinar esta información con los horarios de todos los profesores, ya que será necesario saber:
- Qué grupos y aulas quedan sin profesor.
- Qué profesores están de guardia o de refuerzo en cada franja horaria.

Además, los grupos de alumnos se clasifican como "buenos" o "problemáticos", lo cual influirá en la distribución de sustituciones.

---

## Reglas y Supuestos Funcionales

### 1. Horarios de Profesores
- Cada profesor tiene un horario semanal fijo con:
  - Clases.
  - Horas de guardia.

### 2. Registro de Ausencias
- Las ausencias pueden ser de uno o varios días consecutivos.
- Se deben especificar las horas afectadas cada día (1ª a 6ª).
- Al registrar una ausencia el programa debe de leer el horario del profesor para mostrar sólo las horas en las que tiene clase.
- Cada ausencia debe incluir tareas obligatorias para los alumnos.
- Y también debe permitir subir un archivo o más de uno en caso de que el profesor lo vea conveniente.

### 3. Clasificación de Grupos
- Cada grupo de alumnos se clasifica como:
  - Bueno.
  - Problemático.

### 4. Guardias
- Cualquier profesor puede ser profesor de guardia
- Cada hora de guardia se organiza así:
  1. Un profesor OBLIGATORIAMENTE debe cubrir el aula de convivencia (alumnos expulsados).
  2. El resto cubrirá ausencias de profesores.

### 5. Reglas de Asignación de Sustituciones
- Se mantiene un contador de sustituciones realizadas por cada profesor, desglosado por:
  - Día de la semana.
  - Hora.
- La asignación debe:
  - Distribuir equitativamente las sustituciones entre todos los profesores que están de guardia a esa hora y ese día de la semana.
  - Evitar que siempre sean los mismos quienes cubran grupos problemáticos.
  - Se creará un contador de guardias realizadas y otro de guardias problemáticas para cada profesor en cada tramo horario que está de guardia, por lo normal suele tener cada profesor entre 2-4 guardias a la semana, pero también puede que no tenga ninguna guardia.
  - Para ello cada vez que un profesor realice una guardia, se sumará en uno el contador suyo de guardias.
  - Y cuando realice una guardia problemática se incrementará en una unidad el contador de guardias problemáticas y también el de guardias.
  - Cuando un profesor inserte una falta se debe asignar/reasignar la cobertura de guardias a los profesores que tienen guardia a esa hora. Teniendo en cuenta que en cada hora de cada día, se asignará primero el profesor que va al aula de convivencia (asignando al profesor que menos horas de convivencia tiene), después se asignarán las guardias de los grupos problemáticos al profesor que menos horas de grupos problemáticos tiene para esa guardia en ese día y hora, y por último la guardia normal para el profesor que menos guardias normales tenga en su contador.

### 6. Restricciones y Alertas
- Las sustituciones no alteran el horario regular de guardias/refuerzos.
- El sistema debe permitir consultar:
  - Qué profesores están de guardia o refuerzo en cada hora y día.
- Si no hay suficientes profesores de guardia y refuerzo para cubrir todas las ausencias en alguna hora del día, el sistema debe alertar.

---

## Requisitos Clave del Sistema

1. **Horario semanal fijo** con clases, guardias y refuerzos.
2. **Registro de ausencias** (días, horas y tareas).
3. **Identificar los grupos problemáticos**.
4. **Gestión de equipo de guardia**.
5. **Contador de sustituciones** (día/hora).
6. **Asignación justa y rotativa** de guardias.
7. **Obligación de cubrir aula de convivencia**.
8. **Consulta de disponibilidad** por día y hora.
9. **Alertas** cuando no haya suficientes guardias/refuerzos.

---

## Funcionalidades Principales

### 📋 Gestión de Ausencias
- Registro de ausencias por profesor
- Especificación de días y horas afectadas
- Definición de tareas para alumnos
- Subida de archivos adjuntos

### 👥 Gestión de Profesores
- Horarios semanales individuales
- Clasificación de horas (clases/guardias)
- Contadores de sustituciones realizadas
- Historial de guardias por día/hora

### 🏫 Gestión de Grupos
- Clasificación de grupos (buenos/problemáticos)
- Asignación a aulas específicas
- Seguimiento de sustituciones recibidas

### ⚖️ Sistema de Asignación Equitativa
- Distribución justa de guardias
- Priorización por contadores existentes
- Asignación automática de aula de convivencia
- Rotación equilibrada de responsabilidades

### 🚨 Sistema de Alertas
- Notificaciones de insuficiencia de guardias
- Alertas de desequilibrio en asignaciones
- Avisos de grupos sin cobertura

### 📊 Reportes y Consultas
- Disponibilidad de profesores por día/hora
- Estadísticas de guardias realizadas
- Histórico de ausencias y coberturas
- Informes de carga de trabajo por profesor

---

## Requisitos Específicos de Cobertura de Guardias

### 🎯 Sistema de Contadores por Profesor
Cada profesor debe tener los siguientes contadores por cada tramo horario (día + hora):

1. **Contador de Guardias Normales**: Número de veces que ha cubierto grupos "buenos"
2. **Contador de Guardias Problemáticas**: Número de veces que ha cubierto grupos "problemáticos"  
3. **Contador de Aula de Convivencia**: Número de veces que ha cubierto el aula de convivencia
4. **Contador Total de Guardias**: Suma de todos los tipos de guardias realizadas

### 🔄 Algoritmo de Asignación Automática
Cuando se registra una nueva ausencia, el sistema debe **redistribuir TODAS las coberturas del día** para optimizar la equidad:

#### **🎯 Proceso de Redistribución Completa:**
1. **Eliminar coberturas automáticas existentes** del día (estado ASIGNADA) para permitir reasignación
2. **Mantener coberturas validadas** (estado VALIDADA) que ya han sido confirmadas por administradores
3. **Identificar profesores disponibles** en cada tramo horario que tengan guardia asignada
4. **Aplicar algoritmo de asignación equitativa** por orden de prioridad

#### **📋 Orden de Asignación por Tramo Horario:**
**1️⃣ AULA DE CONVIVENCIA (OBLIGATORIA)**
- Se asigna **SIEMPRE** al profesor con menor contador de "Aula de Convivencia" en ese día+hora
- **Un profesor por hora** debe estar obligatoriamente en convivencia
- Este profesor queda **excluido** del resto de asignaciones de esa hora

**2️⃣ GRUPOS PROBLEMÁTICOS**  
- Se asignan a los profesores con menor contador de "Guardias Problemáticas" en ese día+hora
- **Excluidos**: Profesores ya asignados al aula de convivencia
- Se asignan tantos profesores como grupos problemáticos haya sin cubrir

**3️⃣ GRUPOS NORMALES**
- Se asignan a los profesores con menor contador de "Guardias Normales" en ese día+hora  
- **Excluidos**: Profesores ya asignados a convivencia y grupos problemáticos
- Se asignan hasta completar todas las ausencias restantes

#### **⚖️ Casos Especiales:**
- **Sin ausencias**: Solo se asigna aula de convivencia (siempre obligatoria)
- **Más profesores que ausencias**: Solo se asignan los necesarios + convivencia
- **Más ausencias que profesores**: Se alerta de insuficiencia de personal
- **Empates en contadores**: Se desempata por orden alfabético del email del profesor

### ⚖️ Criterios de Equidad
- **Prioridad por día/hora específico**: Los contadores son independientes para cada combinación día+hora
- **Rotación automática**: El sistema siempre asigna al profesor con menos guardias en esa franja
- **Transparencia**: Cada profesor puede consultar sus propios contadores
- **Redistribución**: Si se cancela una ausencia, los contadores se ajustan automáticamente

### 🔍 Validaciones del Sistema
- Verificar que hay al menos un profesor de guardia para cubrir el aula de convivencia
- Alertar si no hay suficientes profesores para cubrir todas las ausencias
- Validar que no se asigne a un profesor que ya tiene clase en esa hora
- Comprobar que el grupo problemático se asigna efectivamente a quien menos ha cubierto este tipo

### 📈 Métricas y Reportes de Cobertura
- **Dashboard de Equidad**: Visualización de contadores por profesor y tramo horario
- **Reporte de Desbalance**: Identificación de profesores con cargas muy dispares
- **Historial de Asignaciones**: Registro completo de todas las coberturas realizadas
- **Proyección de Carga**: Estimación de guardias futuras basada en patrones históricos
