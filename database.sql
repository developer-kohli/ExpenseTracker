-- =============================================================
--  ExpenseTracker – MySQL 8.x Database Schema
--  File   : database.sql
--  Usage  : mysql -u <user> -p < database.sql
-- =============================================================

-- -------------------------------------------------------------
-- 0. Database
-- -------------------------------------------------------------
CREATE DATABASE IF NOT EXISTS expense_tracker
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE expense_tracker;

-- -------------------------------------------------------------
-- 1. users
--    Stores registered application users.
--    Passwords must be stored as a secure hash (e.g. BCrypt)
--    — never in plain text.
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    id         INT          NOT NULL AUTO_INCREMENT,
    name       VARCHAR(100) NOT NULL,
    email      VARCHAR(100) NOT NULL,
    password   VARCHAR(255) NOT NULL,          -- BCrypt / hashed value

    CONSTRAINT pk_users        PRIMARY KEY (id),
    CONSTRAINT uq_users_email  UNIQUE      (email)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- -------------------------------------------------------------
-- 2. expenses
--    Each row represents one expense recorded by a user.
--    Deleting a user automatically removes all their expenses
--    (ON DELETE CASCADE on the foreign key).
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS expenses (
    id           INT            NOT NULL AUTO_INCREMENT,
    user_id      INT            NOT NULL,
    amount       DECIMAL(10, 2) NOT NULL,
    category     VARCHAR(50)    NOT NULL,
    expense_date DATE           NOT NULL,
    description  VARCHAR(255)            DEFAULT NULL,
    created_at   TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_expenses          PRIMARY KEY (id),
    CONSTRAINT fk_expenses_user_id  FOREIGN KEY (user_id)
        REFERENCES users (id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- -------------------------------------------------------------
-- 3. Indexes
--    • idx_expenses_user_id    – fast look-up of a user's expenses
--    • idx_expenses_date       – date-range queries / date-wise view
--    • idx_expenses_category   – category-wise summary / filter
--    • idx_expenses_user_date  – composite: user + date (most common
--                                query pattern in dashboards)
-- -------------------------------------------------------------
CREATE INDEX idx_expenses_user_id  ON expenses (user_id);
CREATE INDEX idx_expenses_date     ON expenses (expense_date);
CREATE INDEX idx_expenses_category ON expenses (category);
CREATE INDEX idx_expenses_user_date
    ON expenses (user_id, expense_date);   -- composite for dashboard queries

-- =============================================================
--  End of schema
-- =============================================================
