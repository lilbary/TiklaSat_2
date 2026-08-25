-- =====================================================================
-- TıklaSat · V5 · Örnek Veri (Seed Data)
-- ---------------------------------------------------------------------
-- Tüm ID'ler gen_random_uuid() ile üretilir. Diğer migration'lar
-- referans alırken email / slug / title üzerinden subquery kullanır.
-- =====================================================================

-- Demo satıcı
INSERT INTO users (id, email, full_name, password_hash, phone, created_at)
VALUES (
    gen_random_uuid(),
    'demo.satici@tiklasat.com',
    'Demo Satıcı',
    'seed-not-a-real-hash',
    '5550000000',
    now()
);

-- ========================================
-- KATEGORİ AĞACI — SEVİYE 0 (KÖKLER)
-- ========================================
INSERT INTO categories (id, parent_id, name, slug, created_at) VALUES
    (gen_random_uuid(), NULL, 'Elektronik',               'elektronik',          now()),
    (gen_random_uuid(), NULL, 'Moda',                     'moda',                now()),
    (gen_random_uuid(), NULL, 'Ev & Yaşam',               'ev-yasam',            now()),
    (gen_random_uuid(), NULL, 'Anne & Bebek',             'anne-bebek',          now()),
    (gen_random_uuid(), NULL, 'Kozmetik & Kişisel Bakım', 'kozmetik',            now()),
    (gen_random_uuid(), NULL, 'Spor & Outdoor',           'spor-outdoor',        now()),
    (gen_random_uuid(), NULL, 'Kitap & Kırtasiye',        'kitap-kirtasiye',     now()),
    (gen_random_uuid(), NULL, 'Süpermarket',              'supermarket',         now()),
    (gen_random_uuid(), NULL, 'Araç',                     'arac',                now()),
    (gen_random_uuid(), NULL, 'Yapı Market',              'yapi-market',         now()),
    (gen_random_uuid(), NULL, 'Oyun & Hobi',              'oyun-hobi',           now()),
    (gen_random_uuid(), NULL, 'Bahçe & Evcil Hayvan',     'bahce-evcil-hayvan',  now());

-- ========================================
-- SEVİYE 1: ANA ALT KATEGORİLER
-- ========================================

-- Elektronik altı
INSERT INTO categories (id, parent_id, name, slug, created_at) VALUES
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'elektronik'), 'Cep Telefonu & Aksesuar', 'cep-telefonu',        now()),
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'elektronik'), 'Bilgisayar & Tablet',     'bilgisayar-tablet',   now()),
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'elektronik'), 'TV, Görüntü & Ses',       'tv-goruntu-ses',      now()),
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'elektronik'), 'Beyaz Eşya',              'beyaz-esya',          now()),
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'elektronik'), 'Küçük Ev Aletleri',       'kucuk-ev-aletleri',   now()),
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'elektronik'), 'Foto & Kamera',           'foto-kamera',         now()),
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'elektronik'), 'Oyun Konsolları',         'oyun-konsollari',     now());

-- Moda altı
INSERT INTO categories (id, parent_id, name, slug, created_at) VALUES
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'moda'), 'Kadın Giyim',       'kadin-giyim',    now()),
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'moda'), 'Erkek Giyim',       'erkek-giyim',    now()),
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'moda'), 'Çocuk Giyim',       'cocuk-giyim',    now()),
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'moda'), 'Kadın Ayakkabı',    'kadin-ayakkabi', now()),
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'moda'), 'Erkek Ayakkabı',    'erkek-ayakkabi', now()),
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'moda'), 'Çanta',             'canta',          now()),
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'moda'), 'Saat & Aksesuar',   'saat-aksesuar',  now()),
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'moda'), 'Takı & Mücevher',   'taki-mucevher',  now());

-- Ev & Yaşam altı
INSERT INTO categories (id, parent_id, name, slug, created_at) VALUES
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'ev-yasam'), 'Mobilya',             'mobilya',          now()),
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'ev-yasam'), 'Ev Tekstili',         'ev-tekstili',      now()),
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'ev-yasam'), 'Mutfak Gereçleri',    'mutfak-gerecleri', now()),
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'ev-yasam'), 'Aydınlatma',          'aydinlatma',       now()),
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'ev-yasam'), 'Dekorasyon',          'dekorasyon',       now()),
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'ev-yasam'), 'Banyo',               'banyo',            now());

-- Anne & Bebek altı
INSERT INTO categories (id, parent_id, name, slug, created_at) VALUES
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'anne-bebek'), 'Bebek Giyim',        'bebek-giyim',       now()),
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'anne-bebek'), 'Bebek Arabaları',    'bebek-arabalari',   now()),
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'anne-bebek'), 'Bebek Bakım',        'bebek-bakim',       now()),
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'anne-bebek'), 'Oyuncak',            'oyuncak',           now()),
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'anne-bebek'), 'Hamile & Anne',      'hamile-anne',       now());

