-- Datos iniciales de AlphaBackend. Idempotente: MERGE INTO por clave (id, o la clave natural en tablas de unión).
-- Contraseña de todos los usuarios semilla: Password123

MERGE INTO permissions (id, name, description) KEY (id) VALUES
    (1, 'USER_READ',        'Consultar usuarios'),
    (2, 'USER_WRITE',       'Crear, editar y eliminar usuarios'),
    (3, 'ROLE_READ',        'Consultar roles'),
    (4, 'ROLE_WRITE',       'Crear, editar y eliminar roles'),
    (5, 'PERMISSION_READ',  'Consultar permisos'),
    (6, 'PERMISSION_WRITE', 'Crear, editar y eliminar permisos'),
    (7, 'CATEGORY_MANAGE',  'Administrar categorías predefinidas'),
    (8, 'FINANCE_OWN',      'Gestionar las finanzas personales propias');

MERGE INTO roles (id, name, description) KEY (id) VALUES
    (1, 'ADMIN', 'Administrador de la plataforma'),
    (2, 'USER',  'Estudiante o colaborador Icesi');

MERGE INTO role_permissions (role_id, permission_id) KEY (role_id, permission_id) VALUES
    (1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), (1, 7), (1, 8),
    (2, 1), (2, 8);

MERGE INTO users (id, full_name, email, password_hash, academic_program, currency, enabled, created_at) KEY (id) VALUES
    (1, 'Administrador Alpha',  'admin@u.icesi.edu.co',  '$2a$10$eIaxw/PdF0c/N1q/fj7xWuvIcjlosCWM5EqfStpwtpKmN4Dvz7XK6', 'Bienestar Universitario', 'COP', TRUE, '2026-09-01 08:00:00.000'),
    (2, 'Ana Torres',           'ana.torres@u.icesi.edu.co', '$2a$10$eIaxw/PdF0c/N1q/fj7xWuvIcjlosCWM5EqfStpwtpKmN4Dvz7XK6', 'Ingeniería de Sistemas', 'COP', TRUE, '2026-09-01 08:05:00.000'),
    (3, 'Carlos Gómez',         'carlos.gomez@u.icesi.edu.co', '$2a$10$eIaxw/PdF0c/N1q/fj7xWuvIcjlosCWM5EqfStpwtpKmN4Dvz7XK6', 'Economía', 'COP', TRUE, '2026-09-01 08:10:00.000');

MERGE INTO user_roles (user_id, role_id) KEY (user_id, role_id) VALUES
    (1, 1), (2, 2), (3, 2);

MERGE INTO accounts (id, user_id, name, type, initial_balance, current_balance, archived, created_at) KEY (id) VALUES
    (1, 2, 'Efectivo',      'CASH',   50000,   50000,   FALSE, '2026-09-01 09:00:00.000'),
    (2, 2, 'Bancolombia',   'BANK',   1200000, 1200000, FALSE, '2026-09-01 09:01:00.000'),
    (3, 3, 'Nequi',         'WALLET', 300000,  300000,  FALSE, '2026-09-01 09:02:00.000');

MERGE INTO categories (id, name, type, owner_user_id) KEY (id) VALUES
    (1, 'Alimentación', 'EXPENSE', NULL),
    (2, 'Transporte',   'EXPENSE', NULL),
    (3, 'Educación',    'EXPENSE', NULL),
    (4, 'Ocio',         'EXPENSE', NULL),
    (5, 'Mesada',       'INCOME',  NULL),
    (6, 'Monitoría',    'INCOME',  NULL),
    (7, 'Cafetería favorita', 'EXPENSE', 2);

MERGE INTO recurring_transactions
    (id, user_id, account_id, category_id, amount, periodicity, next_occurrence_date, status, created_at) KEY (id) VALUES
    (1, 2, 2, 5, 800000, 'MONTHLY', '2026-10-01', 'ACTIVE', '2026-09-01 09:10:00.000');

MERGE INTO movements
    (id, user_id, account_id, category_id, recurring_transaction_id, type, amount, movement_date, description, receipt_image_url, created_at) KEY (id) VALUES
    (1, 2, 2, 5, 1,    'INCOME',  800000, '2026-09-01', 'Mesada de septiembre', NULL, '2026-09-01 10:00:00.000'),
    (2, 2, 1, 1, NULL, 'EXPENSE', 18000,  '2026-09-03', 'Almuerzo',              NULL, '2026-09-03 13:00:00.000'),
    (3, 2, 2, 2, NULL, 'EXPENSE', 9000,   '2026-09-04', 'Taxi a la universidad', NULL, '2026-09-04 07:00:00.000'),
    (4, 3, 3, 4, NULL, 'EXPENSE', 35000,  '2026-09-05', 'Cine',                  NULL, '2026-09-05 20:00:00.000');

