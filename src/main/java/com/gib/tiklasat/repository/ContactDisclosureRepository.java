package com.gib.tiklasat.repository;

import com.gib.tiklasat.entity.ContactDisclosure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ContactDisclosureRepository extends JpaRepository<ContactDisclosure, UUID> {
    List<ContactDisclosure> findBySubjectUserIdOrderByDisclosedAtDesc(UUID subjectUserId);
}
