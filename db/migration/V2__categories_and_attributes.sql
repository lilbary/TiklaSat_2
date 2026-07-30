-- =====================================================================
-- TıklaSat · V2 · Kategori Ağacı ve Dinamik Özellikler (EAV)
-- ---------------------------------------------------------------------
-- Kapsanan kurallar: BR-C-001..007
-- Amaç: Yeni kategori ve yeni ilan alanı eklemek KOD DEĞİŞİKLİĞİ veya
--       MIGRATION gerektirmesin. "sahibinden.com gibi" olmanın teknik
--       karşılığı budur.
-- =====================================================================

-- =====================================================================
-- KATEGORİ AĞACI
-- =====================================================================

CREATE TABLE categories (
    id          UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    parent_id   UUID,                       -- NULL = kök kategori
    name        VARCHAR(80)   NOT NULL,     -- "Otomobil"
    slug        VARCHAR(80)   NOT NULL,     -- "otomobil"

    -- Materialized path: '/vasita/otomobil/sedan/'
    -- "Vasıta ve tüm altları" sorgusunu WITH RECURSIVE olmadan çözer.
    path        TEXT          NOT NULL,

    depth       SMALLINT      NOT NULL,     -- BR-C-001 · 1..4
    is_leaf     BOOLEAN       NOT NULL DEFAULT true,   -- BR-C-002
    sort_order  INTEGER       NOT NULL DEFAULT 0,
    is_active   BOOLEAN       NOT NULL DEFAULT true,   -- BR-C-007
    icon        VARCHAR(50),

    created_at  TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ   NOT NULL DEFAULT now(),

    CONSTRAINT fk_categories_parent FOREIGN KEY (parent_id)
        REFERENCES categories (id) ON DELETE RESTRICT,

    -- BR-C-001 · en fazla 4 seviye
    CONSTRAINT ck_categories_depth CHECK (depth BETWEEN 1 AND 4),

    -- Kök kategorinin derinliği 1'dir; alt kategorinin ebeveyni olmalıdır.
    CONSTRAINT ck_categories_root CHECK (
        (parent_id IS NULL AND depth = 1) OR (parent_id IS NOT NULL AND depth > 1)
    ),

    CONSTRAINT ck_categories_path_format CHECK (
        path LIKE '/%' AND path LIKE '%/'
    ),

    CONSTRAINT uq_categories_path UNIQUE (path)
);

-- Kardeş kategoriler aynı slug'a sahip olamaz.
-- NULLS NOT DISTINCT (PostgreSQL 15+) olmadan, parent_id NULL olan kök
-- kategoriler birbirinden "farklı" sayılır ve kural kökte çalışmazdı.
ALTER TABLE categories
    ADD CONSTRAINT uq_categories_parent_slug
    UNIQUE NULLS NOT DISTINCT (parent_id, slug);

CREATE TRIGGER trg_categories_updated_at
    BEFORE UPDATE ON categories
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE INDEX ix_categories_parent ON categories (parent_id, sort_order);

-- text_pattern_ops: LIKE '/vasita/%' önek aramasının index kullanabilmesi
-- için ZORUNLUDUR. Varsayılan operatör sınıfı collation'a bağlıdır ve
-- LIKE sorgularında index'i devre dışı bırakır.
CREATE INDEX ix_categories_path ON categories (path text_pattern_ops);

CREATE INDEX ix_categories_active ON categories (is_active, depth)
    WHERE is_active;

-- =====================================================================
-- DİNAMİK ÖZELLİK TANIMLARI (BR-C-003)
-- =====================================================================

