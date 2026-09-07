package com.suture.crm.crm.company;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "company")
public class Company {
    @Id
    private UUID id;
    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;
    @Column(nullable = false)
    private String name;
    private String website;
    private String address;
    private String phone;
    private String whatsapp;
    private String email;
    private String industry;
    private String city;
    private String country;
    private String source;
    @Column(nullable = false)
    private String status;
    @Column(name = "owner_id")
    private UUID ownerId;
    @Column(name = "created_by_id")
    private UUID createdById;
    @Column(name = "updated_by_id")
    private UUID updatedById;
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected Company() { }

    Company(UUID tenantId, UUID ownerId, String name, String website, String address, String phone, String whatsapp, String email,
            String industry, String city, String country, String source) {
        this.id = UUID.randomUUID(); this.tenantId = tenantId; this.name = name; this.website = website;
        this.address = address; this.phone = phone; this.whatsapp = whatsapp; this.email = email;
        this.industry = industry; this.city = city; this.country = country; this.source = source; this.status = "ACTIVE";
        this.ownerId = ownerId; this.createdById = ownerId;
    }
    @PrePersist void onCreate() { createdAt = OffsetDateTime.now(); updatedAt = createdAt; }
    @PreUpdate void onUpdate() { updatedAt = OffsetDateTime.now(); }
    public UUID getId() { return id; } public UUID getTenantId() { return tenantId; } public String getName() { return name; }
    public String getWebsite() { return website; } public String getIndustry() { return industry; } public String getCity() { return city; }
    public String getCountry() { return country; } public String getSource() { return source; } public String getStatus() { return status; }
    public String getAddress() { return address; } public String getPhone() { return phone; } public String getWhatsapp() { return whatsapp; }
    public String getEmail() { return email; }
    public UUID getOwnerId() { return ownerId; }
    public UUID getCreatedById() { return createdById; }
    public UUID getUpdatedById() { return updatedById; }
}
