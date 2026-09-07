package com.suture.crm.syna;

import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/deals")
public class DealController {
    private final SynaReadService service;
    DealController(SynaReadService service) { this.service = service; }
    @GetMapping public SynaReadService.SynaPage<SynaReadService.SynaDeal> list(HttpServletRequest request, @RequestParam(required = false) String query, @RequestParam(required = false) String stage, @RequestParam(defaultValue = "20") int limit) { return service.deals(SynaConnectionContext.require(request).tenantId(), query, stage, limit); }
    @GetMapping("/{id}") public SynaReadService.SynaDeal get(HttpServletRequest request, @PathVariable UUID id) { return service.deal(SynaConnectionContext.require(request).tenantId(), id); }
}
