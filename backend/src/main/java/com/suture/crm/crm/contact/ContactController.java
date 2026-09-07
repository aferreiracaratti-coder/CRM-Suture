package com.suture.crm.crm.contact;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import com.suture.crm.auth.CrmUserPrincipal;

@RestController
@RequestMapping("/api/contacts")
public class ContactController {
    private final ContactService service;
    ContactController(ContactService service) { this.service = service; }

    @GetMapping
    public List<ContactResponse> list(@AuthenticationPrincipal CrmUserPrincipal principal, @RequestParam UUID companyId) {
        return service.list(principal.tenantId(), companyId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ContactResponse create(@AuthenticationPrincipal CrmUserPrincipal principal, @Valid @RequestBody CreateContactRequest request) {
        return service.create(principal.tenantId(), principal.id(), request);
    }
}
