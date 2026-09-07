package com.suture.crm.crm.opportunity;

import com.suture.crm.audit.AuditTrailService;
import com.suture.crm.common.ResourceNotFoundException;
import com.suture.crm.crm.company.CompanyRepository;
import com.suture.crm.crm.contact.ContactRepository;
import com.suture.crm.event.DomainEventService;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OpportunityService {
    private final OpportunityRepository opportunities;
    private final CompanyRepository companies;
    private final ContactRepository contacts;
    private final DomainEventService events;
    private final AuditTrailService audit;

    OpportunityService(OpportunityRepository opportunities, CompanyRepository companies, ContactRepository contacts,
                       DomainEventService events, AuditTrailService audit) {
        this.opportunities = opportunities; this.companies = companies; this.contacts = contacts; this.events = events; this.audit = audit;
    }

    @Transactional(readOnly = true)
    public List<OpportunityResponse> list(UUID tenantId) {
        return opportunities.findByTenantIdOrderByStageAscNextActionDateAsc(tenantId).stream().map(OpportunityResponse::from).toList();
    }

    @Transactional
    public OpportunityResponse create(UUID tenantId, UUID actorId, CreateOpportunityRequest request) {
        assertReferencesBelongToTenant(tenantId, request.companyId(), request.contactId());
        Opportunity opportunity = opportunities.save(new Opportunity(tenantId, actorId, request));
        events.publish(tenantId, "OPPORTUNITY_CREATED", "OPPORTUNITY", opportunity.getId(), opportunity.getName());
        audit.recordUserAction(tenantId, actorId, "OPPORTUNITY_CREATED", "OPPORTUNITY", opportunity.getId(), opportunity.getName());
        return OpportunityResponse.from(opportunity);
    }

    @Transactional
    public OpportunityResponse move(UUID tenantId, UUID actorId, UUID id, MoveOpportunityRequest request) {
        Opportunity opportunity = opportunities.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Oportunidad no encontrada"));
        opportunity.moveTo(request.stage(), request.lostReason());
        events.publish(tenantId, "OPPORTUNITY_STAGE_CHANGED", "OPPORTUNITY", opportunity.getId(), opportunity.getName());
        audit.recordUserAction(tenantId, actorId, "OPPORTUNITY_STAGE_CHANGED", "OPPORTUNITY", opportunity.getId(), opportunity.getName());
        return OpportunityResponse.from(opportunity);
    }

    @Transactional
    public void delete(UUID tenantId, UUID actorId, UUID id) {
        Opportunity opportunity = opportunities.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Oportunidad no encontrada"));
        opportunities.delete(opportunity);
        events.publish(tenantId, "OPPORTUNITY_DELETED", "OPPORTUNITY", id, opportunity.getName());
        audit.recordUserAction(tenantId, actorId, "OPPORTUNITY_DELETED", "OPPORTUNITY", id, opportunity.getName());
    }

    private void assertReferencesBelongToTenant(UUID tenantId, UUID companyId, UUID contactId) {
        if (companyId == null || !companies.existsByIdAndTenantId(companyId, tenantId)) {
            throw new ResourceNotFoundException("Empresa no encontrada");
        }
        if (contactId != null && !contacts.existsByIdAndTenantId(contactId, tenantId)) {
            throw new ResourceNotFoundException("Contacto no encontrado");
        }
    }
}
