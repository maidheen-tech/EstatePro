package com.estatepro.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "bookings", indexes = @Index(name = "idx_booking_buyer_date", columnList = "buyer_id,createdAt"))
public class Booking {
    @Id @GeneratedValue(strategy = GenerationType.UUID) @JdbcTypeCode(SqlTypes.CHAR)
    private UUID id;
    @Column(nullable = false, unique = true, length = 24)
    private String reference;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "property_id")
    private Property property;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "buyer_id")
    private User buyer;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "agent_id")
    private User agent;
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal amount;
    @Column(nullable = false, length = 20)
    private String status = "REQUESTED";
    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
    protected Booking() {}
    public Booking(String reference, Property property, User buyer) {
        this.reference = reference; this.property = property; this.buyer = buyer;
        this.agent = property.getAgent(); this.amount = property.getPrice();
    }
    public UUID getId() { return id; }
    public String getReference() { return reference; }
    public Property getProperty() { return property; }
    public User getBuyer() { return buyer; }
    public User getAgent() { return agent; }
    public BigDecimal getAmount() { return amount; }
    public String getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public void setStatus(String status) { this.status = status; }
}
