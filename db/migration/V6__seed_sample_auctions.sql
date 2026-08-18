-- =====================================================================
-- TıklaSat · V6 · Örnek Açık Artırmalar (Seed Data)
-- ---------------------------------------------------------------------
-- V5'teki 4 ilana birer açık artırma açıyor. Bitiş zamanları bilinçli
-- olarak farklı (bazısı bugün, bazısı bu hafta bitiyor) ki Ana
-- Sayfa'daki "Bugün Bitecekler" / "Bu Hafta Bitecekler" bölümlerini
-- gerçek veriyle test edebilelim.
--
-- V5'te ilan id'leri sabit değil (gen_random_uuid() ile üretildi),
-- bu yüzden burada başlıktan bulup bağlıyoruz.
-- =====================================================================

INSERT INTO auctions (id, listing_id, start_price, starts_at, ends_at, status, created_at) VALUES
    (gen_random_uuid(),
     (SELECT id FROM listings WHERE title = 'iPhone 14 Pro 256GB Uzay Grisi'),
     15000.00, now() - interval '2 days', now() + interval '3 hours', 'ACTIVE', now()),

    (gen_random_uuid(),
     (SELECT id FROM listings WHERE title = 'Sınırlı Sayı Deri Ceket'),
     2500.00, now() - interval '12 hours', now() + interval '8 hours', 'ACTIVE', now()),

    (gen_random_uuid(),
     (SELECT id FROM listings WHERE title = '2019 Model Otomatik Vites Sedan'),
     450000.00, now() - interval '1 day', now() + interval '2 days', 'ACTIVE', now()),

    (gen_random_uuid(),
     (SELECT id FROM listings WHERE title = 'Vintage Ahşap Yemek Masası Takımı'),
     8000.00, now() - interval '3 days', now() + interval '5 days', 'ACTIVE', now());
