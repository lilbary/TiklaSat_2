package com.gib.tiklasat.repository;

import com.gib.tiklasat.entity.UserRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserRatingRepository extends JpaRepository<UserRating, UUID> {
    List<UserRating> findByRateeIdOrderByCreatedAtDesc(UUID rateeId);
}
