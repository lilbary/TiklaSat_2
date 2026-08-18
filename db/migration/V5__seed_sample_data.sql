-- =====================================================================
-- TıklaSat · V5 · Örnek Veri (Seed Data)
-- ---------------------------------------------------------------------
-- Ana sayfayı geliştirirken gösterecek gerçek bir şey olsun diye
-- birkaç örnek kategori ve ilan. Sabit UUID'ler kullanılıyor ki bu
-- migration her ortamda (kimin makinesinde çalışırsa çalışsın) aynı,
-- öngörülebilir veriyi üretsin.
-- =====================================================================

-- Demo satıcı — ilanların sahibi olacak, gerçek giriş yapılması
-- beklenmeyen sabit bir kullanıcı.
INSERT INTO users (id, email, full_name, password_hash, phone, created_at)
VALUES (
    '00000000-0000-0000-0000-000000000001',
    'demo.satici@tiklasat.com',
    'Demo Satıcı',
    'seed-not-a-real-hash',
    '5550000000',
    now()
);

-- Kök kategoriler
INSERT INTO categories (id, name, slug, created_at) VALUES
    ('00000000-0000-0000-0000-000000000101', 'Elektronik', 'elektronik', now()),
    ('00000000-0000-0000-0000-000000000102', 'Araç', 'arac', now()),
    ('00000000-0000-0000-0000-000000000103', 'Ev Eşyası', 'ev-esyasi', now()),
    ('00000000-0000-0000-0000-000000000104', 'Moda', 'moda', now());

-- Örnek ilanlar
INSERT INTO listings (id, seller_id, category_id, title, description, status, view_count, created_at) VALUES
    (gen_random_uuid(), '00000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000101',
     'iPhone 14 Pro 256GB Uzay Grisi',
     'Kutulu, faturalı, garanti süresi devam ediyor. Ekran koruyucu ve kılıfla birlikte kullanıldı, hiç çizik yok.',
     'APPROVED', 12, now()),
    (gen_random_uuid(), '00000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000102',
     '2019 Model Otomatik Vites Sedan',
     'Tek elden, hasar kaydı yok, bakımları yetkili serviste yapıldı. 85.000 km''de.',
     'APPROVED', 34, now()),
    (gen_random_uuid(), '00000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000103',
     'Vintage Ahşap Yemek Masası Takımı',
     '6 kişilik, masif meşe, 1980''ler dönemine ait, restore edilmiş.',
     'APPROVED', 7, now()),
    (gen_random_uuid(), '00000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000104',
     'Sınırlı Sayı Deri Ceket',
     'Orijinal İtalyan derisi, M beden, hiç giyilmedi, etiketli.',
     'APPROVED', 3, now());
