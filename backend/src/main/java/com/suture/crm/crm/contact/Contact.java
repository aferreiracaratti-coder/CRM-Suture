package com.suture.crm.crm.contact;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "contact")
public class Contact {
    @Id private UUID id;
    @Column(name = "tenant_id", nullable = false) private UUID tenantId;
    @Column(name = "company_id", nullable = false) private UUID companyId;
    @Column(name = "first_name", nullable = false) private String firstName;
    @Column(name = "last_name") private String lastName;
    private String role;
    private String email;
    private String phone;
    private String instagram;
    private String linkedin;
    private String notes;
    @Column(name = "created_by_id")
    private UUID createdById;
    @Column(name = "updated_by_id")
    private UUID updatedById;
    @Column(name = "created_at", nullable = false) private OffsetDateTime createdAt;
    @Column(name = "updated_at", nullable = false) private OffsetDateTime updatedAt;

    protected Contact() { }

    Contact(UUID tenantId, UUID actorId, UUID companyId, CreateContactRequest request) {
        this.id = UUID.randomUUID();
        this.tenantId = tenantId;
        this.companyId = companyId;
        this.createdById = actorId;
        this.firstName = request.firstName();
        this.lastName = request.lastName();
        this.role = request.role();
        this.email = request.email();
        this.phone = request.phone();
        this.instagram = request.instagram();
        this.linkedin = request.linkedin();
        this.notes = request.notes();
    }

    @PrePersist void onCreate() { createdAt = OffsetDateTime.now(); updatedAt = createdAt; }
    @PreUpdate void onUpdate() { updatedAt = OffsetDateTime.now(); }

    public UUID getId() { return id; }
    public UUID getTenantId() { return tenantId; }
    public UUID getCompanyId() { return companyId; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getRole() { return role; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getInstagram() { return instagram; }
    public String getLinkedin() { return linkedin; }
    public String getNotes() { return notes; }
    public UUID getCreatedById() { return createdById; }
    public UUID getUpdatedById() { return updatedById; }
}
