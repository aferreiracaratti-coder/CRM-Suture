package com.suture.crm.crm.company;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import com.suture.crm.auth.CrmUserPrincipal;

@RestController
@RequestMapping("/api/companies")
public class CompanyController {
    private final CompanyService service;
    CompanyController(CompanyService service) { this.service = service; }
    @GetMapping public List<CompanyResponse> list(@AuthenticationPrincipal CrmUserPrincipal principal) { return service.list(principal.tenantId()); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public CompanyResponse create(@AuthenticationPrincipal CrmUserPrincipal principal, @Valid @RequestBody CreateCompanyRequest request) { return service.create(principal.tenantId(), principal.id(), request); }
    @DeleteMapping("/{id}")
    public void delete(@AuthenticationPrincipal CrmUserPrincipal principal, @PathVariable UUID id) { service.delete(principal.tenantId(), principal.id(), id); }
}
