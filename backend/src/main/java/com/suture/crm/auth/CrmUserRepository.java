package com.suture.crm.auth;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CrmUserRepository extends JpaRepository<CrmUser, UUID> {
    Optional<CrmUser> findByEmailIgnoreCase(String email);
}
