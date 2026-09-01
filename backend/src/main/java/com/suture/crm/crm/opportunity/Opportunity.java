package com.suture.crm.crm.opportunity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "opportunity")
public class Opportunity {
    @Id private UUID id;
    @Column(name = "tenant_id", nullable = false) private UUID tenantId;
    @Column(name = "company_id", nullable = false) private UUID companyId;
    @Column(name = "contact_id") private UUID contactId;
    @Column(nullable = false) private String name;
    @Column(nullable = false) private String stage;
    @Column(name = "estimated_value") private BigDecimal estimatedValue;
    @Column(nullable = false) private String currency;
    private Short probability;
    @Column(name = "expected_close_date") private LocalDate expectedCloseDate;
    @Column(name = "next_action") private String nextAction;
    @Column(name = "next_action_date") private LocalDate nextActionDate;
    @Column(name = "lost_reason") private String lostReason;
    @Column(name = "created_at", nullable = false) private OffsetDateTime createdAt;
    @Column(name = "updated_at", nullable = false) private OffsetDateTime updatedAt;

    protected Opportunity() { }

    Opportunity(UUID tenantId, CreateOpportunityRequest request) {
        this.id = UUID.randomUUID(); this.tenantId = tenantId; this.companyId = request.companyId(); this.contactId = request.contactId();
        this.name = request.name(); this.stage = request.stage() == null ? "NEW" : request.stage(); this.estimatedValue = request.estimatedValue();
        this.currency = request.currency() == null ? "UYU" : request.currency(); this.probability = request.probability();
        this.expectedCloseDate = request.expectedCloseDate(); this.nextAction = request.nextAction(); this.nextActionDate = request.nextActionDate();
    }

    void moveTo(String stage, String lostReason) { this.stage = stage; this.lostReason = lostReason; }
    @PrePersist void onCreate() { createdAt = OffsetDateTime.now(); updatedAt = createdAt; }
    @PreUpdate void onUpdate() { updatedAt = OffsetDateTime.now(); }

    public UUID getId() { return id; }
    public UUID getTenantId() { return tenantId; }
    public UUID getCompanyId() { return companyId; }
    public UUID getContactId() { return contactId; }
    public String getName() { return name; }
    public String getStage() { return stage; }
    public BigDecimal getEstimatedValue() { return estimatedValue; }
    public String getCurrency() { return currency; }
    public Short getProbability() { return probability; }
    public LocalDate getExpectedCloseDate() { return expectedCloseDate; }
    public String getNextAction() { return nextAction; }
    public LocalDate getNextActionDate() { return nextActionDate; }
    public String getLostReason() { return lostReason; }
}