-- Kozmetik altı
INSERT INTO categories (id, parent_id, name, slug, created_at) VALUES
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'kozmetik'), 'Makyaj',            'makyaj',           now()),
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'kozmetik'), 'Cilt Bakım',        'cilt-bakim',       now()),
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'kozmetik'), 'Saç Bakım',         'sac-bakim',        now()),
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'kozmetik'), 'Parfüm & Deodorant','parfum-deodorant', now()),
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'kozmetik'), 'Erkek Bakım',       'erkek-bakim',      now());

-- Spor & Outdoor altı
INSERT INTO categories (id, parent_id, name, slug, created_at) VALUES
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'spor-outdoor'), 'Spor Giyim',         'spor-giyim',        now()),
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'spor-outdoor'), 'Spor Ayakkabı',      'spor-ayakkabi',     now()),
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'spor-outdoor'), 'Fitness & Kondisyon', 'fitness-kondisyon', now()),
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'spor-outdoor'), 'Kamp & Doğa',        'kamp-doga',         now()),
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'spor-outdoor'), 'Bisiklet',           'bisiklet',          now());

-- Araç altı
INSERT INTO categories (id, parent_id, name, slug, created_at) VALUES
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'arac'), 'Otomobil',         'otomobil',         now()),
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'arac'), 'Motorsiklet',      'motorsiklet',      now()),
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'arac'), 'Yedek Parça',      'yedek-parca',      now()),
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'arac'), 'Oto Aksesuar',     'oto-aksesuar',     now()),
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'arac'), 'Oto Elektronik',   'oto-elektronik',   now());

-- Kitap & Kırtasiye altı
INSERT INTO categories (id, parent_id, name, slug, created_at) VALUES
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'kitap-kirtasiye'), 'Roman & Öykü',       'roman-oyku',      now()),
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'kitap-kirtasiye'), 'Ders & Sınav Kitap', 'ders-sinav',      now()),
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'kitap-kirtasiye'), 'Çocuk Kitapları',    'cocuk-kitaplari', now()),
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'kitap-kirtasiye'), 'Kırtasiye Malzemeleri','kirtasiye',     now());

-- Süpermarket altı
INSERT INTO categories (id, parent_id, name, slug, created_at) VALUES
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'supermarket'), 'Gıda',              'gida',           now()),
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'supermarket'), 'İçecek',            'icecek',         now()),
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'supermarket'), 'Temizlik',          'temizlik',       now()),
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'supermarket'), 'Kağıt Ürünleri',    'kagit-urunleri', now());

-- Yapı Market altı
INSERT INTO categories (id, parent_id, name, slug, created_at) VALUES
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'yapi-market'), 'El Aletleri',       'el-aletleri',    now()),
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'yapi-market'), 'Elektrikli Aletler','elektrikli-aletler', now()),
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'yapi-market'), 'Boya & Duvar Kağıdı','boya-duvar',   now()),
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'yapi-market'), 'Hırdavat',          'hirdavat',       now());

-- Oyun & Hobi altı
INSERT INTO categories (id, parent_id, name, slug, created_at) VALUES
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'oyun-hobi'), 'Video Oyunları',    'video-oyunlari',  now()),
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'oyun-hobi'), 'Puzzle & Yapboz',   'puzzle-yapboz',   now()),
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'oyun-hobi'), 'Koleksiyon',        'koleksiyon',      now()),
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'oyun-hobi'), 'Müzik Aletleri',    'muzik-aletleri',  now());

-- Bahçe & Evcil Hayvan altı
INSERT INTO categories (id, parent_id, name, slug, created_at) VALUES
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'bahce-evcil-hayvan'), 'Bahçe Mobilya',     'bahce-mobilya',     now()),
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'bahce-evcil-hayvan'), 'Bahçe Aletleri',    'bahce-aletleri',    now()),
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'bahce-evcil-hayvan'), 'Kedi Ürünleri',     'kedi-urunleri',     now()),
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'bahce-evcil-hayvan'), 'Köpek Ürünleri',    'kopek-urunleri',    now());


-- ========================================
-- SEVİYE 2: DETAY KATEGORİLER
-- ========================================

-- Cep Telefonu altı
INSERT INTO categories (id, parent_id, name, slug, created_at) VALUES
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'cep-telefonu'), 'Apple Telefonlar',     'apple-telefonlar',    now()),
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'cep-telefonu'), 'Samsung Telefonlar',   'samsung-telefonlar',  now()),
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'cep-telefonu'), 'Xiaomi Telefonlar',    'xiaomi-telefonlar',   now()),
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'cep-telefonu'), 'Telefon Kılıfları',    'telefon-kiliflari',   now()),
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'cep-telefonu'), 'Şarj & Kablolar',      'sarj-kablolar',       now()),
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'cep-telefonu'), 'Ekran Koruyucu',       'ekran-koruyucu',      now()),
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'cep-telefonu'), 'Kulaklık & Hoparlör',  'kulaklik-hoparlor',   now());

