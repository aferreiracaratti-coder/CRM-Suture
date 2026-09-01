package com.suture.crm.crm.company;

import java.util.UUID;

public record CompanyResponse(UUID id, String name, String website, String address, String phone, String whatsapp, String email,
                              String industry, String city, String country, String source, String status) {
    static CompanyResponse from(Company company) {
        return new CompanyResponse(company.getId(), company.getName(), company.getWebsite(), company.getAddress(), company.getPhone(),
                company.getWhatsapp(), company.getEmail(), company.getIndustry(), company.getCity(), company.getCountry(),
                company.getSource(), company.getStatus());
    }
}
