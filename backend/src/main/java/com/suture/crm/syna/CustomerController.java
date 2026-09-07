package com.suture.crm.syna;

import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {
    private final SynaReadService service;
    CustomerController(SynaReadService service) { this.service = service; }
    @GetMapping public SynaReadService.SynaPage<SynaReadService.SynaCustomer> list(HttpServletRequest request, @RequestParam(required = false) String query, @RequestParam(defaultValue = "20") int limit) { return service.customers(SynaConnectionContext.require(request).tenantId(), query, limit); }
    @GetMapping("/{id}") public SynaReadService.SynaCustomer get(HttpServletRequest request, @PathVariable UUID id) { return service.customer(SynaConnectionContext.require(request).tenantId(), id); }
}
