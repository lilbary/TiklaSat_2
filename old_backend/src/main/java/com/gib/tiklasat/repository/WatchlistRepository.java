package com.gib.tiklasat.repository;

import com.gib.tiklasat.entity.Watchlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WatchlistRepository extends JpaRepository<Watchlist, Watchlist.WatchlistId> {
    List<Watchlist> findByUserId(UUID userId);
    List<Watchlist> findByListingId(UUID listingId);
    boolean existsByUserIdAndListingId(UUID userId, UUID listingId);
}
