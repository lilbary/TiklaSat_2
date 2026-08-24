-- pg_trgm: Postgres'e "trigram" (3'lü harf grubu) tabanlı arama desteği ekleyen uzantı.
-- Bu olmadan ILIKE '%kelime%' gibi aramalar index kullanamaz, her seferinde
-- tüm tabloyu satır satır tarar (yavaş). Bu uzantı sayesinde bir GIN index kurabiliyoruz.
CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE INDEX idx_listings_title_trgm ON listings USING GIN (title gin_trgm_ops);