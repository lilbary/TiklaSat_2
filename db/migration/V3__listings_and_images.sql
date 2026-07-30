-- =====================================================================
-- TıklaSat · V3 · İlan, Fotoğraflar ve EAV Değerleri
-- ---------------------------------------------------------------------
-- Kapsanan kurallar: BR-L-001..011, BR-C-004, BR-C-005
-- Not: listings, açık artırmanın "soğuk" tarafıdır — nadiren yazılır,
--      çok okunur. Sıcak taraf (auctions) V4'te ayrı tabloda durur.
--      Gerekçe: ADR-0003.
-- =====================================================================

CREATE TABLE listings (
    id               UUID           PRIMARY KEY DEFAULT gen_random_uuid(),
    seller_id        UUID           NOT NULL,
    category_id      UUID           NOT NULL,

    title            VARCHAR(70)    NOT NULL,     -- BR-L-001
    description      VARCHAR(3000)  NOT NULL,     -- BR-L-002

    city_id          SMALLINT       NOT NULL,     -- BR-L-005
    district_id      INTEGER        NOT NULL,

    status           VARCHAR(20)    NOT NULL DEFAULT 'DRAFT',   -- BR-L-006

    -- Moderasyon (BR-L-007)
    moderated_by     UUID,
    moderated_at     TIMESTAMPTZ,
    moderation_note  VARCHAR(500),

    published_at     TIMESTAMPTZ,
    view_count       INTEGER        NOT NULL DEFAULT 0,

    -- BR-L-010 · Tam metin arama
    -- GENERATED ALWAYS ... STORED: PostgreSQL kolonu kendisi hesaplar ve
    -- title/description değiştiğinde otomatik tazeler. Uygulama kodunun
    -- bu kolonu güncellemesi ne gerekir ne de mümkündür → "arama index'ini
    -- güncellemeyi unuttum" hatası yapısal olarak imkânsızdır.
    -- setweight: başlıktaki eşleşme ('A') açıklamadakinden ('B') değerlidir.
    search_vector    TSVECTOR
        GENERATED ALWAYS AS (
            setweight(to_tsvector('turkish', coalesce(title, '')),       'A') ||
            setweight(to_tsvector('turkish', coalesce(description, '')), 'B')
        ) STORED,

    created_at       TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ    NOT NULL DEFAULT now(),
    version          BIGINT         NOT NULL DEFAULT 0,

    CONSTRAINT fk_listings_seller FOREIGN KEY (seller_id)
        REFERENCES users (id) ON DELETE RESTRICT,      -- BR-L-011 · geçmiş korunur
    CONSTRAINT fk_listings_category FOREIGN KEY (category_id)
        REFERENCES categories (id) ON DELETE RESTRICT, -- BR-C-007
    CONSTRAINT fk_listings_city FOREIGN KEY (city_id)
        REFERENCES cities (id) ON DELETE RESTRICT,
    CONSTRAINT fk_listings_district FOREIGN KEY (district_id)
        REFERENCES districts (id) ON DELETE RESTRICT,
    CONSTRAINT fk_listings_moderator FOREIGN KEY (moderated_by)
        REFERENCES users (id) ON DELETE SET NULL,

    -- BR-L-001 · char_length kullanılır (VARCHAR(70) yalnızca üst sınırı verir)
    CONSTRAINT ck_listings_title_length CHECK (char_length(title) BETWEEN 10 AND 70),

    -- BR-L-002
    CONSTRAINT ck_listings_description_length CHECK (char_length(description) <= 3000),

    -- BR-L-006
    CONSTRAINT ck_listings_status CHECK (status IN (
        'DRAFT', 'PENDING_REVIEW', 'APPROVED', 'REJECTED', 'ARCHIVED'
    )),

    -- BR-L-007 · reddedilen ilanda gerekçe zorunlu
    CONSTRAINT ck_listings_moderation CHECK (
        status <> 'REJECTED'
        OR (moderation_note IS NOT NULL AND moderated_by IS NOT NULL)
    ),

    -- Yayınlanmış ilanın yayın tarihi olmalıdır
    CONSTRAINT ck_listings_published CHECK (
        status <> 'APPROVED' OR published_at IS NOT NULL
    ),

    CONSTRAINT ck_listings_view_count CHECK (view_count >= 0)
);

