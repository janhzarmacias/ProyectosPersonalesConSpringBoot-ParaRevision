-- V2__add_consent_columns.sql
-- Agrega campos de consentimiento (términos y privacidad) al usuario

ALTER TABLE auth.user_auth
    ADD COLUMN IF NOT EXISTS terms_accepted BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS privacy_accepted BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS terms_version VARCHAR(32),
    ADD COLUMN IF NOT EXISTS privacy_version VARCHAR(32),
    ADD COLUMN IF NOT EXISTS consent_at TIMESTAMP NULL;

-- Opcional: si ya existían usuarios y marcaste aceptaciones por otro flujo,
-- normaliza el timestamp de consentimiento:
UPDATE auth.user_auth
SET consent_at = NOW()
WHERE (terms_accepted = TRUE OR privacy_accepted = TRUE)
  AND consent_at IS NULL;

-- Índices: normalmente no necesarios para estos flags; si a futuro filtras por versión,
-- añade uno compuesto (opcional):
-- CREATE INDEX IF NOT EXISTS idx_user_auth_terms_version ON auth.user_auth (terms_version);
-- CREATE INDEX IF NOT EXISTS idx_user_auth_privacy_version ON auth.user_auth (privacy_version);
