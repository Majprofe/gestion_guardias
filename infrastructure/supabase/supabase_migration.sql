-- ============================================================================
-- SCRIPT DE MIGRACIÓN DE DATOS PARA SUPABASE
-- Sistema de Gestión de Guardias Escolares
-- ============================================================================

-- Crear extensiones necesarias
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

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

-- ============================================================================
-- TRIGGERS PARA UPDATED_AT
-- ============================================================================
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Aplicar triggers
CREATE TRIGGER update_profesores_updated_at BEFORE UPDATE ON profesores 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_ausencias_updated_at BEFORE UPDATE ON ausencias 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_coberturas_updated_at BEFORE UPDATE ON coberturas 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ============================================================================
-- ROW LEVEL SECURITY (RLS) PARA SUPABASE
-- ============================================================================

-- Habilitar RLS en todas las tablas
ALTER TABLE profesores ENABLE ROW LEVEL SECURITY;
ALTER TABLE ausencias ENABLE ROW LEVEL SECURITY;
ALTER TABLE coberturas ENABLE ROW LEVEL SECURITY;
ALTER TABLE asignaturas ENABLE ROW LEVEL SECURITY;
ALTER TABLE aulas ENABLE ROW LEVEL SECURITY;
ALTER TABLE grupos ENABLE ROW LEVEL SECURITY;
ALTER TABLE tramos_horarios ENABLE ROW LEVEL SECURITY;
ALTER TABLE actividades ENABLE ROW LEVEL SECURITY;

-- Políticas básicas (todos los usuarios autenticados pueden leer)
CREATE POLICY "Usuarios pueden ver profesores" ON profesores FOR SELECT USING (auth.role() = 'authenticated');
CREATE POLICY "Usuarios pueden ver asignaturas" ON asignaturas FOR SELECT USING (auth.role() = 'authenticated');
CREATE POLICY "Usuarios pueden ver aulas" ON aulas FOR SELECT USING (auth.role() = 'authenticated');
CREATE POLICY "Usuarios pueden ver grupos" ON grupos FOR SELECT USING (auth.role() = 'authenticated');
CREATE POLICY "Usuarios pueden ver tramos horarios" ON tramos_horarios FOR SELECT USING (auth.role() = 'authenticated');
CREATE POLICY "Usuarios pueden ver actividades" ON actividades FOR SELECT USING (auth.role() = 'authenticated');

-- Políticas para ausencias (los profesores solo pueden ver/editar sus propias ausencias)
CREATE POLICY "Profesores pueden ver sus ausencias" ON ausencias FOR SELECT 
    USING (auth.email() = profesor_ausente_email OR EXISTS (
        SELECT 1 FROM profesores WHERE email = auth.email() AND es_admin = true
    ));

CREATE POLICY "Profesores pueden crear ausencias" ON ausencias FOR INSERT 
    WITH CHECK (auth.email() = profesor_ausente_email);

CREATE POLICY "Profesores pueden editar sus ausencias" ON ausencias FOR UPDATE 
    USING (auth.email() = profesor_ausente_email OR EXISTS (
        SELECT 1 FROM profesores WHERE email = auth.email() AND es_admin = true
    ));

CREATE POLICY "Profesores pueden eliminar sus ausencias" ON ausencias FOR DELETE 
    USING (auth.email() = profesor_ausente_email OR EXISTS (
        SELECT 1 FROM profesores WHERE email = auth.email() AND es_admin = true
    ));

-- Políticas para coberturas
CREATE POLICY "Usuarios pueden ver coberturas" ON coberturas FOR SELECT 
    USING (auth.role() = 'authenticated');

CREATE POLICY "Solo admins pueden gestionar coberturas" ON coberturas FOR ALL 
    USING (EXISTS (
        SELECT 1 FROM profesores WHERE email = auth.email() AND es_admin = true
    ));

-- ============================================================================
-- DATOS INICIALES DE EJEMPLO
-- ============================================================================

-- Insertar tramos horarios básicos
INSERT INTO tramos_horarios (dia_semana, hora_dia, hora_inicio, hora_fin) VALUES
(1, 1, '08:00', '09:00'), (1, 2, '09:00', '10:00'), (1, 3, '10:00', '11:00'),
(1, 4, '11:30', '12:30'), (1, 5, '12:30', '13:30'), (1, 6, '13:30', '14:30'),
(2, 1, '08:00', '09:00'), (2, 2, '09:00', '10:00'), (2, 3, '10:00', '11:00'),
(2, 4, '11:30', '12:30'), (2, 5, '12:30', '13:30'), (2, 6, '13:30', '14:30'),
(3, 1, '08:00', '09:00'), (3, 2, '09:00', '10:00'), (3, 3, '10:00', '11:00'),
(3, 4, '11:30', '12:30'), (3, 5, '12:30', '13:30'), (3, 6, '13:30', '14:30'),
(4, 1, '08:00', '09:00'), (4, 2, '09:00', '10:00'), (4, 3, '10:00', '11:00'),
(4, 4, '11:30', '12:30'), (4, 5, '12:30', '13:30'), (4, 6, '13:30', '14:30'),
(5, 1, '08:00', '09:00'), (5, 2, '09:00', '10:00'), (5, 3, '10:00', '11:00'),
(5, 4, '11:30', '12:30'), (5, 5, '12:30', '13:30'), (5, 6, '13:30', '14:30')
ON CONFLICT (dia_semana, hora_dia) DO NOTHING;

-- Insertar algunas asignaturas de ejemplo
INSERT INTO asignaturas (nombre, abreviatura) VALUES
('Matemáticas', 'MAT'),
('Lengua Castellana', 'LEN'),
('Inglés', 'ING'),
('Historia', 'HIS'),
('Geografía', 'GEO'),
('Educación Física', 'EF'),
('Tecnología', 'TEC'),
('Informática', 'INF')
ON CONFLICT DO NOTHING;

-- Insertar algunas aulas de ejemplo
INSERT INTO aulas (nombre, abreviatura) VALUES
('Aula 101', 'A101'),
('Aula 102', 'A102'),
('Laboratorio Informática', 'LAB1'),
('Gimnasio', 'GYM'),
('Aula Música', 'MUS'),
('Biblioteca', 'BIB')
ON CONFLICT DO NOTHING;

-- Insertar algunos grupos de ejemplo
INSERT INTO grupos (nombre, abreviatura, es_problematico) VALUES
('1º ESO A', '1A', false),
('1º ESO B', '1B', true),
('2º ESO A', '2A', false),
('3º ESO A', '3A', false),
('4º ESO A', '4A', true)
ON CONFLICT DO NOTHING;