-- Bilgisayar & Tablet altı
INSERT INTO categories (id, parent_id, name, slug, created_at) VALUES
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'bilgisayar-tablet'), 'Laptop',              'laptop',              now()),
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'bilgisayar-tablet'), 'Masaüstü Bilgisayar', 'masaustu-bilgisayar', now()),
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'bilgisayar-tablet'), 'Tablet',              'tablet',              now()),
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'bilgisayar-tablet'), 'Monitör',             'monitor',             now()),
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'bilgisayar-tablet'), 'Bilgisayar Bileşenleri','bilgisayar-bilesenleri', now()),
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'bilgisayar-tablet'), 'Yazıcı & Tarayıcı',  'yazici-tarayici',     now());

-- TV, Görüntü & Ses altı
INSERT INTO categories (id, parent_id, name, slug, created_at) VALUES
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'tv-goruntu-ses'), 'Televizyon',        'televizyon',       now()),
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'tv-goruntu-ses'), 'Soundbar & Ses',    'soundbar-ses',     now()),
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'tv-goruntu-ses'), 'Projeksiyon',       'projeksiyon',      now()),
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'tv-goruntu-ses'), 'TV Aksesuar',       'tv-aksesuar',      now());

-- Beyaz Eşya altı
INSERT INTO categories (id, parent_id, name, slug, created_at) VALUES
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'beyaz-esya'), 'Bulaşık Makinesi',  'bulasik-makinesi', now()),
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'beyaz-esya'), 'Çamaşır Makinesi',  'camasir-makinesi', now()),
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'beyaz-esya'), 'Buzdolabı',         'buzdolabi',        now()),
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'beyaz-esya'), 'Fırın & Ocak',      'firin-ocak',       now()),
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'beyaz-esya'), 'Klima',             'klima',            now());

-- Kadın Giyim altı
INSERT INTO categories (id, parent_id, name, slug, created_at) VALUES
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'kadin-giyim'), 'Elbise',           'kadin-elbise',     now()),
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'kadin-giyim'), 'Tişört',           'kadin-tisort',     now()),
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'kadin-giyim'), 'Gömlek & Bluz',    'kadin-gomlek',     now()),
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'kadin-giyim'), 'Pantolon',         'kadin-pantolon',   now()),
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'kadin-giyim'), 'Etek',             'kadin-etek',       now()),
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'kadin-giyim'), 'Mont & Kaban',     'kadin-mont',       now()),
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'kadin-giyim'), 'Triko & Kazak',    'kadin-triko',      now());

-- Erkek Giyim altı
INSERT INTO categories (id, parent_id, name, slug, created_at) VALUES
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'erkek-giyim'), 'Tişört',           'erkek-tisort',     now()),
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'erkek-giyim'), 'Gömlek',           'erkek-gomlek',     now()),
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'erkek-giyim'), 'Pantolon',         'erkek-pantolon',   now()),
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'erkek-giyim'), 'Mont & Kaban',     'erkek-mont',       now()),
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'erkek-giyim'), 'Takım Elbise',     'erkek-takim',      now()),
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'erkek-giyim'), 'Sweatshirt',       'erkek-sweatshirt', now());

-- Mobilya altı
INSERT INTO categories (id, parent_id, name, slug, created_at) VALUES
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'mobilya'), 'Koltuk & Kanepe',   'koltuk-kanepe',   now()),
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'mobilya'), 'Yatak',             'yatak',            now()),
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'mobilya'), 'Masa',              'masa',             now()),
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'mobilya'), 'Sandalye',          'sandalye',         now()),
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'mobilya'), 'Dolap & Gardırop',  'dolap-gardirop',  now()),
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'mobilya'), 'TV Ünitesi',        'tv-unitesi',      now());

-- Makyaj altı
INSERT INTO categories (id, parent_id, name, slug, created_at) VALUES
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'makyaj'), 'Fondöten',          'fondoten',        now()),
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'makyaj'), 'Ruj',               'ruj',             now()),
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'makyaj'), 'Maskara',           'maskara',         now()),
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'makyaj'), 'Far & Eyeliner',    'far-eyeliner',    now()),
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'makyaj'), 'Makyaj Seti',       'makyaj-seti',     now());

