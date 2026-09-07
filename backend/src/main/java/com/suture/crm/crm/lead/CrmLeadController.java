package com.suture.crm.crm.lead;

import com.suture.crm.auth.CrmUserPrincipal;
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

@RestController
@RequestMapping("/api/crm/leads")
public class CrmLeadController {
    private final CrmLeadService service;
    CrmLeadController(CrmLeadService service) { this.service = service; }

    @GetMapping
    public List<CrmLeadService.CrmLeadResponse> list(@AuthenticationPrincipal CrmUserPrincipal principal) {
        return service.list(principal.tenantId());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CrmLeadService.CrmLeadResponse create(@AuthenticationPrincipal CrmUserPrincipal principal,
                                                 @Valid @RequestBody CrmLeadService.CreateCrmLeadRequest request) {
        return service.create(principal.tenantId(), principal.id(), request);
    }

    @DeleteMapping("/{id}")
    public void delete(@AuthenticationPrincipal CrmUserPrincipal principal, @PathVariable UUID id) {
        service.delete(principal.tenantId(), principal.id(), id);
    }
}
