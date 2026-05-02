-- ============================================================
-- V2__seed_initial_data.sql
-- Dados iniciais: departamentos e usuário admin
-- ============================================================

-- Departamentos iniciais
INSERT INTO departments (name, description) VALUES
    ('Tecnologia',       'Desenvolvimento de software e infraestrutura'),
    ('Recursos Humanos', 'Gestão de pessoas e processos'),
    ('Financeiro',       'Contabilidade, orçamento e pagamentos'),
    ('Comercial',        'Vendas e relacionamento com clientes'),
    ('Operações',        'Logística e processos operacionais');

-- Funcionários de exemplo
INSERT INTO employees (first_name, last_name, email, phone, hire_date, salary, position, department_id) VALUES
    ('Admin',   'Sistema', 'admin@hrsystem.com',   '(81) 99999-0000', '2020-01-01', 0.00, 'Administrador', NULL),
    ('Maria',   'Silva',   'maria@hrsystem.com',   '(81) 99888-1111', '2021-03-15', 7500.00, 'Desenvolvedora', 1),
    ('João',    'Santos',  'joao@hrsystem.com',    '(81) 99777-2222', '2022-06-01', 6800.00, 'Analista de RH', 2),
    ('Ana',     'Costa',   'ana@hrsystem.com',     '(81) 99666-3333', '2021-09-10', 8200.00, 'Gerente Financeira', 3);

-- Usuário admin (senha: Admin@1234 em BCrypt)
INSERT INTO users (username, email, password, role, employee_id) VALUES
    ('admin', 'admin@hrsystem.com', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LeOcMYH7c/wY8K0uq', 'ADMIN', 1);
