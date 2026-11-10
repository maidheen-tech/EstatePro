package com.estatepro.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "appointments", uniqueConstraints = @UniqueConstraint(name = "uk_agent_visit_slot", columnNames = {"agent_id", "visit_date", "visit_time"}))
public class Appointment {
    @Id @GeneratedValue(strategy = GenerationType.UUID) @JdbcTypeCode(SqlTypes.CHAR)
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "property_id")
    private Property property;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "buyer_id")
    private User buyer;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "agent_id")
    private User agent;
    @Column(name = "visit_date", nullable = false)
    private LocalDate visitDate;
    @Column(name = "visit_time", nullable = false)
    private LocalTime visitTime;
    @Column(nullable = false, length = 20)
    private String status = "REQUESTED";
    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
    protected Appointment() {}
    public Appointment(Property property, User buyer, LocalDate date, LocalTime time) {
        this.property = property; this.buyer = buyer; this.agent = property.getAgent();
        this.visitDate = date; this.visitTime = time;
    }
    public UUID getId() { return id; }
    public Property getProperty() { return property; }
    public User getBuyer() { return buyer; }
    public User getAgent() { return agent; }
    public LocalDate getVisitDate() { return visitDate; }
    public LocalTime getVisitTime() { return visitTime; }
    public String getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public void setStatus(String status) { this.status = status; }
}
