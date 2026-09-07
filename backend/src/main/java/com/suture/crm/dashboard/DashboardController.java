package com.suture.crm.dashboard;

import com.suture.crm.auth.CrmUserPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    private final DashboardService service;
    DashboardController(DashboardService service) { this.service = service; }
    @GetMapping("/today") public DashboardResponse today(@AuthenticationPrincipal CrmUserPrincipal principal) { return service.today(principal.tenantId()); }
}
