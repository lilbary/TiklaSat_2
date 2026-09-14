-- =====================================================================
-- TıklaSat · Repeatable Migration · Zengin Örnek Veriler (Seed Data)
-- ---------------------------------------------------------------------
-- Bu dosya her değiştiğinde Flyway tarafından baştan çalıştırılır.
-- Eski örnek veriler silinip güncel olanlar eklenir.
-- =====================================================================

-- =====================================================================
-- 1. TEMİZLİK (Eski örnek verileri sil)
-- =====================================================================
-- Sadece demo e-postalarına sahip kullanıcıların verilerini siliyoruz ki
-- gerçek kullanıcıların verilerine zarar gelmesin.

DELETE FROM bids WHERE bidder_id IN (SELECT id FROM users WHERE email LIKE 'demo.%@tiklasat.com')
                    OR auction_id IN (SELECT a.id FROM auctions a JOIN listings l ON a.listing_id = l.id JOIN users u ON l.seller_id = u.id WHERE u.email LIKE 'demo.%@tiklasat.com');

DELETE FROM notifications WHERE user_id IN (SELECT id FROM users WHERE email LIKE 'demo.%@tiklasat.com')
                             OR auction_id IN (SELECT a.id FROM auctions a JOIN listings l ON a.listing_id = l.id JOIN users u ON l.seller_id = u.id WHERE u.email LIKE 'demo.%@tiklasat.com');
DELETE FROM favorites WHERE user_id IN (SELECT id FROM users WHERE email LIKE 'demo.%@tiklasat.com')
                         OR auction_id IN (SELECT a.id FROM auctions a JOIN listings l ON a.listing_id = l.id JOIN users u ON l.seller_id = u.id WHERE u.email LIKE 'demo.%@tiklasat.com');

DELETE FROM addresses WHERE user_id IN (SELECT id FROM users WHERE email LIKE 'demo.%@tiklasat.com');
DELETE FROM auctions WHERE listing_id IN (SELECT l.id FROM listings l JOIN users u ON l.seller_id = u.id WHERE u.email LIKE 'demo.%@tiklasat.com');
DELETE FROM listing_images WHERE listing_id IN (SELECT l.id FROM listings l JOIN users u ON l.seller_id = u.id WHERE u.email LIKE 'demo.%@tiklasat.com');
DELETE FROM listings WHERE seller_id IN (SELECT id FROM users WHERE email LIKE 'demo.%@tiklasat.com');
DELETE FROM user_roles WHERE user_id IN (SELECT id FROM users WHERE email LIKE 'demo.%@tiklasat.com');
DELETE FROM users WHERE email LIKE 'demo.%@tiklasat.com';


-- =====================================================================
-- 2. KULLANICILARI OLUŞTUR
-- =====================================================================
INSERT INTO users (id, email, full_name, password_hash, phone, created_at) VALUES
    ('11111111-1111-1111-1111-111111111111', 'demo.satici@tiklasat.com', 'Demo Satıcı', '$2a$10$wOItR2aQyT10xW7D0I.V2eY/XwG3E.7Tz36m8t0k.mX/1.5M/03jO', '5551112233', now()),
    ('22222222-2222-2222-2222-222222222222', 'demo.alici@tiklasat.com',  'Demo Alıcı',  '$2a$10$wOItR2aQyT10xW7D0I.V2eY/XwG3E.7Tz36m8t0k.mX/1.5M/03jO', '5559998877', now());

-- Rolleri ekle (BUYER, SELLER)
INSERT INTO user_roles (user_id, role) VALUES
    ('11111111-1111-1111-1111-111111111111', 'BUYER'),
    ('11111111-1111-1111-1111-111111111111', 'SELLER'),
    ('22222222-2222-2222-2222-222222222222', 'BUYER');


-- =====================================================================
-- 3. İLANLARI OLUŞTUR (JSONB Attributes ile Birlikte)
-- =====================================================================
INSERT INTO listings (id, seller_id, category_id, title, description, status, view_count, attributes, created_at) VALUES
    -- 3.1. Otomobil İlanı
    ('aaaaaaaa-1111-1111-1111-aaaaaaaaaaaa', '11111111-1111-1111-1111-111111111111', (SELECT id FROM categories WHERE slug = 'otomobil'),
     '2021 BMW 320i M Sport',
     'İlk sahibinden, kazasız boyasız kapalı garaj arabası. Borusan çıkışlı. Tüm bakımları yetkili serviste yapılmıştır. Yedek anahtarı mevcuttur.',
     'APPROVED', 145,
     '{"marka": "BMW", "model_yili": 2021, "kilometre": 45000, "yakit_tipi": "Benzin", "vites": "Otomatik", "kasa_tipi": "Sedan", "renk": "Lacivert"}'::jsonb,
     now()),

    -- 3.2. Laptop İlanı
    ('aaaaaaaa-2222-2222-2222-aaaaaaaaaaaa', '11111111-1111-1111-1111-111111111111', (SELECT id FROM categories WHERE slug = 'laptop'),
     'MacBook Pro M2 16GB RAM 512GB SSD',
     'Yazılımcıdan çok temiz cihaz, pil devri henüz 45. Herhangi bir çiziği veya deformasyonu yoktur. Kutusu ve orijinal şarj adaptörü ile verilecektir.',
     'APPROVED', 89,
     '{"islemci": "Apple M2", "ram": "16 GB", "depolama": "512 GB SSD", "ekran_boyutu": "14 inç", "ekran_karti": "Apple 10 Çekirdekli GPU"}'::jsonb,
     now()),

    -- 3.3. Apple Telefon İlanı
    ('aaaaaaaa-3333-3333-3333-aaaaaaaaaaaa', '11111111-1111-1111-1111-111111111111', (SELECT id FROM categories WHERE slug = 'apple-telefonlar'),
     'iPhone 15 Pro Max 256GB Natürel Titanyum',
     'Sıfır ayarında, hiç kılıfsız ve ekran koruyucusuz kullanılmadı. Garanti süresi 1 yıl daha devam ediyor. Kutusu, faturası tamdır.',
     'APPROVED', 210,
     '{"dahili_hafiza": "256 GB", "renk": "Natürel Titanyum", "garanti_durumu": "Garantisi Var"}'::jsonb,
     now());


