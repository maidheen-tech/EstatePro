package com.estatepro.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "properties", indexes = {
        @Index(name = "idx_property_status_city", columnList = "status,city"),
        @Index(name = "idx_property_price", columnList = "price"),
        @Index(name = "idx_property_created", columnList = "createdAt")
})
public class Property {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID id;
    @Column(nullable = false, length = 150)
    private String title;
    @Column(nullable = false, length = 5000)
    private String description;
    @Column(nullable = false, length = 40)
    private String propertyType;
    @Column(nullable = false, length = 20)
    private String listingType;
    @Column(nullable = false, length = 80)
    private String city;
    @Column(nullable = false, length = 120)
    private String locality;
    @Column(length = 250)
    private String address;
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal price;
    private Integer bedrooms;
    private Integer bathrooms;
    private Integer areaSqft;
    @Column(nullable = false, length = 400)
    private String imageUrl;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ListingStatus status = ListingStatus.PENDING;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private User owner;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_id")
    private User agent;
    @Column(nullable = false)
    private boolean featured;
    @Column(length = 500)
    private String moderationNote;
    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected Property() {}

    public Property(String title, String description, String propertyType, String listingType, String city,
                    String locality, String address, BigDecimal price, Integer bedrooms, Integer bathrooms,
                    Integer areaSqft, String imageUrl, ListingStatus status, User owner, boolean featured) {
        this.title = title;
        this.description = description;
        this.propertyType = propertyType;
        this.listingType = listingType;
        this.city = city;
        this.locality = locality;
        this.address = address;
        this.price = price;
        this.bedrooms = bedrooms;
        this.bathrooms = bathrooms;
        this.areaSqft = areaSqft;
        this.imageUrl = imageUrl;
        this.status = status;
        this.owner = owner;
        this.featured = featured;
    }

    public UUID getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getPropertyType() { return propertyType; }
    public String getListingType() { return listingType; }
    public String getCity() { return city; }
    public String getLocality() { return locality; }
    public String getAddress() { return address; }
    public BigDecimal getPrice() { return price; }
    public Integer getBedrooms() { return bedrooms; }
    public Integer getBathrooms() { return bathrooms; }
    public Integer getAreaSqft() { return areaSqft; }
    public String getImageUrl() { return imageUrl; }
    public ListingStatus getStatus() { return status; }
    public User getOwner() { return owner; }
    public User getAgent() { return agent; }
    public boolean isFeatured() { return featured; }
    public Instant getCreatedAt() { return createdAt; }
    public String getModerationNote() { return moderationNote; }
    public void setStatus(ListingStatus status) { this.status = status; }
    public void setAgent(User agent) { this.agent = agent; }
    public void setModerationNote(String moderationNote) { this.moderationNote = moderationNote; }
    public void update(String title, String description, String propertyType, String listingType, String city,
                       String locality, String address, BigDecimal price, Integer bedrooms, Integer bathrooms,
                       Integer areaSqft, String imageUrl) {
        this.title = title; this.description = description; this.propertyType = propertyType;
        this.listingType = listingType; this.city = city; this.locality = locality; this.address = address;
        this.price = price; this.bedrooms = bedrooms; this.bathrooms = bathrooms;
        this.areaSqft = areaSqft; this.imageUrl = imageUrl;
    }
}
