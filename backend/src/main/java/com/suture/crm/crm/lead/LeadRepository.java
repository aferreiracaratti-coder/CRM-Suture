package com.suture.crm.crm.lead;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeadRepository extends JpaRepository<Lead, UUID> {
    List<Lead> findByTenantIdOrderByPriorityAscNextContactAtAsc(UUID tenantId);
}
