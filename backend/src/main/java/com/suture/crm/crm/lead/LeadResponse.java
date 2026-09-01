package com.suture.crm.crm.lead;

import java.time.OffsetDateTime;
import java.util.UUID;

public record LeadResponse(UUID id, UUID companyId, String companyName, UUID contactId, String source, String temperature, String status,
                           String priority, String dataQuality, OffsetDateTime nextContactAt, Integer score, String summary) {
    static LeadResponse from(Lead lead, String companyName) {
        return new LeadResponse(lead.getId(), lead.getCompanyId(), companyName, lead.getContactId(), lead.getSource(), lead.getTemperature(),
                lead.getStatus(), lead.getPriority(), lead.getDataQuality(), lead.getNextContactAt(), lead.getScore(), lead.getSummary());
    }
}
