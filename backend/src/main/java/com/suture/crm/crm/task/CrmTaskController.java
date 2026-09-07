package com.suture.crm.crm.task;

import com.suture.crm.auth.CrmUserPrincipal;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/crm/tasks")
public class CrmTaskController {
    private final CrmTaskService service;
    CrmTaskController(CrmTaskService service) { this.service = service; }

    @GetMapping
    public List<CrmTaskService.CrmTaskResponse> list(@AuthenticationPrincipal CrmUserPrincipal principal) {
        return service.list(principal.tenantId());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CrmTaskService.CrmTaskResponse create(@AuthenticationPrincipal CrmUserPrincipal principal,
                                                 @Valid @RequestBody CrmTaskService.CreateCrmTaskRequest request) {
        return service.create(principal.tenantId(), principal.id(), request);
    }

    @PatchMapping("/{id}")
    public CrmTaskService.CrmTaskResponse toggle(@AuthenticationPrincipal CrmUserPrincipal principal,
                                                 @PathVariable UUID id,
                                                 @RequestBody ToggleCrmTaskRequest request) {
        return service.toggle(principal.tenantId(), principal.id(), id, request.done());
    }

    @DeleteMapping("/{id}")
    public void delete(@AuthenticationPrincipal CrmUserPrincipal principal, @PathVariable UUID id) {
        service.delete(principal.tenantId(), principal.id(), id);
    }

    public record ToggleCrmTaskRequest(boolean done) { }
}
