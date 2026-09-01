package com.suture.crm.crm.company;

import com.suture.crm.common.ResourceNotFoundException;
import com.suture.crm.event.DomainEventService;
import com.suture.crm.audit.AuditTrailService;
import com.suture.crm.tenant.TenantRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CompanyService {
    private final CompanyRepository companies;
    private final TenantRepository tenants;
    private final DomainEventService events;
    private final AuditTrailService audit;
    CompanyService(CompanyRepository companies, TenantRepository tenants, DomainEventService events, AuditTrailService audit) { this.companies = companies; this.tenants = tenants; this.events = events; this.audit = audit; }
    @Transactional(readOnly = true) public List<CompanyResponse> list(UUID tenantId) { tenantExists(tenantId); return companies.findByTenantIdOrderByNameAsc(tenantId).stream().map(CompanyResponse::from).toList(); }
    @Transactional public CompanyResponse create(UUID tenantId, CreateCompanyRequest request) {
        tenantExists(tenantId);
        Company company = companies.save(new Company(tenantId, request.name(), request.website(), request.address(), request.phone(),
                request.whatsapp(), request.email(), request.industry(), request.city(), request.country(), request.source()));
        events.publish(tenantId, "COMPANY_CREATED", "COMPANY", company.getId(), request.name());
        audit.recordSystemAction(tenantId, "COMPANY_CREATED", "COMPANY", company.getId(), request.name());
        return CompanyResponse.from(company);
    }
    private void tenantExists(UUID tenantId) { if (!tenants.existsById(tenantId)) throw new ResourceNotFoundException("Tenant no encontrado"); }
}
