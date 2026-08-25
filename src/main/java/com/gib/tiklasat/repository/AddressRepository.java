package com.gib.tiklasat.repository;

import com.gib.tiklasat.entity.Address;
import com.gib.tiklasat.entity.Listing;
import com.gib.tiklasat.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AddressRepository extends JpaRepository<Address, UUID> {

    List<Address> findByUserId(UUID userId);
    Optional<Address> findByIdAndUserId(UUID id, UUID userId);
}
