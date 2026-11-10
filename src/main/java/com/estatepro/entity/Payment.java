package com.estatepro.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payments")
public class Payment {
    @Id @GeneratedValue(strategy = GenerationType.UUID) @JdbcTypeCode(SqlTypes.CHAR)
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "booking_id")
    private Booking booking;
    @Column(nullable = false, unique = true, length = 32)
    private String transactionReference;
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal amount;
    @Column(nullable = false, length = 20)
    private String status = "DEMO_SUCCESS";
    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
    protected Payment() {}
    public Payment(Booking booking, String reference, BigDecimal amount) {
        this.booking = booking; this.transactionReference = reference; this.amount = amount;
    }
    public UUID getId() { return id; }
    public Booking getBooking() { return booking; }
    public String getTransactionReference() { return transactionReference; }
    public BigDecimal getAmount() { return amount; }
    public String getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
}
