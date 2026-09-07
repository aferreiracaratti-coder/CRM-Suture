package com.suture.crm.crm.opportunity;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import com.suture.crm.auth.CrmUserPrincipal;

@RestController
@RequestMapping("/api/opportunities")
public class OpportunityController {
    private final OpportunityService service;
    OpportunityController(OpportunityService service) { this.service = service; }

    @GetMapping
    public List<OpportunityResponse> list(@AuthenticationPrincipal CrmUserPrincipal principal) { return service.list(principal.tenantId()); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OpportunityResponse create(@AuthenticationPrincipal CrmUserPrincipal principal, @Valid @RequestBody CreateOpportunityRequest request) {
        return service.create(principal.tenantId(), principal.id(), request);
    }

    @PatchMapping("/{id}/stage")
    public OpportunityResponse move(@AuthenticationPrincipal CrmUserPrincipal principal, @PathVariable UUID id,
                                    @Valid @RequestBody MoveOpportunityRequest request) {
        return service.move(principal.tenantId(), principal.id(), id, request);
    }
}
