CREATE TABLE outbox_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_type VARCHAR(100) NOT NULL,
    payload JSONB NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    processed BOOLEAN DEFAULT FALSE
);

-- İşlenmemiş event'leri hızlıca bulabilmek için index (sadece false olanlar)
CREATE INDEX idx_outbox_events_processed ON outbox_events(processed) WHERE processed = false;
