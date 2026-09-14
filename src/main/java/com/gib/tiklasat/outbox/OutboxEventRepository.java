package com.gib.tiklasat.outbox;

import com.gib.tiklasat.outbox.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {
    
    // İşlenmemiş eventleri eskiden yeniye doğru (FIFO) getirir.
    List<OutboxEvent> findByProcessedFalseOrderByCreatedAtAsc();
}
