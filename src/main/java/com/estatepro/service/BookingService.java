package com.estatepro.service;

import com.estatepro.dto.ApiDtos.BookingView;
import com.estatepro.entity.*;
import com.estatepro.exception.ResourceNotFoundException;
import com.estatepro.repository.BookingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

@Service
public class BookingService {
    private final BookingRepository bookings;
    private final PropertyService propertyService;
    public BookingService(BookingRepository bookings, PropertyService propertyService) {
        this.bookings = bookings; this.propertyService = propertyService;
    }
    @Transactional
    public BookingView create(User user, UUID propertyId) {
        if (user.getRole() != Role.BUYER) throw new SecurityException("Only buyer accounts can request bookings");
        var property = propertyService.requirePublished(propertyId);
        String reference = "EP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return BookingView.from(bookings.save(new Booking(reference, property, user)));
    }
    @Transactional(readOnly = true)
    public List<BookingView> list(User user) {
        List<Booking> rows = switch (user.getRole()) {
            case ADMIN -> bookings.findAll();
            case AGENT -> bookings.findByAgentIdOrderByCreatedAtDesc(user.getId());
            case BUYER -> bookings.findByBuyerIdOrderByCreatedAtDesc(user.getId());
            default -> List.of();
        };
        return rows.stream().map(BookingView::from).toList();
    }
    @Transactional
    public BookingView update(User user, UUID id, String status) {
        Booking b = bookings.findById(id).orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
        if (user.getRole() == Role.BUYER && !b.getBuyer().getId().equals(user.getId()))
            throw new SecurityException("You do not have access to this booking");
        if (user.getRole() == Role.AGENT && (b.getAgent() == null || !b.getAgent().getId().equals(user.getId())))
            throw new SecurityException("You do not have access to this booking");
        if (user.getRole() != Role.BUYER && user.getRole() != Role.AGENT && user.getRole() != Role.ADMIN)
            throw new SecurityException("Your account does not have permission for this action");
        String value = status.toUpperCase();
        List<String> allowed = user.getRole() == Role.BUYER ? List.of("CANCELLED")
                : List.of("CONFIRMED", "CANCELLED", "COMPLETED");
        if (!allowed.contains(value)) throw new IllegalArgumentException("Unsupported booking status");
        b.setStatus(value);
        return BookingView.from(bookings.save(b));
    }
    public Booking requireOwnedForPayment(UUID id, User buyer) {
        Booking b = bookings.findById(id).orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
        if (!b.getBuyer().getId().equals(buyer.getId())) throw new SecurityException("You do not have access to this booking");
        if (!"CONFIRMED".equals(b.getStatus())) throw new IllegalArgumentException("Only confirmed bookings can be paid");
        return b;
    }
}
