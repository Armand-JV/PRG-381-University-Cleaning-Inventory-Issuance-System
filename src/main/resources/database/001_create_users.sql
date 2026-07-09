-- =========================================================
-- 001_create_users.sql
-- University Cleaning Inventory & Issuance System
-- Table: users
-- Purpose: Staff accounts for authentication (Register/Login/Logout)
--          and role-based access control (Storekeeper / Supervisor).
-- =========================================================

CREATE TABLE IF NOT EXISTS users (
    user_id         SERIAL PRIMARY KEY,
    full_name       VARCHAR(100)        NOT NULL,
    username        VARCHAR(50)         NOT NULL,
    email           VARCHAR(150)        NOT NULL,
    password_hash   VARCHAR(255)        NOT NULL,
    role            VARCHAR(20)         NOT NULL DEFAULT 'Storekeeper',
    is_active       BOOLEAN             NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_users_username UNIQUE (username),   -- Prevent duplicate usernames
    CONSTRAINT uq_users_email    UNIQUE (email),       -- Prevent duplicate email addresses
    CONSTRAINT chk_users_role    CHECK (role IN ('Storekeeper', 'Supervisor'))
);

-- Speeds up login lookups and duplicate checks
CREATE INDEX IF NOT EXISTS idx_users_username ON users (username);
CREATE INDEX IF NOT EXISTS idx_users_email    ON users (email);

COMMENT ON TABLE users IS 'Staff members who can log in to the system (Storekeeper or Supervisor roles).';
