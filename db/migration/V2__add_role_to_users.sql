-- =====================================================================
-- TıklaSat · V2 · users tablosuna role kolonu
-- ---------------------------------------------------------------------
-- User.java'ya JWT/güvenlik implementasyonuyla birlikte yeni bir alan
-- eklendi: role (Role enum, @Enumerated(EnumType.STRING)). V1'de bu
-- kolon yoktu — V1'e dokunmuyoruz, ileriye doğru V2 ile ekliyoruz.
--
-- DEFAULT 'USER' zorunlu: tabloda zaten satır varsa (bizim test
-- kullanıcımız gibi), yeni NOT NULL kolon için PostgreSQL bir değer
-- ister. Varsayılan, Java tarafındaki `Role role = Role.USER;` ile
-- aynı davranışı yansıtıyor.
-- =====================================================================

ALTER TABLE users ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT 'USER';
