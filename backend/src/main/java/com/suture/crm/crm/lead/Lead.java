package com.suture.crm.crm.lead;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "lead")
public class Lead {
    @Id private UUID id;
    @Column(name = "tenant_id", nullable = false) private UUID tenantId;
    @Column(name = "company_id", nullable = false) private UUID companyId;
    @Column(name = "contact_id") private UUID contactId;
    private String source;
    private String temperature;
    private String status;
    @Column(name = "next_contact_at") private OffsetDateTime nextContactAt;
    private Integer score;
    private String summary;
    private String priority;
    @Column(name = "data_quality") private String dataQuality;

    protected Lead() { }
    public UUID getId() { return id; }
    public UUID getCompanyId() { return companyId; }
    public UUID getContactId() { return contactId; }
    public String getSource() { return source; }
    public String getTemperature() { return temperature; }
    public String getStatus() { return status; }
    public OffsetDateTime getNextContactAt() { return nextContactAt; }
    public Integer getScore() { return score; }
    public String getSummary() { return summary; }
    public String getPriority() { return priority; }
    public String getDataQuality() { return dataQuality; }
}
