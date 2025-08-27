-- ============================================================================
-- Configuración de Base de Datos MySQL - Sistema de Gestión de Guardias
-- ============================================================================

-- Crear base de datos para el sistema de guardias
DROP DATABASE IF EXISTS gestion_guardias;
CREATE DATABASE gestion_guardias CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Crear base de datos para el sistema de horarios
DROP DATABASE IF EXISTS gestion_horarios;
CREATE DATABASE gestion_horarios CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Crear usuario para las aplicaciones (opcional, puede usar root)
-- CREATE USER 'guardias_app'@'localhost' IDENTIFIED BY 'guardias_password';
-- GRANT ALL PRIVILEGES ON gestion_guardias.* TO 'guardias_app'@'localhost';
-- GRANT ALL PRIVILEGES ON gestion_horarios.* TO 'guardias_app'@'localhost';
-- FLUSH PRIVILEGES;

-- Usar la base de datos de guardias
USE gestion_guardias;

-- Las tablas se crearán automáticamente con JPA/Hibernate
-- Pero aquí documentamos la estructura esperada:

/*
-- Tabla de ausencias (creada automáticamente por JPA)
CREATE TABLE IF NOT EXISTS ausencias (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email_profesor VARCHAR(255) NOT NULL,
    fecha DATE NOT NULL,
    hora VARCHAR(10) NOT NULL,
    aula VARCHAR(50),
    observaciones TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_email_profesor (email_profesor),
    INDEX idx_fecha (fecha)
);

-- Tabla de coberturas (creada automáticamente por JPA)
CREATE TABLE IF NOT EXISTS coberturas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ausencia_id BIGINT NOT NULL,
    email_profesor_cobertura VARCHAR(255) NOT NULL,
    observaciones TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (ausencia_id) REFERENCES ausencias(id) ON DELETE CASCADE,
    INDEX idx_email_cobertura (email_profesor_cobertura),
    INDEX idx_ausencia_id (ausencia_id)
);
*/

-- Usar la base de datos de horarios
USE gestion_horarios;

-- Las tablas se crearán automáticamente con JPA/Hibernate
-- Estructura esperada para horarios:

/*
-- Tabla de profesores (creada automáticamente por JPA)
CREATE TABLE IF NOT EXISTS profesores (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    nombre VARCHAR(255) NOT NULL,
    departamento VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_email (email)
);

-- Tabla de horarios (creada automáticamente por JPA)
CREATE TABLE IF NOT EXISTS horarios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    profesor_id BIGINT NOT NULL,
    dia_semana VARCHAR(20) NOT NULL,
    hora VARCHAR(10) NOT NULL,
    asignatura VARCHAR(255),
    aula VARCHAR(50),
    grupo VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (profesor_id) REFERENCES profesores(id) ON DELETE CASCADE,
    INDEX idx_profesor_id (profesor_id),
    INDEX idx_dia_hora (dia_semana, hora)
);
*/

-- Mostrar bases de datos creadas
SHOW DATABASES LIKE 'gestion_%';
