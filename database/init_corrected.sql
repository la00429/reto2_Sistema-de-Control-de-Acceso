-- Base de datos para el sistema de control de acceso peatonal
-- Estructura ajustada según especificación

CREATE DATABASE IF NOT EXISTS access_control_db;
USE access_control_db;

-- ==========================================
-- TABLA EMPLOYEE (EmployeeDB)
-- ==========================================
-- Según especificación: document, firstname, lastname, email, phone, status (Boolean)
CREATE TABLE IF NOT EXISTS employee (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    document VARCHAR(50) UNIQUE NOT NULL COMMENT 'Documento de identidad (identificador principal)',
    firstname VARCHAR(100) NOT NULL,
    lastname VARCHAR(100) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    phone VARCHAR(20),
    status BOOLEAN DEFAULT TRUE COMMENT 'true=activo, false=inactivo',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_document (document),
    INDEX idx_email (email),
    INDEX idx_status (status)
);

-- ==========================================
-- TABLA ACCESS (AccessControlDB)
-- ==========================================
-- Según especificación: employeeID (String - documento), accessdatetime (String)
CREATE TABLE IF NOT EXISTS access (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employeeID VARCHAR(50) NOT NULL COMMENT 'Documento del empleado (String)',
    accessdatetime VARCHAR(50) NOT NULL COMMENT 'Fecha y hora como String',
    access_type ENUM('ENTRY', 'EXIT') NOT NULL COMMENT 'Tipo de acceso (para lógica interna)',
    location VARCHAR(100),
    device_id VARCHAR(100),
    status ENUM('SUCCESS', 'FAILED', 'BLOCKED') DEFAULT 'SUCCESS',
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_employeeID (employeeID),
    INDEX idx_accessdatetime (accessdatetime),
    INDEX idx_access_type (access_type)
);

-- ==========================================
-- TABLA ALERT (AccessControlDB)
-- ==========================================
-- Según especificación: ID (String), Timestamp (String/Date), Description (String), Code (String)
CREATE TABLE IF NOT EXISTS alert (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    alert_id VARCHAR(100) UNIQUE NOT NULL COMMENT 'ID de la alerta (String)',
    timestamp VARCHAR(50) NOT NULL COMMENT 'Timestamp como String',
    description TEXT COMMENT 'Description de la alerta',
    code VARCHAR(50) NOT NULL COMMENT 'Code de la alerta',
    username VARCHAR(100),
    employee_code VARCHAR(50),
    ip_address VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_code (code),
    INDEX idx_username (username),
    INDEX idx_employee_code (employee_code),
    INDEX idx_timestamp (timestamp)
);

-- ==========================================
-- TABLAS ADICIONALES (para funcionalidad extendida)
-- ==========================================

-- Tabla de usuarios del sistema (para autenticación)
CREATE TABLE IF NOT EXISTS system_users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('ADMIN', 'USER', 'OPERATOR') DEFAULT 'USER',
    employee_id BIGINT,
    status ENUM('ACTIVE', 'INACTIVE', 'LOCKED') DEFAULT 'ACTIVE',
    failed_login_attempts INT DEFAULT 0,
    last_login TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (employee_id) REFERENCES employee(id) ON DELETE SET NULL,
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_status (status)
);

-- ==========================================
-- DATOS DE EJEMPLO
-- ==========================================

-- Insertar empleados de ejemplo
INSERT INTO employee (document, firstname, lastname, email, phone, status) VALUES
('12345678', 'Juan', 'Pérez', 'juan.perez@empresa.com', '555-0101', TRUE),
('87654321', 'María', 'González', 'maria.gonzalez@empresa.com', '555-0102', TRUE),
('11223344', 'Carlos', 'Rodríguez', 'carlos.rodriguez@empresa.com', '555-0103', TRUE);

-- Insertar usuario administrador
INSERT INTO system_users (username, email, password_hash, role, status) VALUES
('admin', 'admin@empresa.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ADMIN', 'ACTIVE');
-- Password: admin123



