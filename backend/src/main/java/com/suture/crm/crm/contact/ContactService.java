package com.suture.crm.crm.contact;

import com.suture.crm.audit.AuditTrailService;
import com.suture.crm.common.ResourceNotFoundException;
import com.suture.crm.crm.company.CompanyRepository;
import com.suture.crm.event.DomainEventService;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ContactService {
    private final ContactRepository contacts;
    private final CompanyRepository companies;
    private final DomainEventService events;
    private final AuditTrailService audit;

    ContactService(ContactRepository contacts, CompanyRepository companies, DomainEventService events, AuditTrailService audit) {
        this.contacts = contacts; this.companies = companies; this.events = events; this.audit = audit;
    }

    @Transactional(readOnly = true)
    public List<ContactResponse> list(UUID tenantId, UUID companyId) {
        assertCompanyBelongsToTenant(tenantId, companyId);
        return contacts.findByTenantIdAndCompanyIdOrderByFirstNameAscLastNameAsc(tenantId, companyId).stream().map(ContactResponse::from).toList();
    }

    @Transactional
    public ContactResponse create(UUID tenantId, CreateContactRequest request) {
        assertCompanyBelongsToTenant(tenantId, request.companyId());
        Contact contact = contacts.save(new Contact(tenantId, request.companyId(), request));
        String name = request.firstName() + (request.lastName() == null ? "" : " " + request.lastName());
        events.publish(tenantId, "CONTACT_CREATED", "CONTACT", contact.getId(), name);
        audit.recordSystemAction(tenantId, "CONTACT_CREATED", "CONTACT", contact.getId(), name);
        return ContactResponse.from(contact);
    }

    private void assertCompanyBelongsToTenant(UUID tenantId, UUID companyId) {
        if (companyId == null || !companies.existsByIdAndTenantId(companyId, tenantId)) {
            throw new ResourceNotFoundException("Empresa no encontrada");
        }
    }
}