CREATE TRIGGER trg_listings_updated_at
    BEFORE UPDATE ON listings
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- BR-L-010 · Tam metin arama
CREATE INDEX ix_listings_search ON listings USING GIN (search_vector);

-- Kategori sayfası
CREATE INDEX ix_listings_category_status
    ON listings (category_id, status, published_at DESC);

-- Konum filtresi · yalnızca yayındaki ilanlar aranır → kısmi index
CREATE INDEX ix_listings_location
    ON listings (city_id, district_id)
    WHERE status = 'APPROVED';

-- "İlanlarım"
CREATE INDEX ix_listings_seller ON listings (seller_id, created_at DESC);

-- Moderasyon kuyruğu
CREATE INDEX ix_listings_moderation_queue
    ON listings (created_at)
    WHERE status = 'PENDING_REVIEW';

-- =====================================================================
-- FOTOĞRAFLAR
-- =====================================================================

CREATE TABLE listing_images (
    id            UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    listing_id    UUID          NOT NULL,

    -- Görselin KENDİSİ veritabanında saklanmaz; nesne deposundaki yolu tutulur.
    storage_key   VARCHAR(255)  NOT NULL,
    content_type  VARCHAR(50)   NOT NULL,
    size_bytes    INTEGER       NOT NULL,
    width         INTEGER       NOT NULL,
    height        INTEGER       NOT NULL,

    sort_order    SMALLINT      NOT NULL,   -- 0..14 → en fazla 15 fotoğraf
    is_cover      BOOLEAN       NOT NULL DEFAULT false,   -- BR-L-004
    created_at    TIMESTAMPTZ   NOT NULL DEFAULT now(),

    CONSTRAINT fk_listing_images_listing FOREIGN KEY (listing_id)
        REFERENCES listings (id) ON DELETE CASCADE,

    CONSTRAINT uq_listing_images_storage_key UNIQUE (storage_key),
    CONSTRAINT uq_listing_images_order UNIQUE (listing_id, sort_order),

    -- BR-L-003 · üst sınır (15 fotoğraf) sort_order aralığıyla garanti edilir
    CONSTRAINT ck_listing_images_order CHECK (sort_order BETWEEN 0 AND 14),
    CONSTRAINT ck_listing_images_size  CHECK (size_bytes BETWEEN 1 AND 5242880),
    CONSTRAINT ck_listing_images_type  CHECK (content_type IN (
        'image/jpeg', 'image/png', 'image/webp'
    )),
    CONSTRAINT ck_listing_images_dimensions CHECK (width > 0 AND height > 0)
);

-- BR-L-004 · Kısmi benzersiz index
-- Index'e yalnızca is_cover=true satırlar girer → bir ilana ikinci kapak
-- eklenmesi benzersizlik ihlali olur. Trigger yazmadan, veritabanı
-- seviyesinde "her ilanın en fazla bir kapağı olur" garantisi.
CREATE UNIQUE INDEX uq_listing_images_single_cover
    ON listing_images (listing_id)
    WHERE is_cover;

CREATE INDEX ix_listing_images_listing ON listing_images (listing_id, sort_order);

-- BİLİNÇLİ İSTİSNA (BR-L-003 alt sınırı):
-- "En az 1 fotoğraf" kuralı tek satırlık CHECK ile ifade EDİLEMEZ; bir
-- satırın kısıtı kardeş satırların sayısını göremez. Trigger ile yapılabilirdi
-- ancak trigger'lar ilan oluşturma akışında görünmez yan etki üretir ve hata
-- ayıklamayı zorlaştırır. KARAR: alt sınır servis katmanında doğrulanır.
-- Bu istisna data-model.md §5'te de kayıtlıdır.

-- =====================================================================
-- EAV DEĞERLERİ (BR-C-004, BR-C-005)
-- =====================================================================

