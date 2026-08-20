-- Mevcut V5 ilanlarının fiyatlarını güncelleyelim (Arama testleri için sıfır olmasın)
UPDATE listings SET price = 45000.00 WHERE title LIKE '%iPhone 14%';
UPDATE listings SET price = 850000.00 WHERE title LIKE '%2019 Model%';
UPDATE listings SET price = 12000.00 WHERE title LIKE '%Yemek Masası%';
UPDATE listings SET price = 3500.00 WHERE title LIKE '%Deri Ceket%';

-- Arama ve filtreleme testlerimiz için yepyeni bol çeşitli ilanlar ekleyelim
INSERT INTO listings (id, seller_id, category_id, title, description, status, view_count, price, created_at) VALUES
    (gen_random_uuid(), '00000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000101', 'MacBook Pro M2 16GB RAM', 'Yazılımcıdan temiz MacBook. Pil döngüsü 40. Kutusu var.', 'APPROVED', 150, 65000.00, now()),
    (gen_random_uuid(), '00000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000101', 'Samsung Galaxy S23 Ultra', '1 aylık cihaz, yurtiçi kayıtlı, sıfır ayarında.', 'APPROVED', 85, 52000.00, now()),
    (gen_random_uuid(), '00000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000102', '2022 Fiat Egea Cross 1.4 Fire', 'Hatasız boyasız, ilk sahibinden.', 'APPROVED', 420, 950000.00, now()),
    (gen_random_uuid(), '00000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000103', 'IKEA Kivik 3''lü Kanepe', 'Gri renk, kılıfları yıkanabilir. Taşınma sebebiyle satılık.', 'APPROVED', 12, 4500.00, now()),
    (gen_random_uuid(), '00000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000101', 'Sony PlayStation 5 Çift Kol', 'Kozmetik olarak 10/10. CD versiyonudur.', 'APPROVED', 560, 21000.00, now());
