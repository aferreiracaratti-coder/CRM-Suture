package com.suture.crm.crm.lead;

import com.suture.crm.crm.company.CompanyRepository;
import java.util.Map;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LeadService {
    private final LeadRepository leads;
    private final CompanyRepository companies;
    LeadService(LeadRepository leads, CompanyRepository companies) { this.leads = leads; this.companies = companies; }

    @Transactional(readOnly = true)
    public List<LeadResponse> list(UUID tenantId) {
        Map<UUID, String> companyNames = companies.findByTenantIdOrderByNameAsc(tenantId).stream()
                .collect(java.util.stream.Collectors.toMap(company -> company.getId(), company -> company.getName()));
        return leads.findByTenantIdOrderByPriorityAscNextContactAtAsc(tenantId).stream()
                .map(lead -> LeadResponse.from(lead, companyNames.get(lead.getCompanyId()))).toList();
    }
}
