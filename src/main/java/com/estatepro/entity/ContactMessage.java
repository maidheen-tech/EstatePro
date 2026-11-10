package com.estatepro.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "contact_messages")
public class ContactMessage {
    @Id @GeneratedValue(strategy = GenerationType.UUID) @JdbcTypeCode(SqlTypes.CHAR)
    private UUID id;
    @Column(nullable = false, length = 120)
    private String name;
    @Column(nullable = false, length = 190)
    private String email;
    @Column(nullable = false, length = 150)
    private String subject;
    @Column(nullable = false, length = 3000)
    private String message;
    @Column(nullable = false, length = 20)
    private String status = "NEW";
    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected ContactMessage() {}
    public ContactMessage(String name, String email, String subject, String message) {
        this.name = name; this.email = email.toLowerCase().trim(); this.subject = subject; this.message = message;
    }
}
