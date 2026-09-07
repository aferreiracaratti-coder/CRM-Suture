package com.suture.crm.syna;

import jakarta.servlet.http.HttpServletRequest;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {
    private final SynaReadService service;
    private final SynaTaskWriteService writes;
    TaskController(SynaReadService service, SynaTaskWriteService writes) { this.service = service; this.writes = writes; }
    @GetMapping public SynaReadService.SynaPage<SynaReadService.SynaTask> list(HttpServletRequest request, @RequestParam(required = false) String query, @RequestParam(required = false) String status, @RequestParam(required = false) String assigneeId, @RequestParam(required = false) OffsetDateTime dueBefore, @RequestParam(defaultValue = "20") int limit) { return service.tasks(SynaConnectionContext.require(request).tenantId(), query, status, assigneeId, dueBefore, limit); }
    @GetMapping("/{id}") public SynaReadService.SynaTask get(HttpServletRequest request, @PathVariable UUID id) { return service.task(SynaConnectionContext.require(request).tenantId(), id); }
    @PostMapping public ResponseEntity<SynaTaskWriteService.SynaTaskWriteResult> create(HttpServletRequest request,
            @RequestHeader("Idempotency-Key") String idempotencyKey, @Valid @RequestBody SynaTaskWriteService.CreateSynaTaskRequest body) {
        SynaConnectionContext context = SynaConnectionContext.require(request);
        SynaTaskWriteService.SynaTaskWriteResult result = writes.create(context, idempotencyKey, body);
        return ResponseEntity.status(result.created() ? 201 : 200).body(result);
    }
}
