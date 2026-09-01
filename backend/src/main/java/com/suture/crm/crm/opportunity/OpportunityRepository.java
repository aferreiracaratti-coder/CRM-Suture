package com.suture.crm.crm.opportunity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OpportunityRepository extends JpaRepository<Opportunity, UUID> {
    List<Opportunity> findByTenantIdOrderByStageAscNextActionDateAsc(UUID tenantId);
    Optional<Opportunity> findByIdAndTenantId(UUID id, UUID tenantId);
}
