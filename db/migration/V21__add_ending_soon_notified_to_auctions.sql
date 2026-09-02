ALTER TABLE auctions ADD COLUMN ending_soon_notified BOOLEAN NOT NULL DEFAULT false;

CREATE INDEX idx_auctions_ending_soon
ON auctions (ends_at)
WHERE status = 'ACTIVE' AND ending_soon_notified = false;