-- =====================================================================
-- TıklaSat · V2 · ShedLock Kilit Tablosu
-- ---------------------------------------------------------------------
-- ShedLock kütüphanesi bu tabloyu kullanarak zamanlanmış görevlerin
-- (scheduled jobs) aynı anda sadece bir sunucu tarafından
-- çalıştırılmasını garanti eder.
-- =====================================================================

CREATE TABLE shedlock (
    name       VARCHAR(64)   NOT NULL,
    lock_until TIMESTAMP     NOT NULL,
    locked_at  TIMESTAMP     NOT NULL,
    locked_by  VARCHAR(255)  NOT NULL,
    PRIMARY KEY (name)
);
