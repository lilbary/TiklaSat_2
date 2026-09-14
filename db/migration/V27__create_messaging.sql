-- Site içi mesajlaşma (Facebook tarzı): sohbet ilana değil, KİŞİYE bağlı.
-- İki kullanıcı arasında tek sohbet olur; hangi ilandan başladığı fark etmez.

CREATE TABLE conversations (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_a_id       UUID        NOT NULL,
    user_b_id       UUID        NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    last_message_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT fk_conversations_user_a FOREIGN KEY (user_a_id)
        REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_conversations_user_b FOREIGN KEY (user_b_id)
        REFERENCES users (id) ON DELETE CASCADE,

    -- Kanonik sıra: küçük UUID her zaman user_a olur.
    CONSTRAINT chk_conversation_order CHECK (user_a_id < user_b_id),

    -- Yukarıdaki sıra kuralı sayesinde bu UNIQUE gerçekten tekliği garanti eder.
    CONSTRAINT uq_conversation UNIQUE (user_a_id, user_b_id)
);

CREATE TABLE messages (
    id              UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    conversation_id UUID          NOT NULL,
    sender_id       UUID          NOT NULL,
    body            VARCHAR(2000) NOT NULL,
    created_at      TIMESTAMPTZ   NOT NULL DEFAULT now(),
    read_at         TIMESTAMPTZ,

    CONSTRAINT fk_messages_conversation FOREIGN KEY (conversation_id)
        REFERENCES conversations (id) ON DELETE CASCADE,
    CONSTRAINT fk_messages_sender FOREIGN KEY (sender_id) REFERENCES users (id)
);

-- Sohbet açılınca son mesajlar: conversation_id'ye atla, created_at'e göre
-- tersten oku. Index zaten sıralı olduğu için ayrıca sıralama adımı çalışmıyor.
CREATE INDEX idx_messages_conversation ON messages (conversation_id, created_at DESC);

-- "Sohbetlerim" listesi, en son konuşulan üstte. Kullanıcı bazen user_a
-- bazen user_b tarafında olduğu için iki ayrı index gerekiyor.
CREATE INDEX idx_conversations_user_a ON conversations (user_a_id, last_message_at DESC);
CREATE INDEX idx_conversations_user_b ON conversations (user_b_id, last_message_at DESC);

-- Sağ alttaki kutunun okunmamış rozeti için kısmi index: okunmuş mesajlar
-- (zamanla ezici çoğunluk) index'e hiç girmiyor, index hep küçük kalıyor.
CREATE INDEX idx_messages_unread ON messages (conversation_id, sender_id)
    WHERE read_at IS NULL;