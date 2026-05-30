-- Создание таблиц для OTP Service
-- Выполните в PostgreSQL 17

CREATE DATABASE otp_db;

\c otp_db;

-- Таблица пользователей
CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    login VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('ADMIN', 'USER')),
    email VARCHAR(255),
    phone VARCHAR(20),
    telegram_chat_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Таблица конфигурации OTP (только одна запись)
CREATE TABLE IF NOT EXISTS otp_config (
    id SERIAL PRIMARY KEY,
    code_length INT DEFAULT 6 CHECK (code_length BETWEEN 4 AND 8),
    ttl_seconds INT DEFAULT 300 CHECK (ttl_seconds BETWEEN 30 AND 3600)
);

-- Таблица OTP кодов
CREATE TABLE IF NOT EXISTS otp_codes (
    id SERIAL PRIMARY KEY,
    operation_id VARCHAR(255) NOT NULL,
    code VARCHAR(8) NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('ACTIVE', 'EXPIRED', 'USED')),
    length INT NOT NULL,
    ttl_seconds INT NOT NULL,
    sent_via VARCHAR(50),
    destination VARCHAR(255),
    user_id INT REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    validated_at TIMESTAMP
);

-- Индексы для производительности
CREATE INDEX idx_operation_user ON otp_codes(operation_id, user_id);
CREATE INDEX idx_status_expires ON otp_codes(status, expires_at);
CREATE INDEX idx_user_login ON users(login);

-- Вставка дефолтной конфигурации
INSERT INTO otp_config (code_length, ttl_seconds) VALUES (6, 300);

-- Создание первого администратора (пароль: admin123)
-- Вставьте после запуска приложения через API