package com.estatepro.dto;

import com.estatepro.entity.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;

public final class ApiDtos {
    private ApiDtos() {}
    public record RegisterRequest(@NotBlank @Size(max = 120) String fullName,
                                  @NotBlank @Email @Size(max = 190) String email,
                                  @NotBlank @Size(min = 10, max = 72) String password,
                                  @NotNull Role role) {}
    public record LoginRequest(@NotBlank @Email String email, @NotBlank String password) {}
    public record UserView(UUID id, String fullName, String email, Role role, boolean enabled) {
        public static UserView from(User u) { return new UserView(u.getId(), u.getFullName(), u.getEmail(), u.getRole(), u.isEnabled()); }
    }
    public record AuthResponse(String token, UserView user) {}
    public record PropertyRequest(@NotBlank @Size(max = 150) String title,
                                  @NotBlank @Size(max = 5000) String description,
                                  @NotBlank @Pattern(regexp = "(?i)Apartment|Independent house|Villa|Plot|Commercial office|Shop|Warehouse|Penthouse") String propertyType,
                                  @NotBlank @Pattern(regexp = "(?i)For Sale|For Rent|For Lease") String listingType,
                                  @NotBlank String city, @NotBlank String locality,
                                  @Size(max = 250) String address, @NotNull @DecimalMin("1") BigDecimal price,
                                  @Min(0) @Max(20) Integer bedrooms, @Min(0) @Max(20) Integer bathrooms,
                                  @Min(1) @Max(1000000) Integer areaSqft,
                                  @NotBlank @Size(max = 400) String imageUrl) {}
    public record PropertyView(UUID id, String title, String description, String propertyType, String listingType,
                               String city, String locality, String address, BigDecimal price, Integer bedrooms,
                               Integer bathrooms, Integer areaSqft, String imageUrl, ListingStatus status,
                               boolean featured, String ownerName, String agentName, Instant createdAt, String moderationNote) {
        public static PropertyView from(Property p) {
            return new PropertyView(p.getId(), p.getTitle(), p.getDescription(), p.getPropertyType(),
                    p.getListingType(), p.getCity(), p.getLocality(), p.getAddress(), p.getPrice(), p.getBedrooms(),
                    p.getBathrooms(), p.getAreaSqft(), p.getImageUrl(), p.getStatus(), p.isFeatured(),
                    p.getOwner() == null ? "EstatePro" : p.getOwner().getFullName(),
                    p.getAgent() == null ? "EstatePro" : p.getAgent().getFullName(), p.getCreatedAt(), p.getModerationNote());
        }
    }
    public record PageView<T>(List<T> content, int page, int size, long totalElements,
                              int totalPages, boolean first, boolean last) {
        public static <T> PageView<T> from(Page<T> result) {
            return new PageView<>(result.getContent(), result.getNumber(), result.getSize(),
                    result.getTotalElements(), result.getTotalPages(), result.isFirst(), result.isLast());
        }
    }
    public record EnquiryRequest(@NotNull UUID propertyId, @NotBlank @Size(max = 2000) String message) {}
    public record EnquiryStatusRequest(@NotBlank String status, @Size(max = 2000) String internalNote) {}
    public record EnquiryView(UUID id, UUID propertyId, String propertyTitle, String buyerName, String buyerEmail,
                              String agentName, String message, String status, String internalNote, Instant createdAt) {
        public static EnquiryView from(Enquiry e) {
            return new EnquiryView(e.getId(), e.getProperty().getId(), e.getProperty().getTitle(),
                    e.getBuyer().getFullName(), e.getBuyer().getEmail(),
                    e.getAgent() == null ? "Unassigned" : e.getAgent().getFullName(),
                    e.getMessage(), e.getStatus(), e.getInternalNote(), e.getCreatedAt());
        }
    }
    public record AppointmentRequest(@NotNull UUID propertyId, @NotNull LocalDate date, @NotNull LocalTime time) {}
    public record AppointmentStatusRequest(@NotBlank String status) {}
    public record AppointmentView(UUID id, UUID propertyId, String propertyTitle, String buyerName,
                                  String agentName, LocalDate date, LocalTime time, String status) {
        public static AppointmentView from(Appointment a) {
            return new AppointmentView(a.getId(), a.getProperty().getId(), a.getProperty().getTitle(),
                    a.getBuyer().getFullName(), a.getAgent() == null ? "Unassigned" : a.getAgent().getFullName(),
                    a.getVisitDate(), a.getVisitTime(), a.getStatus());
        }
    }
    public record BookingView(UUID id, String reference, UUID propertyId, String propertyTitle,
                              String buyerName, BigDecimal amount, String status, Instant createdAt) {
        public static BookingView from(Booking b) {
            return new BookingView(b.getId(), b.getReference(), b.getProperty().getId(),
                    b.getProperty().getTitle(), b.getBuyer().getFullName(), b.getAmount(), b.getStatus(), b.getCreatedAt());
        }
    }
    public record PaymentView(String transactionReference, String bookingReference, BigDecimal amount,
                              String status, Instant createdAt) {}
    public record ModerationRequest(@NotNull ListingStatus status, @Size(max = 500) String reason) {}
    public record UserUpdateRequest(@NotNull Role role, Boolean enabled) {}
    public record ContactRequest(@NotBlank @Size(max = 120) String name,
                                @NotBlank @Email @Size(max = 190) String email,
                                @NotBlank @Size(max = 150) String subject,
                                @NotBlank @Size(max = 3000) String message) {}
    public record ContactResponse(String message) {}
}
