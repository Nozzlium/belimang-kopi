-- Tambahkan kolom location hanya jika belum ada
ALTER TABLE merchants ADD COLUMN IF NOT EXISTS location GEOGRAPHY(POINT, 4326);

-- Tambahkan index spasial hanya jika belum ada
CREATE INDEX IF NOT EXISTS idx_merchants_location ON merchants USING GIST (location);