-- Migración inicial: Crear tablas base
-- Archivo: 20250818000001_initial_schema.sql

-- Crear extensiones necesarias
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ============================================================================
-- TABLA DE PROFESORES (extendiendo la tabla de usuarios de Supabase)
-- ============================================================================
CREATE TABLE IF NOT EXISTS profesores (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    nombre VARCHAR(255) NOT NULL,
    abreviatura VARCHAR(10),
    departamento VARCHAR(255),
    guardias_realizadas INTEGER DEFAULT 0,
    guardias_problematicas INTEGER DEFAULT 0,
    es_admin BOOLEAN DEFAULT FALSE,
    activo BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- ============================================================================
-- TABLA DE ASIGNATURAS
-- ============================================================================
CREATE TABLE IF NOT EXISTS asignaturas (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    abreviatura VARCHAR(10),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- ============================================================================
-- TABLA DE AULAS
-- ============================================================================
CREATE TABLE IF NOT EXISTS aulas (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    abreviatura VARCHAR(10),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- ============================================================================
-- TABLA DE GRUPOS
-- ============================================================================
CREATE TABLE IF NOT EXISTS grupos (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    abreviatura VARCHAR(10),
    es_problematico BOOLEAN DEFAULT FALSE,
    tutor_id BIGINT REFERENCES profesores(id),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- ============================================================================
-- TABLA DE TRAMOS HORARIOS
-- ============================================================================
CREATE TABLE IF NOT EXISTS tramos_horarios (
    id BIGSERIAL PRIMARY KEY,
    dia_semana INTEGER NOT NULL CHECK (dia_semana BETWEEN 1 AND 7),
    hora_dia INTEGER NOT NULL CHECK (hora_dia BETWEEN 1 AND 10),
    hora_inicio TIME NOT NULL,
    hora_fin TIME NOT NULL,
    UNIQUE(dia_semana, hora_dia)
);

-- ============================================================================
-- TABLA DE ACTIVIDADES (horarios)
-- ============================================================================
CREATE TYPE tipo_actividad AS ENUM ('CLASE', 'GUARDIA', 'REUNION', 'OTROS');

CREATE TABLE IF NOT EXISTS actividades (
    id BIGSERIAL PRIMARY KEY,
    profesor_id BIGINT NOT NULL REFERENCES profesores(id),
    tramo_horario_id BIGINT NOT NULL REFERENCES tramos_horarios(id),
    asignatura_id BIGINT REFERENCES asignaturas(id),
    aula_id BIGINT REFERENCES aulas(id),
    tipo tipo_actividad NOT NULL DEFAULT 'CLASE',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- ============================================================================
-- TABLA DE GRUPOS POR ACTIVIDAD (relación muchos a muchos)
-- ============================================================================
CREATE TABLE IF NOT EXISTS actividad_grupos (
    actividad_id BIGINT NOT NULL REFERENCES actividades(id) ON DELETE CASCADE,
    grupo_id BIGINT NOT NULL REFERENCES grupos(id) ON DELETE CASCADE,
    PRIMARY KEY (actividad_id, grupo_id)
);

-- ============================================================================
-- TABLA DE AUSENCIAS
-- ============================================================================
CREATE TABLE IF NOT EXISTS ausencias (
    id BIGSERIAL PRIMARY KEY,
    profesor_ausente_email VARCHAR(255) NOT NULL,
    fecha DATE NOT NULL,
    grupo VARCHAR(255),
    aula VARCHAR(255),
    hora INTEGER NOT NULL CHECK (hora BETWEEN 1 AND 10),
    tarea TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- ============================================================================
-- TABLA DE COBERTURAS
-- ============================================================================
CREATE TABLE IF NOT EXISTS coberturas (
    id BIGSERIAL PRIMARY KEY,
    ausencia_id BIGINT NOT NULL UNIQUE REFERENCES ausencias(id) ON DELETE CASCADE,
    profesor_cubre_email VARCHAR(255) NOT NULL,
    grupo VARCHAR(255),
    aula VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- ============================================================================
-- ÍNDICES PARA OPTIMIZACIÓN
-- ============================================================================
CREATE INDEX IF NOT EXISTS idx_profesores_email ON profesores(email);
CREATE INDEX IF NOT EXISTS idx_ausencias_fecha ON ausencias(fecha);
CREATE INDEX IF NOT EXISTS idx_ausencias_email ON ausencias(profesor_ausente_email);
CREATE INDEX IF NOT EXISTS idx_coberturas_email ON coberturas(profesor_cubre_email);
CREATE INDEX IF NOT EXISTS idx_actividades_profesor ON actividades(profesor_id);
CREATE INDEX IF NOT EXISTS idx_actividades_tramo ON actividades(tramo_horario_id);
