package com.gib.tiklasat.messaging;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, UUID> {

    /**
     * İki kişi arasındaki sohbeti bul. Çağıran taraf kimlikleri kanonik sıraya
     * (küçük olan önce) sokmuş olmalı — veritabanında da CHECK ile zorlanıyor.
     */
    Optional<Conversation> findByUserAIdAndUserBId(UUID userAId, UUID userBId);

    /**
     * Kullanıcının sohbetleri, en son konuşulan üstte.
     *
     * Kanonik sıralama yüzünden kullanıcı bazı sohbetlerde A, bazılarında B
     * tarafında; bu yüzden iki koşul da gerekiyor. V27'deki iki ayrı index
     * (user_a_id ve user_b_id) tam bunun için var.
     *
     * @EntityGraph: listede karşı tarafın adı gösterileceği için iki kullanıcı
     * da aynı sorguda çekiliyor — sohbet başına ek sorgu (N+1) olmasın.
     */
    @EntityGraph(attributePaths = {"userA", "userB"})
    @Query("""
           SELECT c FROM Conversation c
           WHERE c.userA.id = :userId OR c.userB.id = :userId
           ORDER BY c.lastMessageAt DESC
           """)
    List<Conversation> findByParticipant(@Param("userId") UUID userId, Pageable pageable);
}
