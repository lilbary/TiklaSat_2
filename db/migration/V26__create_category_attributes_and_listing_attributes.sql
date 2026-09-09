
--    Hangi kategoride hangi form alanları çıkacağını tanımlar.
-- ============================================================
CREATE TABLE IF NOT EXISTS category_attributes (
    id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    category_id UUID         NOT NULL REFERENCES categories(id) ON DELETE CASCADE,
    name        VARCHAR(100) NOT NULL,-- Backend key: "kilometre"
    label       VARCHAR(100) NOT NULL,-- UI'da görünecek: "Kilometre"
    field_type  VARCHAR(20)  NOT NULL,-- TEXT, NUMBER, SELECT, BOOLEAN
    options     JSONB,-- SELECT için: ["Benzin","Dizel","Elektrik"]...
    is_required BOOLEAN      NOT NULL DEFAULT true,
    unit        VARCHAR(20),-- Opsiyonel birim: "km", "cc"
    sort_order  INT          NOT NULL DEFAULT 0,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT uq_category_attribute UNIQUE (category_id, name)
);

-- aramalar kolaylassın diye indexledik
CREATE INDEX IF NOT EXISTS idx_category_attributes_category ON category_attributes(category_id);


-- 2) listings tablosuna JSONB attributes kolonu
-- ============================================================
ALTER TABLE listings ADD COLUMN IF NOT EXISTS attributes JSONB DEFAULT '{}'::jsonb;

CREATE INDEX IF NOT EXISTS idx_listings_attributes_gin ON listings USING GIN (attributes);
-- gın indeksi kullanılıyo.



-- 3) Seed Data — Kategori Özellikleri
-- ============================================================

-- ─── OTOMOBİL ───
INSERT INTO category_attributes (category_id, name, label, field_type, options, is_required, unit, sort_order)
VALUES
    ((SELECT id FROM categories WHERE slug = 'otomobil'),
     'marka', 'Marka', 'SELECT',
     '["Audi","BMW","Fiat","Ford","Honda","Hyundai","Mercedes","Opel","Peugeot","Renault","Toyota","Volkswagen","Volvo","Diğer"]',
     true, NULL, 1),

    ((SELECT id FROM categories WHERE slug = 'otomobil'),
     'model_yili', 'Model Yılı', 'NUMBER', NULL,
     true, NULL, 2),

    ((SELECT id FROM categories WHERE slug = 'otomobil'),
     'kilometre', 'Kilometre', 'NUMBER', NULL,
     true, 'km', 3),

    ((SELECT id FROM categories WHERE slug = 'otomobil'),
     'yakit_tipi', 'Yakıt Tipi', 'SELECT',
     '["Benzin","Dizel","LPG","Elektrik","Hibrit"]',
     true, NULL, 4),

    ((SELECT id FROM categories WHERE slug = 'otomobil'),
     'vites', 'Vites', 'SELECT',
     '["Manuel","Otomatik","Yarı Otomatik"]',
     true, NULL, 5),

    ((SELECT id FROM categories WHERE slug = 'otomobil'),
     'kasa_tipi', 'Kasa Tipi', 'SELECT',
     '["Sedan","Hatchback","SUV","Station Wagon","Coupe","Cabrio","Pick-up"]',
     false, NULL, 6),

    ((SELECT id FROM categories WHERE slug = 'otomobil'),
     'renk', 'Renk', 'SELECT',
     '["Beyaz","Siyah","Gri","Kırmızı","Mavi","Lacivert","Gümüş","Kahverengi","Diğer"]',
     false, NULL, 7)
ON CONFLICT ON CONSTRAINT uq_category_attribute DO NOTHING;

-- ─── LAPTOP ───
INSERT INTO category_attributes (category_id, name, label, field_type, options, is_required, unit, sort_order)
VALUES
    ((SELECT id FROM categories WHERE slug = 'laptop'),
     'islemci', 'İşlemci', 'TEXT', NULL,
     true, NULL, 1),

    ((SELECT id FROM categories WHERE slug = 'laptop'),
     'ram', 'RAM', 'SELECT',
     '["4 GB","8 GB","16 GB","32 GB","64 GB"]',
     true, NULL, 2),

    ((SELECT id FROM categories WHERE slug = 'laptop'),
     'depolama', 'Depolama', 'SELECT',
     '["128 GB SSD","256 GB SSD","512 GB SSD","1 TB SSD","1 TB HDD","2 TB HDD"]',
     true, NULL, 3),

    ((SELECT id FROM categories WHERE slug = 'laptop'),
     'ekran_boyutu', 'Ekran Boyutu', 'SELECT',
     '["13 inç","14 inç","15.6 inç","16 inç","17.3 inç"]',
     false, NULL, 4),

    ((SELECT id FROM categories WHERE slug = 'laptop'),
     'ekran_karti', 'Ekran Kartı', 'TEXT', NULL,
     false, NULL, 5)
