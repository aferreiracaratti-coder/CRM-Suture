package com.suture.crm.crm.company;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCompanyRequest(
        @NotBlank @Size(max = 200) String name,
        @Size(max = 300) String website,
        String address,
        @Size(max = 80) String phone,
        @Size(max = 80) String whatsapp,
        @jakarta.validation.constraints.Email @Size(max = 250) String email,
        @Size(max = 120) String industry,
        @Size(max = 120) String city,
        @Size(max = 120) String country,
        @Size(max = 80) String source) { }
