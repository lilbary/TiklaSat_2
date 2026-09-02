package com.gib.tiklasat.repository;

import com.gib.tiklasat.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, UUID> {

    List<Favorite> findByUserId(UUID userId);

    Optional<Favorite> findByUserIdAndAuctionId(UUID userId, UUID auctionId);

    List<Favorite> findByAuctionId(UUID auctionId);

}