ON CONFLICT ON CONSTRAINT uq_category_attribute DO NOTHING;

-- ─── APPLE TELEFONLAR ───
INSERT INTO category_attributes (category_id, name, label, field_type, options, is_required, unit, sort_order)
VALUES
    ((SELECT id FROM categories WHERE slug = 'apple-telefonlar'),
     'dahili_hafiza', 'Dahili Hafıza', 'SELECT',
     '["64 GB","128 GB","256 GB","512 GB","1 TB"]',
     true, NULL, 1),

    ((SELECT id FROM categories WHERE slug = 'apple-telefonlar'),
     'renk', 'Renk', 'TEXT', NULL,
     true, NULL, 2),

    ((SELECT id FROM categories WHERE slug = 'apple-telefonlar'),
     'garanti_durumu', 'Garanti Durumu', 'SELECT',
     '["Garantisi Var","Garantisi Yok","Apple Care+"]',
     false, NULL, 3)
ON CONFLICT ON CONSTRAINT uq_category_attribute DO NOTHING;

-- ─── SAMSUNG TELEFONLAR ───
INSERT INTO category_attributes (category_id, name, label, field_type, options, is_required, unit, sort_order)
VALUES
    ((SELECT id FROM categories WHERE slug = 'samsung-telefonlar'),
     'dahili_hafiza', 'Dahili Hafıza', 'SELECT',
     '["64 GB","128 GB","256 GB","512 GB","1 TB"]',
     true, NULL, 1),

    ((SELECT id FROM categories WHERE slug = 'samsung-telefonlar'),
     'renk', 'Renk', 'TEXT', NULL,
     true, NULL, 2),

    ((SELECT id FROM categories WHERE slug = 'samsung-telefonlar'),
     'garanti_durumu', 'Garanti Durumu', 'SELECT',
     '["Garantisi Var","Garantisi Yok"]',
     false, NULL, 3)
ON CONFLICT ON CONSTRAINT uq_category_attribute DO NOTHING;

-- ─── KOLTUK & KANEPE ───
INSERT INTO category_attributes (category_id, name, label, field_type, options, is_required, unit, sort_order)
VALUES
    ((SELECT id FROM categories WHERE slug = 'koltuk-kanepe'),
     'kisi_sayisi', 'Kişi Sayısı', 'SELECT',
     '["2 Kişilik","3 Kişilik","4 Kişilik","Köşe Takım","L Koltuk"]',
     true, NULL, 1),

    ((SELECT id FROM categories WHERE slug = 'koltuk-kanepe'),
     'malzeme', 'Malzeme', 'SELECT',
     '["Kumaş","Deri","Suni Deri","Kadife","Microfiber"]',
     false, NULL, 2),

    ((SELECT id FROM categories WHERE slug = 'koltuk-kanepe'),
     'renk', 'Renk', 'TEXT', NULL,
     false, NULL, 3)
ON CONFLICT ON CONSTRAINT uq_category_attribute DO NOTHING;

-- ─── OYUN KONSOLLARI ───
INSERT INTO category_attributes (category_id, name, label, field_type, options, is_required, unit, sort_order)
VALUES
    ((SELECT id FROM categories WHERE slug = 'oyun-konsollari'),
     'marka_model', 'Marka / Model', 'SELECT',
     '["PlayStation 5","PlayStation 5 Digital","PlayStation 4","Xbox Series X","Xbox Series S","Nintendo Switch","Nintendo Switch OLED","Steam Deck"]',
     true, NULL, 1),

    ((SELECT id FROM categories WHERE slug = 'oyun-konsollari'),
     'depolama', 'Depolama Kapasitesi', 'SELECT',
     '["256 GB","500 GB","825 GB","1 TB","2 TB"]',
     false, NULL, 2),

    ((SELECT id FROM categories WHERE slug = 'oyun-konsollari'),
     'kol_sayisi', 'Kol Sayısı', 'SELECT',
     '["1","2","3","4"]',
     true, NULL, 3)
ON CONFLICT ON CONSTRAINT uq_category_attribute DO NOTHING;
