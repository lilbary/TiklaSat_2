ALTER TABLE auctions
    ADD COLUMN original_ends_at  TIMESTAMPTZ,
    ADD COLUMN extension_count   INTEGER NOT NULL DEFAULT 0;

-- Mevcut kayıtlar için original_ends_at'i ends_at'ten doldur
UPDATE auctions SET original_ends_at = ends_at WHERE original_ends_at IS NULL;

-- Artık NOT NULL yapabiliriz
ALTER TABLE auctions ALTER COLUMN original_ends_at SET NOT NULL;

-- Güvenlik kısıtları (ADR-0006'dan)
ALTER TABLE auctions ADD CONSTRAINT ck_auctions_extension_count
    CHECK (extension_count BETWEEN 0 AND 20);

ALTER TABLE auctions ADD CONSTRAINT ck_auctions_extension_window
    CHECK (ends_at <= original_ends_at + INTERVAL '60 minutes');