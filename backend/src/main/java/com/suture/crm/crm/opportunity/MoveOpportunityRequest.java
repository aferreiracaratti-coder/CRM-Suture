package com.suture.crm.crm.opportunity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record MoveOpportunityRequest(
        @NotBlank @Pattern(regexp = "NEW|CONTACTED|DISCOVERY|QUALIFIED|PROPOSAL|NEGOTIATION|WON|LOST") String stage,
        @Size(max = 1000) String lostReason) { }
