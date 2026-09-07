package com.suture.crm.auth;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/users")
@PreAuthorize("hasAuthority('CRM_ADMIN')")
public class UserController {
    private static final Set<String> ALLOWED_ROLES = Set.of(
            "CRM_ADMIN", "CRM_MANAGER", "CRM_SALES", "CRM_VIEWER", "SYNA_SALES");

    private final CrmUserRepository users;
    private final PasswordEncoder passwords;

    UserController(CrmUserRepository users, PasswordEncoder passwords) {
        this.users = users;
        this.passwords = passwords;
    }

    @GetMapping
    public List<UserResponse> list(@AuthenticationPrincipal CrmUserPrincipal principal) {
        return users.findByTenantIdOrderByDisplayNameAsc(principal.tenantId()).stream()
                .map(this::response)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse create(@AuthenticationPrincipal CrmUserPrincipal principal,
                               @Valid @RequestBody CreateUserRequest request) {
        String email = request.email().trim().toLowerCase();
        if (users.existsByTenantIdAndEmailIgnoreCase(principal.tenantId(), email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un usuario con ese correo");
        }
        String roles = normalizeRoles(request.roles());
        CrmUser user = users.save(new CrmUser(
                UUID.randomUUID(), principal.tenantId(), email, request.name().trim(),
                passwords.encode(request.password()), roles, true));
        return response(user);
    }

    @PatchMapping("/{id}")
    public UserResponse update(@AuthenticationPrincipal CrmUserPrincipal principal,
                               @PathVariable UUID id,
                               @Valid @RequestBody UpdateUserRequest request) {
        CrmUser existing = users.findByIdAndTenantId(id, principal.tenantId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
        CrmUser updated = new CrmUser(
                existing.getId(), existing.getTenantId(), existing.getEmail(),
                request.name() == null || request.name().isBlank() ? existing.getDisplayName() : request.name().trim(),
                existing.getPasswordHash(),
                request.roles() == null || request.roles().isEmpty() ? String.join(",", existing.roleList()) : normalizeRoles(request.roles()),
                request.active() == null ? existing.isActive() : request.active());
        return response(users.save(updated));
    }

    private String normalizeRoles(List<String> roles) {
        if (roles == null || roles.isEmpty()) return "CRM_SALES,SYNA_SALES";
        List<String> normalized = roles.stream().map(String::trim).map(String::toUpperCase).distinct().toList();
        if (normalized.stream().anyMatch(role -> !ALLOWED_ROLES.contains(role))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rol inválido");
        }
        return String.join(",", normalized);
    }

    private UserResponse response(CrmUser user) {
        return new UserResponse(user.getId().toString(), user.getEmail(), user.getDisplayName(), user.roleList(), user.isActive());
    }

    public record CreateUserRequest(
            @Email @NotBlank @Size(max = 320) String email,
            @NotBlank @Size(max = 160) String name,
            @NotBlank @Size(min = 8, max = 100) String password,
            List<String> roles) { }

    public record UpdateUserRequest(
            @Size(max = 160) String name,
            List<String> roles,
            Boolean active) { }

    public record UserResponse(String id, String email, String name, List<String> roles, boolean active) { }
}
