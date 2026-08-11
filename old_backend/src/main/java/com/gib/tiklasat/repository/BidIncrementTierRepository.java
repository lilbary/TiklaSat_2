package com.gib.tiklasat.repository;

import com.gib.tiklasat.entity.BidIncrementTier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BidIncrementTierRepository extends JpaRepository<BidIncrementTier, Short> {
    List<BidIncrementTier> findByIsActiveTrue();
}