-- =====================================================================
-- 4. İLAN FOTOĞRAFLARINI OLUŞTUR (listing_images)
-- =====================================================================
INSERT INTO listing_images (listing_id, image_url, is_primary) VALUES
    -- Otomobil Fotoğrafları
    ('aaaaaaaa-1111-1111-1111-aaaaaaaaaaaa', 'https://images.unsplash.com/photo-1555215695-3004980ad54e?auto=format&fit=crop&w=800&q=80', true),
    ('aaaaaaaa-1111-1111-1111-aaaaaaaaaaaa', 'https://images.unsplash.com/photo-1580273916550-e323be2ae537?auto=format&fit=crop&w=800&q=80', false),
    ('aaaaaaaa-1111-1111-1111-aaaaaaaaaaaa', 'https://images.unsplash.com/photo-1606159068539-43f36b99d1b2?auto=format&fit=crop&w=800&q=80', false),

    -- Laptop Fotoğrafları
    ('aaaaaaaa-2222-2222-2222-aaaaaaaaaaaa', 'https://images.unsplash.com/photo-1517336714731-489689fd1ca8?auto=format&fit=crop&w=800&q=80', true),
    ('aaaaaaaa-2222-2222-2222-aaaaaaaaaaaa', 'https://images.unsplash.com/photo-1611186871348-b1ce696e52c9?auto=format&fit=crop&w=800&q=80', false),

    -- Telefon Fotoğrafları
    ('aaaaaaaa-3333-3333-3333-aaaaaaaaaaaa', 'https://images.unsplash.com/photo-1695048133142-1a20484d2569?auto=format&fit=crop&w=800&q=80', true),
    ('aaaaaaaa-3333-3333-3333-aaaaaaaaaaaa', 'https://images.unsplash.com/photo-1695048064977-fb3a8e104f67?auto=format&fit=crop&w=800&q=80', false);


-- =====================================================================
-- 5. AÇIK ARTIRMALARI BAŞLAT (auctions)
-- =====================================================================
INSERT INTO auctions (id, listing_id, start_price, current_price, starts_at, ends_at, original_ends_at, reserve_price, status, created_at, extension_count, ending_soon_notified) VALUES
    -- 5.1. Otomobil (Yarın Bitiyor, Aktif)
    (gen_random_uuid(), 'aaaaaaaa-1111-1111-1111-aaaaaaaaaaaa', 1200000.00, 1200000.00, 
     now() - interval '2 days', now() + interval '1 day', now() + interval '1 day', 
     1500000.00, 'ACTIVE', now(), 0, false),

    -- 5.2. Laptop (3 Saat Sonra Bitiyor - Ending Soon testi için)
    (gen_random_uuid(), 'aaaaaaaa-2222-2222-2222-aaaaaaaaaaaa', 45000.00, 48000.00, 
     now() - interval '4 days', now() + interval '3 hours', now() + interval '3 hours', 
     NULL, 'ACTIVE', now(), 0, false),

    -- 5.3. Telefon (1 Hafta Sonra Bitiyor)
    (gen_random_uuid(), 'aaaaaaaa-3333-3333-3333-aaaaaaaaaaaa', 50000.00, 50000.00, 
     now(), now() + interval '7 days', now() + interval '7 days', 
     55000.00, 'ACTIVE', now(), 0, false);


-- =====================================================================
-- 6. ÖRNEK TEKLİFLER (bids) (İsteğe Bağlı)
-- =====================================================================
-- Laptop ilanına bir deneme teklifi yapalım
INSERT INTO bids (id, auction_id, bidder_id, amount, created_at)
SELECT gen_random_uuid(), id, '22222222-2222-2222-2222-222222222222', 48000.00, now()
FROM auctions WHERE listing_id = 'aaaaaaaa-2222-2222-2222-aaaaaaaaaaaa';
