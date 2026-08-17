-- =====================================================================
-- TıklaSat · V3 · Kazanan Kolonu
-- ---------------------------------------------------------------------
-- Açık artırmayı kazanan kullanıcıyı tutacak kolon.
-- İhale kapandığında en yüksek teklifi veren kişi buraya yazılır.
-- =====================================================================

ALTER TABLE auctions ADD COLUMN winner_id UUID;

ALTER TABLE auctions ADD CONSTRAINT fk_auctions_winner
    FOREIGN KEY (winner_id) REFERENCES users(id);
