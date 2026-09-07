package com.suture.crm.crm.company;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyRepository extends JpaRepository<Company, UUID> {
    List<Company> findByTenantIdOrderByNameAsc(UUID tenantId);
    boolean existsByIdAndTenantId(UUID id, UUID tenantId);
    Optional<Company> findByIdAndTenantId(UUID id, UUID tenantId);
}
