package com.estatepro.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users", indexes = @Index(name = "idx_user_email", columnList = "email", unique = true))
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID id;
    @Column(nullable = false, unique = true, length = 190)
    private String email;
    @Column(nullable = false)
    private String passwordHash;
    @Column(nullable = false, length = 120)
    private String fullName;
    @Column(length = 30)
    private String phone;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;
    @Column(nullable = false)
    private boolean enabled = true;
    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected User() {}

    public User(String email, String passwordHash, String fullName, Role role) {
        this.email = email.toLowerCase().trim();
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.role = role;
    }

    public UUID getId() { return id; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public String getFullName() { return fullName; }
    public String getPhone() { return phone; }
    public Role getRole() { return role; }
    public boolean isEnabled() { return enabled; }
    public Instant getCreatedAt() { return createdAt; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public void setRole(Role role) { this.role = role; }
}