MERGE INTO budgets (id, user_id, category_id, budget_year, budget_month, limit_amount) KEY (id) VALUES
    (1, 2, 1, 2026, 9, 250000),
    (2, 2, 2, 2026, 9, 100000);

MERGE INTO debts
    (id, user_id, counterparty_name, direction, amount, origin_date, due_date, description, status, created_at) KEY (id) VALUES
    (1, 2, 'Carlos Gómez', 'OWED_TO_ME', 60000, '2026-09-02', '2026-09-30', 'Préstamo para fotocopias', 'OPEN', '2026-09-02 12:00:00.000');

MERGE INTO debt_payments (id, debt_id, amount, payment_date) KEY (id) VALUES
    (1, 1, 20000, '2026-09-10');

MERGE INTO savings_plans
    (id, owner_user_id, name, description, target_amount, target_date, shared, status, created_at) KEY (id) VALUES
    (1, 2, 'Viaje de fin de semestre', 'Vaquita para el paseo', 1000000, '2026-12-01', TRUE, 'ACTIVE', '2026-09-06 15:00:00.000');

MERGE INTO savings_plan_participants
    (id, plan_id, user_id, individual_goal_amount, status, invited_at, responded_at) KEY (id) VALUES
    (1, 1, 2, 500000, 'ACTIVE', '2026-09-06 15:00:00.000', '2026-09-06 15:00:00.000'),
    (2, 1, 3, 500000, 'ACTIVE', '2026-09-06 15:05:00.000', '2026-09-07 09:00:00.000');

MERGE INTO savings_contributions (id, participant_id, account_id, amount, contribution_date, created_at) KEY (id) VALUES
    (1, 1, 2, 100000, '2026-09-08', '2026-09-08 10:00:00.000'),
    (2, 2, 3, 50000,  '2026-09-09', '2026-09-09 10:00:00.000');

-- H2 no avanza el contador AUTO_INCREMENT con inserciones de id explícito: se resincroniza tras la carga.
ALTER TABLE permissions ALTER COLUMN id RESTART WITH (SELECT COALESCE(MAX(id), 0) + 1 FROM permissions);
ALTER TABLE roles ALTER COLUMN id RESTART WITH (SELECT COALESCE(MAX(id), 0) + 1 FROM roles);
ALTER TABLE users ALTER COLUMN id RESTART WITH (SELECT COALESCE(MAX(id), 0) + 1 FROM users);
ALTER TABLE accounts ALTER COLUMN id RESTART WITH (SELECT COALESCE(MAX(id), 0) + 1 FROM accounts);
ALTER TABLE categories ALTER COLUMN id RESTART WITH (SELECT COALESCE(MAX(id), 0) + 1 FROM categories);
ALTER TABLE recurring_transactions ALTER COLUMN id RESTART WITH (SELECT COALESCE(MAX(id), 0) + 1 FROM recurring_transactions);
ALTER TABLE movements ALTER COLUMN id RESTART WITH (SELECT COALESCE(MAX(id), 0) + 1 FROM movements);
ALTER TABLE budgets ALTER COLUMN id RESTART WITH (SELECT COALESCE(MAX(id), 0) + 1 FROM budgets);
ALTER TABLE debts ALTER COLUMN id RESTART WITH (SELECT COALESCE(MAX(id), 0) + 1 FROM debts);
ALTER TABLE debt_payments ALTER COLUMN id RESTART WITH (SELECT COALESCE(MAX(id), 0) + 1 FROM debt_payments);
ALTER TABLE savings_plans ALTER COLUMN id RESTART WITH (SELECT COALESCE(MAX(id), 0) + 1 FROM savings_plans);
ALTER TABLE savings_plan_participants ALTER COLUMN id RESTART WITH (SELECT COALESCE(MAX(id), 0) + 1 FROM savings_plan_participants);
ALTER TABLE savings_contributions ALTER COLUMN id RESTART WITH (SELECT COALESCE(MAX(id), 0) + 1 FROM savings_contributions);
