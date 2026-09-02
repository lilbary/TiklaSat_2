-- =====================================================================
-- TıklaSat · V20 · Rezerv Fiyat Kolonu
-- ---------------------------------------------------------------------
-- Satıcının isteğe bağlı olarak belirleyebileceği minimum satış fiyatı.
-- Açık artırma bitiminde en yüksek teklif bu fiyatın altında kalırsa,
-- teklif verilmiş olsa bile satış gerçekleşmez (status = RESERVE_NOT_MET).
-- NULL bırakılırsa eski davranış aynen sürer (en yüksek teklif her zaman kazanır).
-- =====================================================================

ALTER TABLE auctions ADD COLUMN reserve_price NUMERIC(15, 2);