CREATE TABLE listing_attribute_values (
    id                       UUID           PRIMARY KEY DEFAULT gen_random_uuid(),
    listing_id               UUID           NOT NULL,
    attribute_definition_id  UUID           NOT NULL,

    -- TİPLİ DEĞER KOLONLARI — bu tasarımın can alıcı noktası.
    -- Klasik EAV tüm değerleri tek bir "value TEXT" kolonunda saklar; o zaman
    -- "100.000-200.000 km arası" sorgusu CAST(value AS NUMERIC) gerektirir,
    -- CAST index'i kullanılamaz hale getirir ve her arama tüm tabloyu tarar.
    -- Ayrı tipli kolonlarla aralık sorgusu doğrudan index kullanır ve
    -- NUMERIC kolona sayı olmayan değer zaten girilemez.
    value_text     TEXT,
    value_number   NUMERIC(18,4),   -- INTEGER ve DECIMAL buraya
    value_bool     BOOLEAN,
    value_date     DATE,
    option_id      UUID,            -- ENUM için

    created_at     TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ     NOT NULL DEFAULT now(),

    CONSTRAINT fk_lav_listing FOREIGN KEY (listing_id)
        REFERENCES listings (id) ON DELETE CASCADE,
    CONSTRAINT fk_lav_definition FOREIGN KEY (attribute_definition_id)
        REFERENCES attribute_definitions (id) ON DELETE RESTRICT,
    CONSTRAINT fk_lav_option FOREIGN KEY (option_id)
        REFERENCES attribute_options (id) ON DELETE RESTRICT,

    -- Bir ilanın bir alanı bir kez olur
    CONSTRAINT uq_lav_listing_attr UNIQUE (listing_id, attribute_definition_id),

    -- Tam olarak BİR değer kolonu dolu olmalı.
    -- Olmasaydı hem value_text='120000' hem value_number=120000 yazan bir satır
    -- oluşabilir, ikisi zamanla ayrışır ve hangisinin doğru olduğu bilinemezdi.
    CONSTRAINT ck_lav_exactly_one_value CHECK (
        num_nonnulls(value_text, value_number, value_bool, value_date, option_id) = 1
    )
);

CREATE TRIGGER trg_lav_updated_at
    BEFORE UPDATE ON listing_attribute_values
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- Aralık filtreleri: "100.000-200.000 km arası"
-- Kolon sırası önemli: önce HANGİ alan, sonra değer. Ters sırada farklı
-- alanların değerleri index'te iç içe geçer ve aralık taraması işe yaramaz.
CREATE INDEX ix_lav_number
    ON listing_attribute_values (attribute_definition_id, value_number)
    WHERE value_number IS NOT NULL;

-- Seçenek filtreleri: "Yakıt = Dizel"
CREATE INDEX ix_lav_option
    ON listing_attribute_values (attribute_definition_id, option_id)
    WHERE option_id IS NOT NULL;

-- Tarih filtreleri
CREATE INDEX ix_lav_date
    ON listing_attribute_values (attribute_definition_id, value_date)
    WHERE value_date IS NOT NULL;

-- İlan detayında tüm özellikleri getir
CREATE INDEX ix_lav_listing ON listing_attribute_values (listing_id);

-- =====================================================================
-- Yorumlar
-- =====================================================================
COMMENT ON TABLE  listings               IS 'İlan içeriği (SOĞUK veri). Fiyat/zaman mekanizması auctions tablosundadır — ADR-0003.';
COMMENT ON COLUMN listings.search_vector IS 'BR-L-010 · Üretilmiş kolon. title/description değişince PostgreSQL otomatik tazeler.';
COMMENT ON COLUMN listings.status        IS 'BR-L-006 · DRAFT → PENDING_REVIEW → APPROVED|REJECTED → ARCHIVED';
COMMENT ON TABLE  listing_images         IS 'BR-L-003/004 · Üst sınır (15) sort_order ile DB''de; alt sınır (1) servis katmanında.';
COMMENT ON TABLE  listing_attribute_values IS 'EAV değerleri. Tipli kolonlar sayesinde aralık filtreleri index kullanır.';
