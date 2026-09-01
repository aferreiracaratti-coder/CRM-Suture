package com.suture.crm.dashboard;

import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    private final DashboardService service;
    DashboardController(DashboardService service) { this.service = service; }
    @GetMapping("/today") public DashboardResponse today(@RequestHeader("X-Tenant-Id") UUID tenantId) { return service.today(tenantId); }
}
