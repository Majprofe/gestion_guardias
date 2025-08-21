-- Migración: Datos iniciales y funciones
-- Archivo: 20250818000003_initial_data.sql

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
('Informática', 'INF'),
('Biología', 'BIO'),
('Física', 'FIS'),
('Química', 'QUI'),
('Francés', 'FRA')
ON CONFLICT DO NOTHING;

-- Insertar algunas aulas de ejemplo
INSERT INTO aulas (nombre, abreviatura) VALUES
('Aula 101', 'A101'),
('Aula 102', 'A102'),
('Aula 103', 'A103'),
('Aula 201', 'A201'),
('Aula 202', 'A202'),
('Laboratorio Informática', 'LAB1'),
('Laboratorio Ciencias', 'LAB2'),
('Gimnasio', 'GYM'),
('Aula Música', 'MUS'),
('Biblioteca', 'BIB'),
('Salón de Actos', 'ACT'),
('Aula Tecnología', 'TEC')
ON CONFLICT DO NOTHING;

-- Insertar algunos grupos de ejemplo
INSERT INTO grupos (nombre, abreviatura, es_problematico) VALUES
('1º ESO A', '1A', false),
('1º ESO B', '1B', true),
('1º ESO C', '1C', false),
('2º ESO A', '2A', false),
('2º ESO B', '2B', false),
('3º ESO A', '3A', false),
('3º ESO B', '3B', true),
('4º ESO A', '4A', false),
('4º ESO B', '4B', true),
('1º Bachillerato A', 'BAC1A', false),
('1º Bachillerato B', 'BAC1B', false),
('2º Bachillerato A', 'BAC2A', false)
ON CONFLICT DO NOTHING;
