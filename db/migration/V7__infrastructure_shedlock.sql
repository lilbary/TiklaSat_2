-- =====================================================================
-- TıklaSat · V7 · Altyapı: ShedLock
-- ---------------------------------------------------------------------
-- Kapsanan kural: BR-A-007 (açık artırmayı sistem kapatır)
-- =====================================================================
--
-- PROBLEM
-- Uygulama üretimde tek kopya çalışmaz; yük dengeleme ve kesintisiz
-- dağıtım için 2+ kopya (instance) aynı anda ayaktadır. Zamanlanmış
-- kapatma işi HER KOPYADA tetiklenir → 3 sunucu aynı açık artırmayı
-- 3 kez kapatmaya çalışır.
--
-- İKİ KATMANLI ÇÖZÜM
--   1. ShedLock (bu tablo): işin kendisi aynı anda yalnızca BİR kopyada
--      başlar. Kaba taneli, iş seviyesinde kilit.
--   2. FOR UPDATE SKIP LOCKED (sorgu içinde): ShedLock devre dışı kalsa
--      veya kilit süresi dolsa bile aynı ARTIRMA satırı iki kez
--      işlenemez. İnce taneli, satır seviyesinde kilit.
--
-- Tek katman yeterli olmaz: ShedLock kilidi zaman aşımına dayanır
-- (sunucu donarsa kilit süresi dolar ve başka kopya devralır) — tam o
-- anda donmuş sunucu uyanırsa iki kopya birlikte çalışabilir.
-- SKIP LOCKED bu boşluğu veritabanı seviyesinde kapatır.
-- =====================================================================

-- Şema, ShedLock kütüphanesinin JdbcTemplateLockProvider'ının beklediği
-- yapıdır. Kütüphanenin şemayı kendisi oluşturmasına GÜVENİLMEZ:
-- şema değişiklikleri versiyon kontrolünde ve gözden geçirilebilir
-- olmalıdır (bkz. ADR-0002 · ddl-auto neden kullanılmıyor).
CREATE TABLE shedlock (
    name       VARCHAR(64)  NOT NULL,
    lock_until TIMESTAMPTZ  NOT NULL,
    locked_at  TIMESTAMPTZ  NOT NULL,
    locked_by  VARCHAR(255) NOT NULL,
    PRIMARY KEY (name)
);

COMMENT ON TABLE  shedlock            IS 'BR-A-007 · Zamanlanmış işlerin dağıtık kilidi. ShedLock kütüphanesi yönetir.';
COMMENT ON COLUMN shedlock.name       IS 'İş adı, örn. auctionClosingJob';
COMMENT ON COLUMN shedlock.lock_until IS 'Kilidin otomatik düşeceği an. Sunucu donarsa başka kopya devralır.';
COMMENT ON COLUMN shedlock.locked_by  IS 'Kilidi tutan kopyanın kimliği (hostname/pod adı) — hata ayıklama için.';
