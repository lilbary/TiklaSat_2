-- =====================================================================
-- TıklaSat · V4 · Kullanıcı Rol Kolonu
-- ---------------------------------------------------------------------
-- User entity'sindeki role alanı (USER, ADMIN) için eksik kolon.
-- =====================================================================

ALTER TABLE users ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT 'USER';
