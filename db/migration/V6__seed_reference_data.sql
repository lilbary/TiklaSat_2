-- =====================================================================
-- TıklaSat · V6 · Referans Veri
-- ---------------------------------------------------------------------
-- Roller, iller/ilçeler, teklif artış kademeleri ve örnek kategori ağacı.
--
-- Kategori/özellik UUID'leri SABİT yazılmıştır (gen_random_uuid() değil).
-- Gerekçe: seed verisinin geliştirme, test ve üretim ortamlarında AYNI
-- kimliklere sahip olması gerekir; aksi halde ortamlar arası veri
-- karşılaştırması ve test fikstürleri kırılır.
-- =====================================================================

-- =====================================================================
-- ROLLER (BR-U-002)
-- =====================================================================
INSERT INTO roles (id, code, name, description) VALUES
    (1, 'BUYER',     'Alıcı',      'Teklif verebilir, ilan takip edebilir'),
    (2, 'SELLER',    'Satıcı',     'İlan açabilir ve açık artırma başlatabilir'),
    (3, 'MODERATOR', 'Moderatör',  'İlan onaylar/reddeder, şikayetleri inceler'),
    (4, 'ADMIN',     'Yönetici',   'Tam yetki: rol atama, artırma iptali, teklif geçersizleştirme');

-- BR-U-002 · ZİYARETÇİ bilinçli olarak YOKTUR.
-- Kimlik doğrulaması yapılmamış istektir; Spring Security'de permitAll().

-- =====================================================================
-- İLLER · id = plaka kodu
-- =====================================================================
INSERT INTO cities (id, name) VALUES
    (1,'Adana'),(2,'Adıyaman'),(3,'Afyonkarahisar'),(4,'Ağrı'),(5,'Amasya'),
    (6,'Ankara'),(7,'Antalya'),(8,'Artvin'),(9,'Aydın'),(10,'Balıkesir'),
    (11,'Bilecik'),(12,'Bingöl'),(13,'Bitlis'),(14,'Bolu'),(15,'Burdur'),
    (16,'Bursa'),(17,'Çanakkale'),(18,'Çankırı'),(19,'Çorum'),(20,'Denizli'),
    (21,'Diyarbakır'),(22,'Edirne'),(23,'Elazığ'),(24,'Erzincan'),(25,'Erzurum'),
    (26,'Eskişehir'),(27,'Gaziantep'),(28,'Giresun'),(29,'Gümüşhane'),(30,'Hakkâri'),
    (31,'Hatay'),(32,'Isparta'),(33,'Mersin'),(34,'İstanbul'),(35,'İzmir'),
    (36,'Kars'),(37,'Kastamonu'),(38,'Kayseri'),(39,'Kırklareli'),(40,'Kırşehir'),
    (41,'Kocaeli'),(42,'Konya'),(43,'Kütahya'),(44,'Malatya'),(45,'Manisa'),
    (46,'Kahramanmaraş'),(47,'Mardin'),(48,'Muğla'),(49,'Muş'),(50,'Nevşehir'),
    (51,'Niğde'),(52,'Ordu'),(53,'Rize'),(54,'Sakarya'),(55,'Samsun'),
    (56,'Siirt'),(57,'Sinop'),(58,'Sivas'),(59,'Tekirdağ'),(60,'Tokat'),
    (61,'Trabzon'),(62,'Tunceli'),(63,'Şanlıurfa'),(64,'Uşak'),(65,'Van'),
    (66,'Yozgat'),(67,'Zonguldak'),(68,'Aksaray'),(69,'Bayburt'),(70,'Karaman'),
    (71,'Kırıkkale'),(72,'Batman'),(73,'Şırnak'),(74,'Bartın'),(75,'Ardahan'),
    (76,'Iğdır'),(77,'Yalova'),(78,'Karabük'),(79,'Kilis'),(80,'Osmaniye'),
    (81,'Düzce');

-- ---------------------------------------------------------------------
-- İLÇELER
-- ---------------------------------------------------------------------
-- ⚠ KAPSAM NOTU: Türkiye'de 970+ ilçe vardır. Burada nüfusça en büyük
--   3 il (İstanbul, Ankara, İzmir) tam olarak seed edilmiştir. Kalan
--   illerin ilçeleri ayrı bir veri yükleme adımıyla (CSV → COPY)
--   aktarılacaktır; migration dosyasını 970 satırla şişirmek yerine
--   veri dosyası olarak yönetmek doğru yaklaşımdır.
-- ---------------------------------------------------------------------

