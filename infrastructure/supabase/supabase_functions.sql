-- ============================================================================
-- FUNCIONES SQL PARA SUPABASE
-- Sistema de Gestión de Guardias Escolares
-- ============================================================================

-- Función para incrementar guardias de un profesor
CREATE OR REPLACE FUNCTION incrementar_guardias(
    profesor_id BIGINT,
    campo_a_incrementar TEXT
) RETURNS void AS $$
BEGIN
    IF campo_a_incrementar = 'guardias_realizadas' THEN
        UPDATE profesores 
        SET guardias_realizadas = guardias_realizadas + 1 
        WHERE id = profesor_id;
    ELSIF campo_a_incrementar = 'guardias_problematicas' THEN
        UPDATE profesores 
        SET guardias_problematicas = guardias_problematicas + 1 
        WHERE id = profesor_id;
    END IF;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Función para decrementar guardias de un profesor
CREATE OR REPLACE FUNCTION decrementar_guardias(
    profesor_id BIGINT,
    campo_a_decrementar TEXT
) RETURNS void AS $$
BEGIN
    IF campo_a_decrementar = 'guardias_realizadas' THEN
        UPDATE profesores 
        SET guardias_realizadas = GREATEST(guardias_realizadas - 1, 0)
        WHERE id = profesor_id;
    ELSIF campo_a_decrementar = 'guardias_problematicas' THEN
        UPDATE profesores 
        SET guardias_problematicas = GREATEST(guardias_problematicas - 1, 0)
        WHERE id = profesor_id;
    END IF;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Función para obtener profesores disponibles para guardia en un momento específico
