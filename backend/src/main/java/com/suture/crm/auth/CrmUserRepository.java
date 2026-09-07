package com.suture.crm.auth;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CrmUserRepository extends JpaRepository<CrmUser, UUID> {
    Optional<CrmUser> findByEmailIgnoreCase(String email);
    List<CrmUser> findByTenantIdOrderByDisplayNameAsc(UUID tenantId);
    Optional<CrmUser> findByIdAndTenantId(UUID id, UUID tenantId);
    boolean existsByTenantIdAndEmailIgnoreCase(UUID tenantId, String email);
}
