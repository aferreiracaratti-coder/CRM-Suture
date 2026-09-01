package com.suture.crm.crm.contact;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record CreateContactRequest(
        UUID companyId,
        @NotBlank @Size(max = 120) String firstName,
        @Size(max = 120) String lastName,
        @Size(max = 160) String role,
        @Email @Size(max = 250) String email,
        @Size(max = 80) String phone,
        @Size(max = 160) String instagram,
        @Size(max = 300) String linkedin,
        String notes) { }
