package com.suture.crm.crm.contact;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContactRepository extends JpaRepository<Contact, UUID> {
    List<Contact> findByTenantIdAndCompanyIdOrderByFirstNameAscLastNameAsc(UUID tenantId, UUID companyId);
    List<Contact> findByTenantIdOrderByFirstNameAscLastNameAsc(UUID tenantId);
    boolean existsByIdAndTenantId(UUID id, UUID tenantId);
    Optional<Contact> findByIdAndTenantId(UUID id, UUID tenantId);
}
