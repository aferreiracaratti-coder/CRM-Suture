package com.suture.crm.crm.opportunity;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/opportunities")
public class OpportunityController {
    private final OpportunityService service;
    OpportunityController(OpportunityService service) { this.service = service; }

    @GetMapping
    public List<OpportunityResponse> list(@RequestHeader("X-Tenant-Id") UUID tenantId) { return service.list(tenantId); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OpportunityResponse create(@RequestHeader("X-Tenant-Id") UUID tenantId, @Valid @RequestBody CreateOpportunityRequest request) {
        return service.create(tenantId, request);
    }

    @PatchMapping("/{id}/stage")
    public OpportunityResponse move(@RequestHeader("X-Tenant-Id") UUID tenantId, @PathVariable UUID id,
                                    @Valid @RequestBody MoveOpportunityRequest request) {
        return service.move(tenantId, id, request);
    }
}
