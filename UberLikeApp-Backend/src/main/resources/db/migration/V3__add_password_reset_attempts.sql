-- V3__add_password_reset_attempts.sql
ALTER TABLE auth.password_reset_tokens
    ADD COLUMN IF NOT EXISTS attempts INT NOT NULL DEFAULT 0;