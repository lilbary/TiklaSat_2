-- 1. Eski süresi biten açık artırmaların süresini 10 gün daha uzatalım ki ekranda görünsünler
UPDATE auctions SET ends_at = now() + interval '10 days' WHERE status = 'ACTIVE';

-- 2. Yeni eklediğimiz ilanlar (MacBook, Egea vs.) için müzayedeleri (Auctions) başlatalım
INSERT INTO auctions (id, listing_id, start_price, starts_at, ends_at, status, created_at) VALUES
    (gen_random_uuid(), (SELECT id FROM listings WHERE title LIKE '%MacBook%'), 40000.00, now(), now() + interval '2 days', 'ACTIVE', now()),
    (gen_random_uuid(), (SELECT id FROM listings WHERE title LIKE '%Samsung%'), 45000.00, now(), now() + interval '5 hours', 'ACTIVE', now()),
    (gen_random_uuid(), (SELECT id FROM listings WHERE title LIKE '%Egea%'), 800000.00, now(), now() + interval '7 days', 'ACTIVE', now()),
    (gen_random_uuid(), (SELECT id FROM listings WHERE title LIKE '%IKEA%'), 2000.00, now(), now() + interval '12 hours', 'ACTIVE', now()),
    (gen_random_uuid(), (SELECT id FROM listings WHERE title LIKE '%PlayStation%'), 15000.00, now(), now() + interval '3 days', 'ACTIVE', now());
