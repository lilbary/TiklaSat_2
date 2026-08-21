-- current_price: her okumada yeniden hesaplamak yerine, teklif verildikçe
-- güncellenen, önceden hesaplanmış "güncel fiyat" sütunu.
ALTER TABLE auctions ADD COLUMN current_price NUMERIC(15, 2);

-- Mevcut kayıtları doldur: en yüksek teklif varsa onu, yoksa başlangıç fiyatını kullan
UPDATE auctions a
SET current_price = COALESCE(
    (SELECT MAX(amount) FROM bids WHERE auction_id = a.id),
    a.start_price
);

-- Artık boş bırakılamaz
ALTER TABLE auctions ALTER COLUMN current_price SET NOT NULL;