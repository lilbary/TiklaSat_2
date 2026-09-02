-- =====================================================================
-- TıklaSat · V19 · "Bu Haftanın Kategorileri" için örnek veri
-- ---------------------------------------------------------------------
-- Otomobil, Oyun Konsolları, Mobilya, Apple Telefonlar kategorilerinde
-- çok az ilan olduğu için (1-2 tane), bu bölüm boş/tekdüze görünüyordu.
-- Her kategoriye 3'er gerçekçi ilan + hemen aktif auction ekleniyor.
-- Sabit demo satıcı (V5) ve kategori slug'ları (V5) üzerinden, subquery
-- ile referans veriliyor — ID'ler yine gen_random_uuid() ile üretiliyor.
-- =====================================================================

-- ========================================
-- OTOMOBİL
-- ========================================
INSERT INTO listings (id, seller_id, category_id, title, description, status, view_count, created_at) VALUES
    (gen_random_uuid(), (SELECT id FROM users WHERE email = 'demo.satici@tiklasat.com'), (SELECT id FROM categories WHERE slug = 'otomobil'),
     '2021 Model Dizel Otomatik SUV', 'Tek elden, hasarsız, bakımları yetkili serviste. 45.000 km''de.', 'APPROVED', 18, now()),
    (gen_random_uuid(), (SELECT id FROM users WHERE email = 'demo.satici@tiklasat.com'), (SELECT id FROM categories WHERE slug = 'otomobil'),
     '2018 Model Manuel Hatchback', 'Ekonomik, şehir içi kullanım, düşük yakıt tüketimi.', 'APPROVED', 9, now()),
    (gen_random_uuid(), (SELECT id FROM users WHERE email = 'demo.satici@tiklasat.com'), (SELECT id FROM categories WHERE slug = 'otomobil'),
     '2023 Model Elektrikli Sedan', 'Sıfır ayarında, garantili, hızlı şarj destekli.', 'APPROVED', 41, now());

-- ========================================
-- OYUN KONSOLLARI
-- ========================================
INSERT INTO listings (id, seller_id, category_id, title, description, status, view_count, created_at) VALUES
    (gen_random_uuid(), (SELECT id FROM users WHERE email = 'demo.satici@tiklasat.com'), (SELECT id FROM categories WHERE slug = 'oyun-konsollari'),
     'Xbox Series X 1TB', 'Kutulu, 2 kol, garanti devam ediyor.', 'APPROVED', 27, now()),
    (gen_random_uuid(), (SELECT id FROM users WHERE email = 'demo.satici@tiklasat.com'), (SELECT id FROM categories WHERE slug = 'oyun-konsollari'),
     'Nintendo Switch OLED', 'Az kullanılmış, ekran koruyucu takılı, kılıflı.', 'APPROVED', 33, now()),
    (gen_random_uuid(), (SELECT id FROM users WHERE email = 'demo.satici@tiklasat.com'), (SELECT id FROM categories WHERE slug = 'oyun-konsollari'),
     'PlayStation 4 Pro 1TB', 'Temiz kullanılmış, 2 orijinal kol ile birlikte.', 'APPROVED', 15, now());

-- ========================================
-- MOBİLYA
-- ========================================
INSERT INTO listings (id, seller_id, category_id, title, description, status, view_count, created_at) VALUES
    (gen_random_uuid(), (SELECT id FROM users WHERE email = 'demo.satici@tiklasat.com'), (SELECT id FROM categories WHERE slug = 'mobilya'),
     'Modern L Koltuk Takımı', 'Kumaş döşemeli, yataklı, depolu, taşınma sebebiyle satılık.', 'APPROVED', 22, now()),
    (gen_random_uuid(), (SELECT id FROM users WHERE email = 'demo.satici@tiklasat.com'), (SELECT id FROM categories WHERE slug = 'mobilya'),
     'Ahşap Çalışma Masası', 'Masif meşe, geniş çalışma yüzeyi, ofis/ev için uygun.', 'APPROVED', 11, now()),
    (gen_random_uuid(), (SELECT id FROM users WHERE email = 'demo.satici@tiklasat.com'), (SELECT id FROM categories WHERE slug = 'mobilya'),
     'Vintage Kitaplık', '1970'' lerden kalma, restore edilmiş, 5 raflı.', 'APPROVED', 7, now());

-- ========================================
-- APPLE TELEFONLAR
-- ========================================
INSERT INTO listings (id, seller_id, category_id, title, description, status, view_count, created_at) VALUES
    (gen_random_uuid(), (SELECT id FROM users WHERE email = 'demo.satici@tiklasat.com'), (SELECT id FROM categories WHERE slug = 'apple-telefonlar'),
     'iPhone 15 Pro Max 256GB', 'Sıfır ayarında, faturalı, kutulu, garanti devam ediyor.', 'APPROVED', 58, now()),
    (gen_random_uuid(), (SELECT id FROM users WHERE email = 'demo.satici@tiklasat.com'), (SELECT id FROM categories WHERE slug = 'apple-telefonlar'),
     'iPhone 13 128GB', 'Batarya sağlığı %92, ekran koruyucu ve kılıfla kullanıldı.', 'APPROVED', 29, now()),
    (gen_random_uuid(), (SELECT id FROM users WHERE email = 'demo.satici@tiklasat.com'), (SELECT id FROM categories WHERE slug = 'apple-telefonlar'),
     'iPhone SE 2022 64GB', 'Az kullanılmış, kutu ve aksesuarları eksiksiz.', 'APPROVED', 14, now());

-- ========================================
-- HER İLAN İÇİN AKTİF BİR AÇIK ARTIRMA
-- ========================================
INSERT INTO auctions (id, listing_id, start_price, current_price, starts_at, ends_at, original_ends_at, status, created_at)
SELECT
    gen_random_uuid(),
    l.id,
    p.start_price,
    p.start_price,
    now(),
    now() + p.duration,
    now() + p.duration,
    'ACTIVE',
    now()
FROM listings l
JOIN (VALUES
    ('2021 Model Dizel Otomatik SUV', 650000.00, INTERVAL '3 days'),
    ('2018 Model Manuel Hatchback', 320000.00, INTERVAL '5 days'),
    ('2023 Model Elektrikli Sedan', 1250000.00, INTERVAL '1 day'),
    ('Xbox Series X 1TB', 18000.00, INTERVAL '2 days'),
    ('Nintendo Switch OLED', 9500.00, INTERVAL '4 days'),
    ('PlayStation 4 Pro 1TB', 7000.00, INTERVAL '6 days'),
    ('Modern L Koltuk Takımı', 22000.00, INTERVAL '3 days'),
    ('Ahşap Çalışma Masası', 4500.00, INTERVAL '5 days'),
    ('Vintage Kitaplık', 3200.00, INTERVAL '1 day'),
    ('iPhone 15 Pro Max 256GB', 55000.00, INTERVAL '2 days'),
    ('iPhone 13 128GB', 24000.00, INTERVAL '4 days'),
    ('iPhone SE 2022 64GB', 12000.00, INTERVAL '6 days')
) AS p(title, start_price, duration) ON l.title = p.title
WHERE l.seller_id = (SELECT id FROM users WHERE email = 'demo.satici@tiklasat.com');
