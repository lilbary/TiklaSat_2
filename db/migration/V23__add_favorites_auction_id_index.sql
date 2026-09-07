-- notifyFavoritedAuctionsEndingSoon her açık artırma için favorites'ı auction_id ile
-- sorguluyor. Mevcut (user_id, auction_id) unique index'i bu sorguda ancak tam tarama
-- ile kullanılabiliyor, çünkü auction_id baştaki kolon değil.
CREATE INDEX idx_favorites_auction_id ON favorites (auction_id);