-- Fitness & Kondisyon altı
INSERT INTO categories (id, parent_id, name, slug, created_at) VALUES
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'fitness-kondisyon'), 'Koşu Bandı',        'kosu-bandi',       now()),
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'fitness-kondisyon'), 'Dambıl & Ağırlık',  'dambil-agirlik',   now()),
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'fitness-kondisyon'), 'Yoga & Pilates',    'yoga-pilates',     now()),
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'fitness-kondisyon'), 'Spor Matı',         'spor-mati',        now());


-- ========================================
-- SEVİYE 3: YAPRAK KATEGORİLER
-- ========================================

-- Apple Telefonlar altı
INSERT INTO categories (id, parent_id, name, slug, created_at) VALUES
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'apple-telefonlar'), 'iPhone 16 Serisi',  'iphone-16',  now()),
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'apple-telefonlar'), 'iPhone 15 Serisi',  'iphone-15',  now()),
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'apple-telefonlar'), 'iPhone 14 Serisi',  'iphone-14',  now()),
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'apple-telefonlar'), 'iPhone SE',         'iphone-se',  now());

-- Samsung Telefonlar altı
INSERT INTO categories (id, parent_id, name, slug, created_at) VALUES
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'samsung-telefonlar'), 'Galaxy S Serisi',   'galaxy-s',   now()),
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'samsung-telefonlar'), 'Galaxy A Serisi',   'galaxy-a',   now()),
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'samsung-telefonlar'), 'Galaxy Z (Katlanır)','galaxy-z',  now());

-- Laptop altı
INSERT INTO categories (id, parent_id, name, slug, created_at) VALUES
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'laptop'), 'Apple MacBook',     'macbook',         now()),
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'laptop'), 'Gaming Laptop',     'gaming-laptop',   now()),
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'laptop'), 'Ultrabook',         'ultrabook',       now()),
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'laptop'), 'Laptop Çantaları',  'laptop-cantalari',now());

-- Televizyon altı
INSERT INTO categories (id, parent_id, name, slug, created_at) VALUES
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'televizyon'), 'OLED TV',          'oled-tv',        now()),
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'televizyon'), 'QLED TV',          'qled-tv',        now()),
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'televizyon'), 'Smart TV',         'smart-tv',       now()),
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'televizyon'), 'LED TV',           'led-tv',         now());

-- Elbise altı
INSERT INTO categories (id, parent_id, name, slug, created_at) VALUES
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'kadin-elbise'), 'Günlük Elbise',     'gunluk-elbise',    now()),
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'kadin-elbise'), 'Abiye',             'abiye',            now()),
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'kadin-elbise'), 'Yazlık Elbise',     'yazlik-elbise',    now());

-- Koltuk & Kanepe altı
INSERT INTO categories (id, parent_id, name, slug, created_at) VALUES
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'koltuk-kanepe'), 'Köşe Koltuk',       'kose-koltuk',      now()),
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'koltuk-kanepe'), 'Yataklı Kanepe',    'yatakli-kanepe',   now()),
    (gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'koltuk-kanepe'), 'Tekli Koltuk',      'tekli-koltuk',     now());


-- ========================================
-- ÖRNEK İLANLAR
-- ========================================
INSERT INTO listings (id, seller_id, category_id, title, description, status, view_count, created_at) VALUES
    (gen_random_uuid(),
     (SELECT id FROM users WHERE email = 'demo.satici@tiklasat.com'),
     (SELECT id FROM categories WHERE slug = 'apple-telefonlar'),
     'iPhone 14 Pro 256GB Uzay Grisi',
     'Kutulu, faturalı, garanti süresi devam ediyor. Ekran koruyucu ve kılıfla birlikte kullanıldı, hiç çizik yok.',
     'APPROVED', 12, now()),

    (gen_random_uuid(),
     (SELECT id FROM users WHERE email = 'demo.satici@tiklasat.com'),
     (SELECT id FROM categories WHERE slug = 'otomobil'),
     '2019 Model Otomatik Vites Sedan',
     'Tek elden, hasar kaydı yok, bakımları yetkili serviste yapıldı. 85.000 km''de.',
     'APPROVED', 34, now()),

    (gen_random_uuid(),
     (SELECT id FROM users WHERE email = 'demo.satici@tiklasat.com'),
     (SELECT id FROM categories WHERE slug = 'mobilya'),
     'Vintage Ahşap Yemek Masası Takımı',
     '6 kişilik, masif meşe, 1980''ler dönemine ait, restore edilmiş.',
     'APPROVED', 7, now()),

    (gen_random_uuid(),
     (SELECT id FROM users WHERE email = 'demo.satici@tiklasat.com'),
     (SELECT id FROM categories WHERE slug = 'erkek-mont'),
     'Sınırlı Sayı Deri Ceket',
     'Orijinal İtalyan derisi, M beden, hiç giyilmedi, etiketli.',
     'APPROVED', 3, now());