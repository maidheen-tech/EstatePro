package com.estatepro.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "enquiries", indexes = @Index(name = "idx_enquiry_agent_status", columnList = "agent_id,status"))
public class Enquiry {
    @Id @GeneratedValue(strategy = GenerationType.UUID) @JdbcTypeCode(SqlTypes.CHAR)
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "property_id")
    private Property property;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "buyer_id")
    private User buyer;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "agent_id")
    private User agent;
    @Column(nullable = false, length = 2000)
    private String message;
    @Column(nullable = false, length = 30)
    private String status = "NEW";
    @Column(length = 2000)
    private String internalNote;
    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
    protected Enquiry() {}
    public Enquiry(Property property, User buyer, String message) {
        this.property = property; this.buyer = buyer; this.agent = property.getAgent(); this.message = message;
    }
    public UUID getId() { return id; }
    public Property getProperty() { return property; }
    public User getBuyer() { return buyer; }
    public User getAgent() { return agent; }
    public String getMessage() { return message; }
    public String getStatus() { return status; }
    public String getInternalNote() { return internalNote; }
    public Instant getCreatedAt() { return createdAt; }
    public void setStatus(String status) { this.status = status; }
    public void setInternalNote(String internalNote) { this.internalNote = internalNote; }
}
