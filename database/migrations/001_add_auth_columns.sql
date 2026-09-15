-- Migration: Add authentication and password reset columns to usuario
-- Run this against your existing task_manager database.

ALTER TABLE usuario
  ADD COLUMN senha VARCHAR(255) NULL,
  ADD COLUMN auth_token VARCHAR(255) NULL,
  ADD COLUMN auth_token_expiry DATETIME NULL,
  ADD COLUMN reset_token VARCHAR(255) NULL,
  ADD COLUMN reset_token_expiry DATETIME NULL;

-- Optional: create index on auth_token and reset_token for quick lookup
CREATE INDEX IF NOT EXISTS idx_usuario_auth_token ON usuario (auth_token);
CREATE INDEX IF NOT EXISTS idx_usuario_reset_token ON usuario (reset_token);

-- Notes:
-- 1) Existing users will have NULL passwords. Use the password reset flow to set passwords
--    or run an UPDATE to set a password hash for specific users.
-- 2) In production, ensure these columns are handled securely and consider additional
--    constraints or a dedicated auth table if needed.
