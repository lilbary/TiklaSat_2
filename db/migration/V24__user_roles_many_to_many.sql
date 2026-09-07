-- BR-U-002 / BR-U-003: tek "role" kolonundan çoklu role geçiş.
--
-- Bir kullanıcı aynı anda BUYER + SELLER (+ ADMIN) olabilmeli, dolayısıyla
-- users tablosundaki tek kolon yetmiyor; ayrı bir bağlantı tablosu gerekiyor.

CREATE TABLE user_roles (
    user_id UUID        NOT NULL,
    role    VARCHAR(20) NOT NULL,

    -- Bileşik birincil anahtar iki işi birden yapıyor:
    -- 1) aynı rolün aynı kullanıcıya iki kez yazılmasını engelliyor,
    -- 2) user_id baştaki kolon olduğu için "bu kullanıcının rolleri" sorgusu
    --    (her oturum açmada çalışıyor) doğrudan index üzerinden gidiyor.
    CONSTRAINT pk_user_roles PRIMARY KEY (user_id, role),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id)
        REFERENCES users (id) ON DELETE CASCADE
);

-- Kayıt olan herkes BUYER'dır (BR-U-003) — eski ADMIN'ler dahil.
INSERT INTO user_roles (user_id, role)
SELECT id, 'BUYER' FROM users;

-- Eski ADMIN'ler adminliğini korusun.
INSERT INTO user_roles (user_id, role)
SELECT id, 'ADMIN' FROM users WHERE role = 'ADMIN';

-- Geriye dönük tutarlılık: hâlihazırda ilanı olan herkes SELLER olmalı.
-- Yeni kural bundan sonra ilk ilanda rolü ekleyecek, ama mevcut kullanıcıların
-- ilanları o kod yolundan geçmediği için burada bir kereye mahsus dolduruyoruz.
INSERT INTO user_roles (user_id, role)
SELECT DISTINCT seller_id, 'SELLER' FROM listings
ON CONFLICT ON CONSTRAINT pk_user_roles DO NOTHING;

-- Eski tek-rol kolonu artık yanıltıcı; iki kaynak bir arada kalmasın.
ALTER TABLE users DROP COLUMN role;
