-- Base de datos para el sistema de control de acceso peatonal

CREATE DATABASE IF NOT EXISTS access_control_db;
USE access_control_db;

-- Tabla de empleados
CREATE TABLE IF NOT EXISTS employees (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_code VARCHAR(50) UNIQUE NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    phone VARCHAR(20),
    department VARCHAR(100),
    position VARCHAR(100),
    status ENUM('ACTIVE', 'INACTIVE', 'SUSPENDED') DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_employee_code (employee_code),
    INDEX idx_email (email),
    INDEX idx_status (status)
);

-- Tabla de accesos
CREATE TABLE IF NOT EXISTS access_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    employee_code VARCHAR(50) NOT NULL,
    access_type ENUM('ENTRY', 'EXIT') NOT NULL,
    access_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    location VARCHAR(100),
    device_id VARCHAR(100),
    status ENUM('SUCCESS', 'FAILED', 'BLOCKED') DEFAULT 'SUCCESS',
    notes TEXT,
    FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE,
    INDEX idx_employee_id (employee_id),
    INDEX idx_employee_code (employee_code),
    INDEX idx_access_timestamp (access_timestamp),
    INDEX idx_access_type (access_type)
);

-- Tabla de usuarios del sistema
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
    FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE SET NULL,
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_status (status)
);

-- Tabla de sesiones (opcional, también puede estar en MongoDB)
CREATE TABLE IF NOT EXISTS user_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    session_token VARCHAR(255) UNIQUE NOT NULL,
    refresh_token VARCHAR(255) UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES system_users(id) ON DELETE CASCADE,
    INDEX idx_session_token (session_token),
    INDEX idx_user_id (user_id),
    INDEX idx_expires_at (expires_at)
);

-- Tabla de alertas
CREATE TABLE IF NOT EXISTS alert (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    alert_id VARCHAR(100) UNIQUE NOT NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    description TEXT,
    code VARCHAR(50) NOT NULL,
    username VARCHAR(100),
    employee_code VARCHAR(50),
    ip_address VARCHAR(50),
    INDEX idx_code (code),
    INDEX idx_username (username),
    INDEX idx_employee_code (employee_code),
    INDEX idx_timestamp (timestamp)
);

-- Insertar datos de ejemplo
INSERT INTO employees (employee_code, first_name, last_name, email, phone, department, position, status) VALUES
('EMP001', 'Juan', 'Pérez', 'juan.perez@empresa.com', '555-0101', 'IT', 'Desarrollador', 'ACTIVE'),
('EMP002', 'María', 'González', 'maria.gonzalez@empresa.com', '555-0102', 'RRHH', 'Analista', 'ACTIVE'),
('EMP003', 'Carlos', 'Rodríguez', 'carlos.rodriguez@empresa.com', '555-0103', 'Ventas', 'Ejecutivo', 'ACTIVE');

INSERT INTO system_users (username, email, password_hash, role, employee_id, status) VALUES
('admin', 'admin@empresa.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ADMIN', NULL, 'ACTIVE');
-- Password: admin123

