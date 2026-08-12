-- =====================================================================
-- TıklaSat · V1 · Başlangıç Şeması
-- ---------------------------------------------------------------------
-- Bu dosya, doğrudan şu an elimizdeki 5 Java entity sınıfına (User,
-- Category, Listing, Auction, Bid) göre yazılmıştır. Her kolon, aynı
-- adı taşıyan @Column'daki karşılığıyla birebir eşleşir — kod ile
-- şema arasında ayrışma (drift) olmasın diye.
--
-- Kapsam dışı (bilinçli, "temellerden gidelim"): roller, moderasyon,
-- EAV kategori özellikleri, bildirim, outbox, denetim kaydı, rezerv
-- fiyat, sniper koruması. Bunlar docs/ ve eski migration'larda (git
-- geçmişinde) hâlâ mevcut; entity'ler büyüdükçe buraya eklenebilir.
-- =====================================================================

CREATE TABLE users (
    id            UUID          PRIMARY KEY,
    email         VARCHAR(255)  NOT NULL,
    full_name     VARCHAR(100)  NOT NULL,
    password_hash VARCHAR(255)  NOT NULL,
    phone         VARCHAR(20),
    created_at    TIMESTAMPTZ   NOT NULL,

    CONSTRAINT uq_users_email UNIQUE (email)
);

CREATE TABLE categories (
    id         UUID         PRIMARY KEY,
    parent_id  UUID,
    name       VARCHAR(80)  NOT NULL,
    slug       VARCHAR(80)  NOT NULL,
    created_at TIMESTAMPTZ  NOT NULL,

    CONSTRAINT uq_categories_slug UNIQUE (slug),
    CONSTRAINT fk_categories_parent FOREIGN KEY (parent_id)
        REFERENCES categories (id)
);

CREATE TABLE listings (
    id          UUID           PRIMARY KEY,
    seller_id   UUID           NOT NULL,
    category_id UUID           NOT NULL,
    title       VARCHAR(70)    NOT NULL,
    description VARCHAR(3000)  NOT NULL,
    status      VARCHAR(20)    NOT NULL DEFAULT 'DRAFT',
    view_count  INTEGER        NOT NULL DEFAULT 0,
    created_at  TIMESTAMPTZ    NOT NULL,

    CONSTRAINT fk_listings_seller FOREIGN KEY (seller_id)
        REFERENCES users (id),
    CONSTRAINT fk_listings_category FOREIGN KEY (category_id)
        REFERENCES categories (id)
);

CREATE TABLE auctions (
    id          UUID           PRIMARY KEY,
    listing_id  UUID           NOT NULL,
    start_price NUMERIC(15,2)  NOT NULL,
    starts_at   TIMESTAMPTZ    NOT NULL,
    ends_at     TIMESTAMPTZ    NOT NULL,
    status      VARCHAR(20)    NOT NULL DEFAULT 'ACTIVE',
    created_at  TIMESTAMPTZ    NOT NULL,

    CONSTRAINT uq_auctions_listing UNIQUE (listing_id),
    CONSTRAINT fk_auctions_listing FOREIGN KEY (listing_id)
        REFERENCES listings (id)
);

CREATE TABLE bids (
    id         UUID           PRIMARY KEY,
    auction_id UUID           NOT NULL,
    bidder_id  UUID           NOT NULL,
    amount     NUMERIC(15,2)  NOT NULL,
    created_at TIMESTAMPTZ    NOT NULL,

    CONSTRAINT fk_bids_auction FOREIGN KEY (auction_id)
        REFERENCES auctions (id),
    CONSTRAINT fk_bids_bidder FOREIGN KEY (bidder_id)
        REFERENCES users (id)
);
