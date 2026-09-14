package com.gib.tiklasat.address;

import com.gib.tiklasat.address.Address;
import com.gib.tiklasat.listing.Listing;
import com.gib.tiklasat.user.User;
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
