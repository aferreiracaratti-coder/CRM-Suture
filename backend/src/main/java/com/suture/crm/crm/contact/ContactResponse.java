package com.suture.crm.crm.contact;

import java.util.UUID;

public record ContactResponse(UUID id, UUID companyId, String firstName, String lastName, String role,
                              String email, String phone, String instagram, String linkedin, String notes) {
    static ContactResponse from(Contact contact) {
        return new ContactResponse(contact.getId(), contact.getCompanyId(), contact.getFirstName(), contact.getLastName(),
                contact.getRole(), contact.getEmail(), contact.getPhone(), contact.getInstagram(), contact.getLinkedin(), contact.getNotes());
    }
}
