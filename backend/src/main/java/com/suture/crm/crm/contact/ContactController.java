package com.suture.crm.crm.contact;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/contacts")
public class ContactController {
    private final ContactService service;
    ContactController(ContactService service) { this.service = service; }

    @GetMapping
    public List<ContactResponse> list(@RequestHeader("X-Tenant-Id") UUID tenantId, @RequestParam UUID companyId) {
        return service.list(tenantId, companyId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ContactResponse create(@RequestHeader("X-Tenant-Id") UUID tenantId, @Valid @RequestBody CreateContactRequest request) {
        return service.create(tenantId, request);
    }
}