-- İstanbul (34)
INSERT INTO districts (city_id, name) VALUES
    (34,'Adalar'),(34,'Arnavutköy'),(34,'Ataşehir'),(34,'Avcılar'),(34,'Bağcılar'),
    (34,'Bahçelievler'),(34,'Bakırköy'),(34,'Başakşehir'),(34,'Bayrampaşa'),(34,'Beşiktaş'),
    (34,'Beykoz'),(34,'Beylikdüzü'),(34,'Beyoğlu'),(34,'Büyükçekmece'),(34,'Çatalca'),
    (34,'Çekmeköy'),(34,'Esenler'),(34,'Esenyurt'),(34,'Eyüpsultan'),(34,'Fatih'),
    (34,'Gaziosmanpaşa'),(34,'Güngören'),(34,'Kadıköy'),(34,'Kağıthane'),(34,'Kartal'),
    (34,'Küçükçekmece'),(34,'Maltepe'),(34,'Pendik'),(34,'Sancaktepe'),(34,'Sarıyer'),
    (34,'Silivri'),(34,'Sultanbeyli'),(34,'Sultangazi'),(34,'Şile'),(34,'Şişli'),
    (34,'Tuzla'),(34,'Ümraniye'),(34,'Üsküdar'),(34,'Zeytinburnu');

-- Ankara (6)
INSERT INTO districts (city_id, name) VALUES
    (6,'Akyurt'),(6,'Altındağ'),(6,'Ayaş'),(6,'Bala'),(6,'Beypazarı'),
    (6,'Çamlıdere'),(6,'Çankaya'),(6,'Çubuk'),(6,'Elmadağ'),(6,'Etimesgut'),
    (6,'Evren'),(6,'Gölbaşı'),(6,'Güdül'),(6,'Haymana'),(6,'Kahramankazan'),
    (6,'Kalecik'),(6,'Keçiören'),(6,'Kızılcahamam'),(6,'Mamak'),(6,'Nallıhan'),
    (6,'Polatlı'),(6,'Pursaklar'),(6,'Sincan'),(6,'Şereflikoçhisar'),(6,'Yenimahalle');

-- İzmir (35)
INSERT INTO districts (city_id, name) VALUES
    (35,'Aliağa'),(35,'Balçova'),(35,'Bayındır'),(35,'Bayraklı'),(35,'Bergama'),
    (35,'Beydağ'),(35,'Bornova'),(35,'Buca'),(35,'Çeşme'),(35,'Çiğli'),
    (35,'Dikili'),(35,'Foça'),(35,'Gaziemir'),(35,'Güzelbahçe'),(35,'Karabağlar'),
    (35,'Karaburun'),(35,'Karşıyaka'),(35,'Kemalpaşa'),(35,'Kınık'),(35,'Kiraz'),
    (35,'Konak'),(35,'Menderes'),(35,'Menemen'),(35,'Narlıdere'),(35,'Ödemiş'),
    (35,'Seferihisar'),(35,'Selçuk'),(35,'Tire'),(35,'Torbalı'),(35,'Urla');

-- =====================================================================
-- TEKLİF ARTIŞ KADEMELERİ (BR-B-003)
-- ---------------------------------------------------------------------
-- Aralıklar: alt sınır DAHİL, üst sınır HARİÇ.
-- ex_tiers_no_overlap kısıtı çakışma olmadığını garanti eder.
--
-- ⚠ DÜZ %5 KURALINA DÖNMEK İSTERSEN:
--   Aşağıdaki 5 satırı silip tek satır ekle:
--   INSERT INTO bid_increment_tiers (min_amount, max_amount, increment_type, increment_value)
--        VALUES (0, NULL, 'PERCENTAGE', 5);
--   Kod değişikliği veya yeniden derleme GEREKMEZ.
-- =====================================================================
INSERT INTO bid_increment_tiers (min_amount, max_amount, increment_type, increment_value) VALUES
    (      0.00,    1000.00, 'FIXED',        25.00),
    (   1000.00,   10000.00, 'FIXED',       100.00),
    (  10000.00,  100000.00, 'FIXED',       500.00),
    ( 100000.00,  500000.00, 'FIXED',      2500.00),
    ( 500000.00,       NULL, 'PERCENTAGE',     1.00);

-- =====================================================================
-- ÖRNEK KATEGORİ AĞACI
-- ---------------------------------------------------------------------
-- 4 kök kategori, 3 seviye derinlik. Üretimde admin panelinden genişletilir
-- (BR-C-003 · kod değişikliği gerektirmez).
-- =====================================================================

