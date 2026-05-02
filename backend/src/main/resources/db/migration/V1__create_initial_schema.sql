-- ============================================================
-- V1__create_initial_schema.sql
-- Criação do schema inicial do Sistema de RH
-- ============================================================

-- Enum para papéis de usuário
CREATE TYPE user_role AS ENUM ('ADMIN', 'MANAGER', 'EMPLOYEE');

-- Enum para status de solicitação de férias
CREATE TYPE vacation_status AS ENUM ('PENDING', 'APPROVED', 'REJECTED', 'CANCELLED');

-- ============================================================
-- Tabela: departments
-- ============================================================
CREATE TABLE departments (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

-- ============================================================
-- Tabela: employees
-- ============================================================
CREATE TABLE employees (
    id            BIGSERIAL PRIMARY KEY,
    first_name    VARCHAR(100) NOT NULL,
    last_name     VARCHAR(100) NOT NULL,
    email         VARCHAR(150) NOT NULL UNIQUE,
    phone         VARCHAR(20),
    hire_date     DATE NOT NULL,
    salary        NUMERIC(12, 2) NOT NULL CHECK (salary >= 0),
    position      VARCHAR(100),
    department_id BIGINT REFERENCES departments(id) ON DELETE SET NULL,
    active        BOOLEAN NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMP NOT NULL DEFAULT NOW()
);

-- ============================================================
-- Tabela: users (autenticação)
-- ============================================================
CREATE TABLE users (
    id          BIGSERIAL PRIMARY KEY,
    username    VARCHAR(80)  NOT NULL UNIQUE,
    email       VARCHAR(150) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    role        user_role    NOT NULL DEFAULT 'EMPLOYEE',
    employee_id BIGINT UNIQUE REFERENCES employees(id) ON DELETE SET NULL,
    active      BOOLEAN NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

-- ============================================================
-- Tabela: vacation_requests
-- ============================================================
CREATE TABLE vacation_requests (
    id          BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
    start_date  DATE NOT NULL,
    end_date    DATE NOT NULL,
    status      vacation_status NOT NULL DEFAULT 'PENDING',
    reason      VARCHAR(500),
    reviewed_by BIGINT REFERENCES users(id) ON DELETE SET NULL,
    reviewed_at TIMESTAMP,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_vacation_dates CHECK (end_date >= start_date)
);

-- ============================================================
-- Índices para performance
-- ============================================================
CREATE INDEX idx_employees_department    ON employees(department_id);
CREATE INDEX idx_employees_email         ON employees(email);
CREATE INDEX idx_employees_active        ON employees(active);
CREATE INDEX idx_users_email             ON users(email);
CREATE INDEX idx_users_employee          ON users(employee_id);
CREATE INDEX idx_vacation_employee       ON vacation_requests(employee_id);
CREATE INDEX idx_vacation_status         ON vacation_requests(status);

-- ============================================================
-- Trigger: atualiza updated_at automaticamente
-- ============================================================
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_departments_updated_at
    BEFORE UPDATE ON departments
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trg_employees_updated_at
    BEFORE UPDATE ON employees
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trg_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trg_vacation_updated_at
    BEFORE UPDATE ON vacation_requests
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
