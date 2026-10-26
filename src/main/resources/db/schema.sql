-- Esquema inicial de AlphaBackend (H2). Idempotente: se ejecuta en cada arranque.

CREATE TABLE IF NOT EXISTS permissions (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(80)  NOT NULL UNIQUE,
    description VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS roles (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(60)  NOT NULL UNIQUE,
    description VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS role_permissions (
    role_id       BIGINT NOT NULL REFERENCES roles (id),
    permission_id BIGINT NOT NULL REFERENCES permissions (id),
    PRIMARY KEY (role_id, permission_id)
);

CREATE TABLE IF NOT EXISTS users (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name        VARCHAR(150) NOT NULL,
    email            VARCHAR(150) NOT NULL UNIQUE,
    password_hash    VARCHAR(100) NOT NULL,
    photo_url        VARCHAR(500),
    academic_program VARCHAR(150),
    currency         VARCHAR(3)   NOT NULL DEFAULT 'COP',
    enabled          BOOLEAN      NOT NULL DEFAULT 1,
    created_at       TIMESTAMP    NOT NULL
);

CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL REFERENCES users (id),
    role_id BIGINT NOT NULL REFERENCES roles (id),
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE IF NOT EXISTS accounts (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT         NOT NULL REFERENCES users (id),
    name            VARCHAR(100)   NOT NULL,
    type            VARCHAR(20)    NOT NULL CHECK (type IN ('CASH', 'BANK', 'WALLET')),
    initial_balance DECIMAL(19, 2) NOT NULL DEFAULT 0,
    current_balance DECIMAL(19, 2) NOT NULL DEFAULT 0,
    archived        BOOLEAN        NOT NULL DEFAULT 0,
    created_at      TIMESTAMP      NOT NULL
);

CREATE TABLE IF NOT EXISTS categories (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(100) NOT NULL,
    type          VARCHAR(20)  NOT NULL CHECK (type IN ('INCOME', 'EXPENSE')),
    owner_user_id BIGINT REFERENCES users (id)
);

CREATE TABLE IF NOT EXISTS recurring_transactions (
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id              BIGINT         NOT NULL REFERENCES users (id),
    account_id           BIGINT         NOT NULL REFERENCES accounts (id),
    category_id          BIGINT         NOT NULL REFERENCES categories (id),
    amount               DECIMAL(19, 2) NOT NULL,
    periodicity          VARCHAR(20)    NOT NULL CHECK (periodicity IN ('WEEKLY', 'BIWEEKLY', 'MONTHLY')),
    next_occurrence_date DATE           NOT NULL,
    status               VARCHAR(20)    NOT NULL CHECK (status IN ('ACTIVE', 'SUSPENDED', 'CANCELLED')),
    created_at           TIMESTAMP      NOT NULL
);

CREATE TABLE IF NOT EXISTS movements (
    id                       BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id                  BIGINT         NOT NULL REFERENCES users (id),
    account_id               BIGINT         NOT NULL REFERENCES accounts (id),
    category_id              BIGINT         NOT NULL REFERENCES categories (id),
    recurring_transaction_id BIGINT REFERENCES recurring_transactions (id),
    type                     VARCHAR(20)    NOT NULL CHECK (type IN ('INCOME', 'EXPENSE')),
    amount                   DECIMAL(19, 2) NOT NULL,
    movement_date            DATE           NOT NULL,
    description              VARCHAR(500),
    receipt_image_url        VARCHAR(500),
    created_at               TIMESTAMP      NOT NULL
);

CREATE TABLE IF NOT EXISTS budgets (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id      BIGINT         NOT NULL REFERENCES users (id),
    category_id  BIGINT         NOT NULL REFERENCES categories (id),
    budget_year  INTEGER        NOT NULL,
    budget_month INTEGER        NOT NULL CHECK (budget_month BETWEEN 1 AND 12),
    limit_amount DECIMAL(19, 2) NOT NULL,
    UNIQUE (user_id, category_id, budget_year, budget_month)
);

CREATE TABLE IF NOT EXISTS debts (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id           BIGINT         NOT NULL REFERENCES users (id),
    counterparty_name VARCHAR(150)   NOT NULL,
    direction         VARCHAR(20)    NOT NULL CHECK (direction IN ('I_OWE', 'OWED_TO_ME')),
    amount            DECIMAL(19, 2) NOT NULL,
    origin_date       DATE           NOT NULL,
    due_date          DATE,
    description       VARCHAR(500),
    status            VARCHAR(20)    NOT NULL CHECK (status IN ('OPEN', 'PAID')),
    created_at        TIMESTAMP      NOT NULL
);

CREATE TABLE IF NOT EXISTS debt_payments (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    debt_id      BIGINT         NOT NULL REFERENCES debts (id),
    amount       DECIMAL(19, 2) NOT NULL,
    payment_date DATE           NOT NULL
);

CREATE TABLE IF NOT EXISTS savings_plans (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    owner_user_id BIGINT         NOT NULL REFERENCES users (id),
    name          VARCHAR(150)   NOT NULL,
    description   VARCHAR(500),
    target_amount DECIMAL(19, 2) NOT NULL,
    target_date   DATE,
    shared        BOOLEAN        NOT NULL DEFAULT 0,
    status        VARCHAR(20)    NOT NULL CHECK (status IN ('ACTIVE', 'CLOSED', 'ARCHIVED')),
    created_at    TIMESTAMP      NOT NULL
);

CREATE TABLE IF NOT EXISTS savings_plan_participants (
    id                     BIGINT AUTO_INCREMENT PRIMARY KEY,
    plan_id                BIGINT      NOT NULL REFERENCES savings_plans (id),
    user_id                BIGINT      NOT NULL REFERENCES users (id),
    individual_goal_amount DECIMAL(19, 2),
    status                 VARCHAR(20) NOT NULL CHECK (status IN ('INVITED', 'ACTIVE', 'REJECTED', 'REMOVED', 'LEFT')),
    invited_at             TIMESTAMP   NOT NULL,
    responded_at           TIMESTAMP,
    UNIQUE (plan_id, user_id)
);

CREATE TABLE IF NOT EXISTS savings_contributions (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    participant_id    BIGINT         NOT NULL REFERENCES savings_plan_participants (id),
    account_id        BIGINT         NOT NULL REFERENCES accounts (id),
    amount            DECIMAL(19, 2) NOT NULL,
    contribution_date DATE           NOT NULL,
    created_at        TIMESTAMP      NOT NULL
);
