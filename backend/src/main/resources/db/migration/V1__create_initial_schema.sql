-- ============================================================
-- SCHEMA COMPLETO DO SISTEMA DE RH
-- Execute esse script diretamente no PostgreSQL
-- psql -U postgres -d hr_system_db -f schema.sql
-- ============================================================

-- ============================================================
-- 1. TIPOS ENUM
-- ============================================================
DO $$ BEGIN
    CREATE TYPE user_role AS ENUM ('ADMIN', 'MANAGER', 'EMPLOYEE');
EXCEPTION
    WHEN duplicate_object THEN NULL;
END $$;

DO $$ BEGIN
    CREATE TYPE vacation_status AS ENUM ('PENDING', 'APPROVED', 'REJECTED', 'CANCELLED');
EXCEPTION
    WHEN duplicate_object THEN NULL;
END $$;

-- ============================================================
-- 2. TABELAS
-- ============================================================

CREATE TABLE IF NOT EXISTS departments (
                                           id          BIGSERIAL    PRIMARY KEY,
                                           name        VARCHAR(100) NOT NULL UNIQUE,
                                           description VARCHAR(255),
                                           created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
                                           updated_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS employees (
                                         id            BIGSERIAL      PRIMARY KEY,
                                         first_name    VARCHAR(100)   NOT NULL,
                                         last_name     VARCHAR(100)   NOT NULL,
                                         email         VARCHAR(150)   NOT NULL UNIQUE,
                                         phone         VARCHAR(20),
                                         hire_date     DATE           NOT NULL,
                                         salary        NUMERIC(12, 2) NOT NULL CHECK (salary >= 0),
                                         position      VARCHAR(100),
                                         department_id BIGINT         REFERENCES departments(id) ON DELETE SET NULL,
                                         active        BOOLEAN        NOT NULL DEFAULT TRUE,
                                         created_at    TIMESTAMP      NOT NULL DEFAULT NOW(),
                                         updated_at    TIMESTAMP      NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS users (
                                     id          BIGSERIAL    PRIMARY KEY,
                                     username    VARCHAR(80)  NOT NULL UNIQUE,
                                     email       VARCHAR(150) NOT NULL UNIQUE,
                                     password    VARCHAR(255) NOT NULL,
                                     role        user_role    NOT NULL DEFAULT 'EMPLOYEE',
                                     employee_id BIGINT       UNIQUE REFERENCES employees(id) ON DELETE SET NULL,
                                     active      BOOLEAN      NOT NULL DEFAULT TRUE,
                                     created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
                                     updated_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS vacation_requests (
                                                 id          BIGSERIAL       PRIMARY KEY,
                                                 employee_id BIGINT          NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
                                                 start_date  DATE            NOT NULL,
                                                 end_date    DATE            NOT NULL,
                                                 status      vacation_status NOT NULL DEFAULT 'PENDING',
                                                 reason      VARCHAR(500),
                                                 reviewed_by BIGINT          REFERENCES users(id) ON DELETE SET NULL,
                                                 reviewed_at TIMESTAMP,
                                                 created_at  TIMESTAMP       NOT NULL DEFAULT NOW(),
                                                 updated_at  TIMESTAMP       NOT NULL DEFAULT NOW(),
                                                 CONSTRAINT chk_vacation_dates CHECK (end_date >= start_date)
);

-- ============================================================
-- 3. ÍNDICES
-- ============================================================
CREATE INDEX IF NOT EXISTS idx_employees_department ON employees(department_id);
CREATE INDEX IF NOT EXISTS idx_employees_email      ON employees(email);
CREATE INDEX IF NOT EXISTS idx_employees_active     ON employees(active);
CREATE INDEX IF NOT EXISTS idx_users_email          ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_employee       ON users(employee_id);
CREATE INDEX IF NOT EXISTS idx_vacation_employee    ON vacation_requests(employee_id);
CREATE INDEX IF NOT EXISTS idx_vacation_status      ON vacation_requests(status);

-- ============================================================
-- 4. FUNÇÃO E TRIGGERS DE updated_at
-- ============================================================
CREATE OR REPLACE FUNCTION update_updated_at_column()
    RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_departments_updated_at ON departments;
CREATE TRIGGER trg_departments_updated_at
    BEFORE UPDATE ON departments
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS trg_employees_updated_at ON employees;
CREATE TRIGGER trg_employees_updated_at
    BEFORE UPDATE ON employees
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS trg_users_updated_at ON users;
CREATE TRIGGER trg_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS trg_vacation_updated_at ON vacation_requests;
CREATE TRIGGER trg_vacation_updated_at
    BEFORE UPDATE ON vacation_requests
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ============================================================
-- 5. DADOS INICIAIS
-- ============================================================
INSERT INTO departments (name, description) VALUES
                                                ('Tecnologia',       'Desenvolvimento de software e infraestrutura'),
                                                ('Recursos Humanos', 'Gestão de pessoas e processos'),
                                                ('Financeiro',       'Contabilidade, orçamento e pagamentos'),
                                                ('Comercial',        'Vendas e relacionamento com clientes'),
                                                ('Operações',        'Logística e processos operacionais')
ON CONFLICT (name) DO NOTHING;

INSERT INTO employees (first_name, last_name, email, phone, hire_date, salary, position, department_id)
VALUES ('Admin', 'Sistema', 'admin@hrsystem.com', '(81) 99999-0000', '2020-01-01', 0.00, 'Administrador', NULL)
ON CONFLICT (email) DO NOTHING;

-- Usuário admin — senha: Admin@1234 (BCrypt strength 12)
INSERT INTO users (username, email, password, role, employee_id)
SELECT 'admin', 'admin@hrsystem.com', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LeOcMYH7c/wY8K0uq', 'ADMIN', e.id
FROM employees e WHERE e.email = 'admin@hrsystem.com'
ON CONFLICT (username) DO NOTHING;

-- ============================================================
-- 6. VERIFICAÇÃO FINAL
-- ============================================================
SELECT
    table_name,
    (SELECT COUNT(*) FROM information_schema.columns c
     WHERE c.table_name = t.table_name AND c.table_schema = 'public') AS total_colunas
FROM information_schema.tables t
WHERE table_schema = 'public'
  AND table_type = 'BASE TABLE'
ORDER BY table_name;