-- ---- Seviye 1 · Kök kategoriler (is_leaf = false) ----
INSERT INTO categories (id, parent_id, name, slug, path, depth, is_leaf, sort_order, icon) VALUES
    ('a0000000-0000-4000-8000-000000000001', NULL, 'Vasıta',            'vasita',      '/vasita/',      1, false, 1, 'car'),
    ('a0000000-0000-4000-8000-000000000002', NULL, 'Emlak',             'emlak',       '/emlak/',       1, false, 2, 'home'),
    ('a0000000-0000-4000-8000-000000000003', NULL, 'Elektronik',        'elektronik',  '/elektronik/',  1, false, 3, 'cpu'),
    ('a0000000-0000-4000-8000-000000000004', NULL, 'Koleksiyon & Sanat','koleksiyon',  '/koleksiyon/',  1, false, 4, 'palette');

-- ---- Seviye 2 ----
INSERT INTO categories (id, parent_id, name, slug, path, depth, is_leaf, sort_order) VALUES
    ('a0000000-0000-4000-8000-000000000101', 'a0000000-0000-4000-8000-000000000001', 'Otomobil',      'otomobil',  '/vasita/otomobil/',        2, false, 1),
    ('a0000000-0000-4000-8000-000000000102', 'a0000000-0000-4000-8000-000000000001', 'Motosiklet',    'motosiklet','/vasita/motosiklet/',      2, true,  2),
    ('a0000000-0000-4000-8000-000000000201', 'a0000000-0000-4000-8000-000000000002', 'Konut',         'konut',     '/emlak/konut/',            2, false, 1),
    ('a0000000-0000-4000-8000-000000000301', 'a0000000-0000-4000-8000-000000000003', 'Telefon',       'telefon',   '/elektronik/telefon/',     2, false, 1),
    ('a0000000-0000-4000-8000-000000000302', 'a0000000-0000-4000-8000-000000000003', 'Bilgisayar',    'bilgisayar','/elektronik/bilgisayar/',  2, true,  2),
    ('a0000000-0000-4000-8000-000000000401', 'a0000000-0000-4000-8000-000000000004', 'Antika',        'antika',    '/koleksiyon/antika/',      2, false, 1);

-- ---- Seviye 3 · Yaprak kategoriler · İLAN YALNIZCA BUNLARA AÇILIR (BR-C-002) ----
INSERT INTO categories (id, parent_id, name, slug, path, depth, is_leaf, sort_order) VALUES
    ('a0000000-0000-4000-8000-000000001101', 'a0000000-0000-4000-8000-000000000101', 'Sedan',          'sedan',      '/vasita/otomobil/sedan/',          3, true, 1),
    ('a0000000-0000-4000-8000-000000001102', 'a0000000-0000-4000-8000-000000000101', 'Hatchback',      'hatchback',  '/vasita/otomobil/hatchback/',      3, true, 2),
    ('a0000000-0000-4000-8000-000000001103', 'a0000000-0000-4000-8000-000000000101', 'SUV',            'suv',        '/vasita/otomobil/suv/',            3, true, 3),
    ('a0000000-0000-4000-8000-000000002101', 'a0000000-0000-4000-8000-000000000201', 'Daire',          'daire',      '/emlak/konut/daire/',              3, true, 1),
    ('a0000000-0000-4000-8000-000000002102', 'a0000000-0000-4000-8000-000000000201', 'Müstakil Ev',    'mustakil-ev','/emlak/konut/mustakil-ev/',        3, true, 2),
    ('a0000000-0000-4000-8000-000000003101', 'a0000000-0000-4000-8000-000000000301', 'Akıllı Telefon', 'akilli-telefon','/elektronik/telefon/akilli-telefon/', 3, true, 1),
    ('a0000000-0000-4000-8000-000000004101', 'a0000000-0000-4000-8000-000000000401', 'Tablo',          'tablo',      '/koleksiyon/antika/tablo/',        3, true, 1),
    ('a0000000-0000-4000-8000-000000004102', 'a0000000-0000-4000-8000-000000000401', 'Saat',           'saat',       '/koleksiyon/antika/saat/',         3, true, 2);

-- =====================================================================
-- ÖRNEK ÖZELLİK TANIMLARI (BR-C-003)
-- ---------------------------------------------------------------------
-- BR-C-006 · Alt kategoriler üst kategorinin alanlarını devralır.
-- Bu yüzden ortak alanlar "Otomobil" (seviye 2) üzerinde tanımlanır;
-- Sedan/Hatchback/SUV ilanlarında da sorulur.
-- =====================================================================

