package com.suture.crm.crm.company;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/companies")
public class CompanyController {
    private final CompanyService service;
    CompanyController(CompanyService service) { this.service = service; }
    @GetMapping public List<CompanyResponse> list(@RequestHeader("X-Tenant-Id") UUID tenantId) { return service.list(tenantId); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public CompanyResponse create(@RequestHeader("X-Tenant-Id") UUID tenantId, @Valid @RequestBody CreateCompanyRequest request) { return service.create(tenantId, request); }
}
