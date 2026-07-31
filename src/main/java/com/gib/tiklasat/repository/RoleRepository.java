package com.gib.tiklasat.repository;

import com.gib.tiklasat.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Short> {

    /** Rol kodu ile bul: "BUYER", "SELLER", "ADMIN", "MODERATOR" */
    Optional<Role> findByCode(String code);
}