-- ---- Otomobil (seviye 2 · alt kategorilere miras kalır) ----
INSERT INTO attribute_definitions
    (id, category_id, code, label, data_type, unit, is_required, is_filterable, min_value, max_value, sort_order) VALUES
    ('b0000000-0000-4000-8000-000000000101', 'a0000000-0000-4000-8000-000000000101', 'model_year',   'Model Yılı',  'INTEGER', NULL, true,  true, 1900, 2027, 1),
    ('b0000000-0000-4000-8000-000000000102', 'a0000000-0000-4000-8000-000000000101', 'mileage_km',   'Kilometre',   'INTEGER', 'km', true,  true, 0,    2000000, 2),
    ('b0000000-0000-4000-8000-000000000103', 'a0000000-0000-4000-8000-000000000101', 'fuel_type',    'Yakıt Tipi',  'ENUM',    NULL, true,  true, NULL, NULL, 3),
    ('b0000000-0000-4000-8000-000000000104', 'a0000000-0000-4000-8000-000000000101', 'transmission', 'Vites',       'ENUM',    NULL, true,  true, NULL, NULL, 4),
    ('b0000000-0000-4000-8000-000000000105', 'a0000000-0000-4000-8000-000000000101', 'engine_power', 'Motor Gücü',  'INTEGER', 'HP', false, true, 1,    2000, 5),
    ('b0000000-0000-4000-8000-000000000106', 'a0000000-0000-4000-8000-000000000101', 'damage_free',  'Hasarsız',    'BOOLEAN', NULL, false, true, NULL, NULL, 6);

INSERT INTO attribute_options (attribute_definition_id, value, label, sort_order) VALUES
    ('b0000000-0000-4000-8000-000000000103', 'GASOLINE',  'Benzin',   1),
    ('b0000000-0000-4000-8000-000000000103', 'DIESEL',    'Dizel',    2),
    ('b0000000-0000-4000-8000-000000000103', 'LPG',       'LPG',      3),
    ('b0000000-0000-4000-8000-000000000103', 'HYBRID',    'Hibrit',   4),
    ('b0000000-0000-4000-8000-000000000103', 'ELECTRIC',  'Elektrik', 5),
    ('b0000000-0000-4000-8000-000000000104', 'MANUAL',    'Manuel',      1),
    ('b0000000-0000-4000-8000-000000000104', 'AUTOMATIC', 'Otomatik',    2),
    ('b0000000-0000-4000-8000-000000000104', 'SEMI_AUTO', 'Yarı Otomatik', 3);

-- ---- Konut (seviye 2) ----
INSERT INTO attribute_definitions
    (id, category_id, code, label, data_type, unit, is_required, is_filterable, min_value, max_value, sort_order) VALUES
    ('b0000000-0000-4000-8000-000000000201', 'a0000000-0000-4000-8000-000000000201', 'room_count',    'Oda Sayısı',    'ENUM',    NULL, true,  true, NULL, NULL,  1),
    ('b0000000-0000-4000-8000-000000000202', 'a0000000-0000-4000-8000-000000000201', 'gross_area_m2', 'Brüt Alan',     'INTEGER', 'm²', true,  true, 1,    10000, 2),
    ('b0000000-0000-4000-8000-000000000203', 'a0000000-0000-4000-8000-000000000201', 'building_age',  'Bina Yaşı',     'INTEGER', 'yıl',true,  true, 0,    200,   3),
    ('b0000000-0000-4000-8000-000000000204', 'a0000000-0000-4000-8000-000000000201', 'floor_no',      'Bulunduğu Kat', 'INTEGER', NULL, false, true, -5,   100,   4),
    ('b0000000-0000-4000-8000-000000000205', 'a0000000-0000-4000-8000-000000000201', 'heating',       'Isıtma',        'ENUM',    NULL, false, true, NULL, NULL,  5);

INSERT INTO attribute_options (attribute_definition_id, value, label, sort_order) VALUES
    ('b0000000-0000-4000-8000-000000000201', '1_PLUS_0', '1+0', 1),
    ('b0000000-0000-4000-8000-000000000201', '1_PLUS_1', '1+1', 2),
    ('b0000000-0000-4000-8000-000000000201', '2_PLUS_1', '2+1', 3),
    ('b0000000-0000-4000-8000-000000000201', '3_PLUS_1', '3+1', 4),
    ('b0000000-0000-4000-8000-000000000201', '4_PLUS_1', '4+1', 5),
    ('b0000000-0000-4000-8000-000000000201', '5_PLUS',   '5+ ', 6),
    ('b0000000-0000-4000-8000-000000000205', 'NATURAL_GAS', 'Doğalgaz',     1),
    ('b0000000-0000-4000-8000-000000000205', 'CENTRAL',     'Merkezi',      2),
    ('b0000000-0000-4000-8000-000000000205', 'AC',          'Klima',        3),
    ('b0000000-0000-4000-8000-000000000205', 'NONE',        'Isıtma Yok',   4);

