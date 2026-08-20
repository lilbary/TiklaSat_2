-- V8'de eklenen listings.price alanı geri alınıyor: bu projede fiyat kavramı
-- her zaman Auction.startingPrice/currentPrice üzerinden yönetiliyor (Listing = "ne satılıyor",
-- Auction = "nasıl/ne fiyata satılıyor" ayrımı). Listing.price hiçbir yerde okunmuyordu ve
-- POST /api/listings isteğinde zorunlu kılınması CreateAuctionPage akışını kırıyordu
-- (form bu adımda hiç fiyat sormuyor, fiyatı bir sonraki adımda Auction için alıyor).
ALTER TABLE listings DROP COLUMN price;
