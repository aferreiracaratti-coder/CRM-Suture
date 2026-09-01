package com.suture.crm.crm.opportunity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record OpportunityResponse(UUID id, UUID companyId, UUID contactId, String name, String stage,
                                  BigDecimal estimatedValue, String currency, Short probability, LocalDate expectedCloseDate,
                                  String nextAction, LocalDate nextActionDate, String lostReason) {
    static OpportunityResponse from(Opportunity opportunity) {
        return new OpportunityResponse(opportunity.getId(), opportunity.getCompanyId(), opportunity.getContactId(),
                opportunity.getName(), opportunity.getStage(), opportunity.getEstimatedValue(), opportunity.getCurrency(),
                opportunity.getProbability(), opportunity.getExpectedCloseDate(), opportunity.getNextAction(),
                opportunity.getNextActionDate(), opportunity.getLostReason());
    }
}