-- ---- Akıllı Telefon (seviye 3 · yaprak) ----
INSERT INTO attribute_definitions
    (id, category_id, code, label, data_type, unit, is_required, is_filterable, min_value, max_value, sort_order) VALUES
    ('b0000000-0000-4000-8000-000000000301', 'a0000000-0000-4000-8000-000000003101', 'storage_gb',   'Hafıza',       'ENUM',    NULL, true,  true, NULL, NULL, 1),
    ('b0000000-0000-4000-8000-000000000302', 'a0000000-0000-4000-8000-000000003101', 'condition',    'Durum',        'ENUM',    NULL, true,  true, NULL, NULL, 2),
    ('b0000000-0000-4000-8000-000000000303', 'a0000000-0000-4000-8000-000000003101', 'has_warranty', 'Garantili',    'BOOLEAN', NULL, false, true, NULL, NULL, 3),
    ('b0000000-0000-4000-8000-000000000304', 'a0000000-0000-4000-8000-000000003101', 'battery_health','Batarya Sağlığı','INTEGER','%', false, true, 0,    100,  4);

INSERT INTO attribute_options (attribute_definition_id, value, label, sort_order) VALUES
    ('b0000000-0000-4000-8000-000000000301', '64',   '64 GB',   1),
    ('b0000000-0000-4000-8000-000000000301', '128',  '128 GB',  2),
    ('b0000000-0000-4000-8000-000000000301', '256',  '256 GB',  3),
    ('b0000000-0000-4000-8000-000000000301', '512',  '512 GB',  4),
    ('b0000000-0000-4000-8000-000000000301', '1024', '1 TB',    5),
    ('b0000000-0000-4000-8000-000000000302', 'NEW',        'Sıfır',        1),
    ('b0000000-0000-4000-8000-000000000302', 'LIKE_NEW',   'Sıfır Ayarında', 2),
    ('b0000000-0000-4000-8000-000000000302', 'GOOD',       'İyi',          3),
    ('b0000000-0000-4000-8000-000000000302', 'FAIR',       'Orta',         4),
    ('b0000000-0000-4000-8000-000000000302', 'FOR_PARTS',  'Parça Değeri', 5);

-- ---- Tablo (seviye 3 · yaprak · DATE tipi örneği) ----
INSERT INTO attribute_definitions
    (id, category_id, code, label, data_type, unit, is_required, is_filterable, sort_order) VALUES
    ('b0000000-0000-4000-8000-000000000401', 'a0000000-0000-4000-8000-000000004101', 'technique',   'Teknik',       'ENUM',    NULL, true,  true, 1),
    ('b0000000-0000-4000-8000-000000000402', 'a0000000-0000-4000-8000-000000004101', 'artist_name', 'Sanatçı',      'TEXT',    NULL, false, true, 2),
    ('b0000000-0000-4000-8000-000000000403', 'a0000000-0000-4000-8000-000000004101', 'created_date','Yapım Tarihi', 'DATE',    NULL, false, true, 3),
    ('b0000000-0000-4000-8000-000000000404', 'a0000000-0000-4000-8000-000000004101', 'is_signed',   'İmzalı',       'BOOLEAN', NULL, false, true, 4);

UPDATE attribute_definitions
   SET max_length = 120
 WHERE id = 'b0000000-0000-4000-8000-000000000402';

INSERT INTO attribute_options (attribute_definition_id, value, label, sort_order) VALUES
    ('b0000000-0000-4000-8000-000000000401', 'OIL',        'Yağlıboya',  1),
    ('b0000000-0000-4000-8000-000000000401', 'WATERCOLOR', 'Suluboya',   2),
    ('b0000000-0000-4000-8000-000000000401', 'ACRYLIC',    'Akrilik',    3),
    ('b0000000-0000-4000-8000-000000000401', 'PRINT',      'Baskı',      4),
    ('b0000000-0000-4000-8000-000000000401', 'MIXED',      'Karışık Teknik', 5);
