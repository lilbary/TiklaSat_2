package com.gib.tiklasat.repository;

import com.gib.tiklasat.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReportRepository extends JpaRepository<Report, UUID> {
    List<Report> findByStatusInOrderByCreatedAtAsc(List<String> statuses);
    List<Report> findByListingId(UUID listingId);
}
