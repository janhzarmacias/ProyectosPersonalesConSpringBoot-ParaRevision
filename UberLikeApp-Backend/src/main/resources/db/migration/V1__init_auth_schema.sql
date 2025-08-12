-- V1__initial_schema.sql (ya listo para soft delete + unicidad parcial)

CREATE SCHEMA IF NOT EXISTS auth;

CREATE TABLE IF NOT EXISTS auth.user_auth (
                                              id BIGSERIAL PRIMARY KEY,
                                              email VARCHAR(255),
    phone_number VARCHAR(20),
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    enabled BOOLEAN NOT NULL,
    is_email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    is_phone_number_verified BOOLEAN NOT NULL DEFAULT FALSE,
    verification_code VARCHAR(10),
    verification_code_expires_at TIMESTAMP,
    provider VARCHAR(50) DEFAULT 'LOCAL' NOT NULL,
    provider_id VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT chk_email_or_phone CHECK (email IS NOT NULL OR phone_number IS NOT NULL)
    );

-- OJO: NO pongas UNIQUE en email/phone_number, porque usaremos índices únicos parciales más abajo

CREATE UNIQUE INDEX IF NOT EXISTS user_auth_email_unique_not_deleted
    ON auth.user_auth(email)
    WHERE deleted = false AND email IS NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS user_auth_phone_unique_not_deleted
    ON auth.user_auth(phone_number)
    WHERE deleted = false AND phone_number IS NOT NULL;

CREATE TABLE IF NOT EXISTS auth.password_reset_tokens (
                                                          id BIGSERIAL PRIMARY KEY,
                                                          token VARCHAR(255) UNIQUE NOT NULL,
    user_id BIGINT NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    is_used BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_user_auth FOREIGN KEY (user_id) REFERENCES auth.user_auth (id) ON DELETE CASCADE
    );

CREATE INDEX IF NOT EXISTS idx_user_auth_provider_id ON auth.user_auth(provider_id);
CREATE INDEX IF NOT EXISTS idx_user_auth_email_deleted ON auth.user_auth (email, deleted);
CREATE INDEX IF NOT EXISTS idx_user_auth_phone_deleted ON auth.user_auth (phone_number, deleted);
CREATE INDEX IF NOT EXISTS idx_user_auth_providerid_deleted ON auth.user_auth (provider_id, deleted);
