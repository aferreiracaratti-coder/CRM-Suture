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

    @Transactional(readOnly = true)
    public List<ContactResponse> listAll(UUID tenantId) {
        return contacts.findByTenantIdOrderByFirstNameAscLastNameAsc(tenantId).stream().map(ContactResponse::from).toList();
    }

    @Transactional
    public ContactResponse create(UUID tenantId, UUID actorId, CreateContactRequest request) {
        assertCompanyBelongsToTenant(tenantId, request.companyId());
        Contact contact = contacts.save(new Contact(tenantId, actorId, request.companyId(), request));
        String name = request.firstName() + (request.lastName() == null ? "" : " " + request.lastName());
        events.publish(tenantId, "CONTACT_CREATED", "CONTACT", contact.getId(), name);
        audit.recordUserAction(tenantId, actorId, "CONTACT_CREATED", "CONTACT", contact.getId(), name);
        return ContactResponse.from(contact);
    }

    @Transactional
    public void delete(UUID tenantId, UUID actorId, UUID id) {
        Contact contact = contacts.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Contacto no encontrado"));
        contacts.delete(contact);
        String name = contact.getFirstName() + (contact.getLastName() == null ? "" : " " + contact.getLastName());
        events.publish(tenantId, "CONTACT_DELETED", "CONTACT", id, name);
        audit.recordUserAction(tenantId, actorId, "CONTACT_DELETED", "CONTACT", id, name);
    }

    private void assertCompanyBelongsToTenant(UUID tenantId, UUID companyId) {
        if (companyId == null || !companies.existsByIdAndTenantId(companyId, tenantId)) {
            throw new ResourceNotFoundException("Empresa no encontrada");
        }
    }
}