CREATE OR REPLACE FUNCTION get_profesores_disponibles_guardia(
    dia_semana_param INTEGER,
    hora_dia_param INTEGER,
    fecha_param DATE DEFAULT CURRENT_DATE
) RETURNS TABLE (
    id BIGINT,
    email VARCHAR(255),
    nombre VARCHAR(255),
    guardias_realizadas INTEGER,
    guardias_problematicas INTEGER
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        p.id,
        p.email,
        p.nombre,
        p.guardias_realizadas,
        p.guardias_problematicas
    FROM profesores p
    INNER JOIN actividades a ON p.id = a.profesor_id
    INNER JOIN tramos_horarios th ON a.tramo_horario_id = th.id
    WHERE 
        p.activo = true
        AND a.tipo = 'GUARDIA'
        AND th.dia_semana = dia_semana_param
        AND th.hora_dia = hora_dia_param
        AND NOT EXISTS (
            -- No está cubriendo otra ausencia en esa fecha/hora
            SELECT 1 FROM coberturas c
            INNER JOIN ausencias au ON c.ausencia_id = au.id
            WHERE c.profesor_cubre_email = p.email
            AND au.fecha = fecha_param
            AND au.hora = hora_dia_param
        )
        AND NOT EXISTS (
            -- No está ausente en esa fecha/hora
            SELECT 1 FROM ausencias au2
            WHERE au2.profesor_ausente_email = p.email
            AND au2.fecha = fecha_param
            AND au2.hora = hora_dia_param
        )
    ORDER BY 
        p.guardias_realizadas ASC,
        p.guardias_problematicas ASC,
        p.nombre ASC;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Función para asignar cobertura automática
CREATE OR REPLACE FUNCTION asignar_cobertura_automatica(
    ausencia_id_param BIGINT
) RETURNS BIGINT AS $$
DECLARE
    ausencia_record RECORD;
    profesor_seleccionado RECORD;
    nueva_cobertura_id BIGINT;
    dia_semana_calc INTEGER;
BEGIN
    -- Obtener datos de la ausencia
    SELECT * INTO ausencia_record
    FROM ausencias
    WHERE id = ausencia_id_param;
    
    IF NOT FOUND THEN
        RAISE EXCEPTION 'Ausencia no encontrada';
    END IF;
    
    -- Calcular día de la semana (1=Lunes, 7=Domingo)
    dia_semana_calc := EXTRACT(DOW FROM ausencia_record.fecha);
    IF dia_semana_calc = 0 THEN
        dia_semana_calc := 7; -- Domingo
    END IF;
    
    -- Buscar profesor disponible
    SELECT * INTO profesor_seleccionado
    FROM get_profesores_disponibles_guardia(
        dia_semana_calc, 
        ausencia_record.hora, 
        ausencia_record.fecha
    )
    LIMIT 1;
    
    IF NOT FOUND THEN
        RAISE EXCEPTION 'No hay profesores disponibles para cubrir la guardia';
    END IF;
    
    -- Crear la cobertura
    INSERT INTO coberturas (ausencia_id, profesor_cubre_email, grupo, aula)
    VALUES (
        ausencia_id_param,
        profesor_seleccionado.email,
        ausencia_record.grupo,
        ausencia_record.aula
    )
    RETURNING id INTO nueva_cobertura_id;
    
    -- Incrementar guardias realizadas del profesor
    PERFORM incrementar_guardias(profesor_seleccionado.id, 'guardias_realizadas');
    
    -- Si el grupo es problemático, incrementar también guardias problemáticas
    IF EXISTS (
        SELECT 1 FROM grupos g 
        WHERE g.nombre = ausencia_record.grupo 
        AND g.es_problematico = true
    ) THEN
        PERFORM incrementar_guardias(profesor_seleccionado.id, 'guardias_problematicas');
    END IF;
    
    RETURN nueva_cobertura_id;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Función para obtener estadísticas de guardias por profesor
CREATE OR REPLACE FUNCTION get_estadisticas_guardias()
RETURNS TABLE (
    profesor_email VARCHAR(255),
    profesor_nombre VARCHAR(255),
    total_guardias INTEGER,
    guardias_problematicas INTEGER,
    mes_actual INTEGER,
    ano_actual INTEGER
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        p.email,
        p.nombre,
        p.guardias_realizadas,
        p.guardias_problematicas,
        COALESCE(stats_mes.guardias_mes, 0) as guardias_mes,
        EXTRACT(YEAR FROM CURRENT_DATE)::INTEGER as ano
    FROM profesores p
    LEFT JOIN (
        SELECT 
            c.profesor_cubre_email,
            COUNT(*) as guardias_mes
        FROM coberturas c
        INNER JOIN ausencias a ON c.ausencia_id = a.id
        WHERE 
            EXTRACT(MONTH FROM a.fecha) = EXTRACT(MONTH FROM CURRENT_DATE)
            AND EXTRACT(YEAR FROM a.fecha) = EXTRACT(YEAR FROM CURRENT_DATE)
        GROUP BY c.profesor_cubre_email
    ) stats_mes ON p.email = stats_mes.profesor_cubre_email
    WHERE p.activo = true
    ORDER BY p.guardias_realizadas DESC, p.nombre ASC;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Trigger para asignación automática de coberturas al crear ausencias
CREATE OR REPLACE FUNCTION trigger_asignar_cobertura_automatica()
RETURNS TRIGGER AS $$
BEGIN
    -- Intentar asignar cobertura automática
    BEGIN
        PERFORM asignar_cobertura_automatica(NEW.id);
    EXCEPTION 
        WHEN OTHERS THEN
            -- Si no se puede asignar automáticamente, continuar sin error
            RAISE NOTICE 'No se pudo asignar cobertura automática para ausencia %: %', NEW.id, SQLERRM;
    END;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Crear trigger (comentado por defecto, activar si se desea asignación automática)
-- DROP TRIGGER IF EXISTS trigger_asignar_cobertura ON ausencias;
-- CREATE TRIGGER trigger_asignar_cobertura
--     AFTER INSERT ON ausencias
--     FOR EACH ROW
--     EXECUTE FUNCTION trigger_asignar_cobertura_automatica();

-- Función para limpiar datos de prueba (solo para desarrollo)
CREATE OR REPLACE FUNCTION limpiar_datos_prueba()
RETURNS void AS $$
BEGIN
    DELETE FROM coberturas;
    DELETE FROM ausencias;
    UPDATE profesores SET guardias_realizadas = 0, guardias_problematicas = 0;
    RAISE NOTICE 'Datos de prueba limpiados correctamente';
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;
