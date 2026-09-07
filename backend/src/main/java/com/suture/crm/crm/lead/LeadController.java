package com.suture.crm.crm.lead;

import java.util.UUID;
import jakarta.servlet.http.HttpServletRequest;
import com.suture.crm.syna.SynaConnectionContext;
import com.suture.crm.syna.SynaReadService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/leads")
public class LeadController {
    private final SynaReadService service;
    LeadController(SynaReadService service) { this.service = service; }

    @GetMapping
    public SynaReadService.SynaPage<SynaReadService.SynaLead> list(HttpServletRequest request,
            @RequestParam(required = false) String query, @RequestParam(defaultValue = "20") int limit) {
        return service.leads(SynaConnectionContext.require(request).tenantId(), query, limit);
    }

    @GetMapping("/{id}")
    public SynaReadService.SynaLead get(HttpServletRequest request, @PathVariable UUID id) {
        return service.lead(SynaConnectionContext.require(request).tenantId(), id);
    }
}