CREATE TABLE attribute_definitions (
    id            UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    category_id   UUID          NOT NULL,
    code          VARCHAR(50)   NOT NULL,   -- 'mileage_km'
    label         VARCHAR(80)   NOT NULL,   -- 'Kilometre'
    data_type     VARCHAR(16)   NOT NULL,   -- BR-C-005
    unit          VARCHAR(16),              -- 'km', 'm²'
    is_required   BOOLEAN       NOT NULL DEFAULT false,   -- BR-C-004
    is_filterable BOOLEAN       NOT NULL DEFAULT true,

    -- Sayısal alanlar için doğrulama sınırları (model yılı 1900..2027 gibi)
    min_value     NUMERIC(18,4),
    max_value     NUMERIC(18,4),
    max_length    INTEGER,                  -- TEXT tipi için

    sort_order    INTEGER       NOT NULL DEFAULT 0,
    is_active     BOOLEAN       NOT NULL DEFAULT true,
    created_at    TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ   NOT NULL DEFAULT now(),

    CONSTRAINT fk_attr_def_category FOREIGN KEY (category_id)
        REFERENCES categories (id) ON DELETE CASCADE,

    CONSTRAINT uq_attr_def_category_code UNIQUE (category_id, code),

    -- BR-C-005
    CONSTRAINT ck_attr_def_data_type CHECK (data_type IN (
        'TEXT', 'INTEGER', 'DECIMAL', 'BOOLEAN', 'DATE', 'ENUM'
    )),

    CONSTRAINT ck_attr_def_range CHECK (
        min_value IS NULL OR max_value IS NULL OR max_value >= min_value
    ),

    -- Sayısal sınırlar yalnızca sayısal tiplerde anlamlıdır.
    CONSTRAINT ck_attr_def_numeric_bounds CHECK (
        (min_value IS NULL AND max_value IS NULL)
        OR data_type IN ('INTEGER', 'DECIMAL')
    ),

    CONSTRAINT ck_attr_def_max_length CHECK (
        max_length IS NULL OR (data_type = 'TEXT' AND max_length > 0)
    )
);

CREATE TRIGGER trg_attr_def_updated_at
    BEFORE UPDATE ON attribute_definitions
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE INDEX ix_attr_def_category ON attribute_definitions (category_id, sort_order)
    WHERE is_active;

-- ---------------------------------------------------------------------
-- ENUM tipli alanların seçenek listesi
-- ---------------------------------------------------------------------
CREATE TABLE attribute_options (
    id                       UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    attribute_definition_id  UUID         NOT NULL,
    value                    VARCHAR(60)  NOT NULL,   -- 'DIESEL'
    label                    VARCHAR(80)  NOT NULL,   -- 'Dizel'
    sort_order               INTEGER      NOT NULL DEFAULT 0,
    is_active                BOOLEAN      NOT NULL DEFAULT true,

    CONSTRAINT fk_attr_opt_definition FOREIGN KEY (attribute_definition_id)
        REFERENCES attribute_definitions (id) ON DELETE CASCADE,
    CONSTRAINT uq_attr_opt_def_value UNIQUE (attribute_definition_id, value)
);

CREATE INDEX ix_attr_opt_definition ON attribute_options (attribute_definition_id, sort_order);

-- =====================================================================
-- Yorumlar
-- =====================================================================
COMMENT ON TABLE  categories       IS 'BR-C-001..002 · Kategori ağacı. İlan yalnızca is_leaf=true olan kategoriye açılır.';
COMMENT ON COLUMN categories.path  IS 'Materialized path (/vasita/otomobil/). Alt ağaç sorgusunu LIKE ile tek adımda çözer.';
COMMENT ON COLUMN categories.depth IS 'BR-C-001 · 1..4. Kök kategori depth=1.';
COMMENT ON TABLE  attribute_definitions IS 'BR-C-003 · Kategoriye özel ilan alanları. Yeni alan eklemek migration gerektirmez.';
COMMENT ON TABLE  attribute_options     IS 'BR-C-005 · Yalnızca data_type=ENUM olan tanımların seçenek listesi.';

-- NOT: listing_attribute_values tablosu, listings tablosuna bağımlı
--      olduğu için V3'te oluşturulur.
