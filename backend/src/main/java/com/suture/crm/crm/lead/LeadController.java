package com.suture.crm.crm.lead;

import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/leads")
public class LeadController {
    private final LeadService service;
    LeadController(LeadService service) { this.service = service; }

    @GetMapping
    public List<LeadResponse> list(@RequestHeader("X-Tenant-Id") UUID tenantId) { return service.list(tenantId); }
}
