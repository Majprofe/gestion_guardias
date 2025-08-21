-- Migración: Configurar Row Level Security
-- Archivo: 20250818000002_setup_rls.sql

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
