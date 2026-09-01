package com.suture.crm.crm.opportunity;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreateOpportunityRequest(
        UUID companyId,
        UUID contactId,
        @NotBlank @Size(max = 240) String name,
        @Pattern(regexp = "NEW|CONTACTED|DISCOVERY|QUALIFIED|PROPOSAL|NEGOTIATION|WON|LOST") String stage,
        @Min(0) BigDecimal estimatedValue,
        @Pattern(regexp = "[A-Z]{3}") String currency,
        @Min(0) @Max(100) Short probability,
        LocalDate expectedCloseDate,
        String nextAction,
        LocalDate nextActionDate) { }
