package com.estatepro.controller;

import com.estatepro.dto.ApiDtos.*;
import com.estatepro.service.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class MarketplaceController {
    private final CurrentUserService currentUser;
    private final FavoriteService favorites;
    private final EnquiryService enquiries;
    private final AppointmentService appointments;
    private final BookingService bookings;
    private final PaymentService payments;
    private final RecommendationService recommendations;
    private final DashboardService dashboards;
    public MarketplaceController(CurrentUserService currentUser, FavoriteService favorites, EnquiryService enquiries,
                                 AppointmentService appointments, BookingService bookings, PaymentService payments,
                                 RecommendationService recommendations, DashboardService dashboards) {
        this.currentUser = currentUser; this.favorites = favorites; this.enquiries = enquiries;
        this.appointments = appointments; this.bookings = bookings; this.payments = payments;
        this.recommendations = recommendations; this.dashboards = dashboards;
    }
    @GetMapping("/dashboard") public Map<String, Object> dashboard(Authentication auth) {
        return dashboards.forUser(currentUser.require(auth));
    }
    @GetMapping("/recommendations")
    public List<Map<String, Object>> recommendations(Authentication auth, @RequestParam(required = false) String city,
            @RequestParam(required = false) String type, @RequestParam(required = false) java.math.BigDecimal maxPrice,
            @RequestParam(required = false) Integer bedrooms) {
        currentUser.require(auth);
        return recommendations.recommend(city, type, maxPrice, bedrooms);
    }
    @GetMapping("/favorites") public List<PropertyView> favoriteList(Authentication auth) {
        return favorites.list(currentUser.require(auth));
    }
    @PostMapping("/favorites/{propertyId}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void addFavorite(Authentication auth, @PathVariable UUID propertyId) {
        favorites.add(currentUser.require(auth), propertyId);
    }
    @DeleteMapping("/favorites/{propertyId}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeFavorite(Authentication auth, @PathVariable UUID propertyId) {
        favorites.remove(currentUser.require(auth), propertyId);
    }
    @GetMapping("/enquiries") public List<EnquiryView> enquiryList(Authentication auth) {
        return enquiries.list(currentUser.require(auth));
    }
    @PostMapping("/enquiries") @ResponseStatus(HttpStatus.CREATED)
    public EnquiryView createEnquiry(Authentication auth, @Valid @RequestBody EnquiryRequest body) {
        return enquiries.create(currentUser.require(auth), body);
    }
    @PatchMapping("/enquiries/{id}") public EnquiryView updateEnquiry(Authentication auth, @PathVariable UUID id,
                                                                       @Valid @RequestBody EnquiryStatusRequest body) {
        return enquiries.update(currentUser.require(auth), id, body);
    }
    @GetMapping("/appointments") public List<AppointmentView> appointmentList(Authentication auth) {
        return appointments.list(currentUser.require(auth));
    }
    @PostMapping("/appointments") @ResponseStatus(HttpStatus.CREATED)
    public AppointmentView createAppointment(Authentication auth, @Valid @RequestBody AppointmentRequest body) {
        return appointments.create(currentUser.require(auth), body);
    }
    @PatchMapping("/appointments/{id}") public AppointmentView updateAppointment(Authentication auth, @PathVariable UUID id,
            @Valid @RequestBody AppointmentStatusRequest body) {
        return appointments.update(currentUser.require(auth), id, body);
    }
    @GetMapping("/bookings") public List<BookingView> bookingList(Authentication auth) {
        return bookings.list(currentUser.require(auth));
    }
    @PostMapping("/bookings/{propertyId}") @ResponseStatus(HttpStatus.CREATED)
    public BookingView createBooking(Authentication auth, @PathVariable UUID propertyId) {
        return bookings.create(currentUser.require(auth), propertyId);
    }
    @PatchMapping("/bookings/{id}") public BookingView updateBooking(Authentication auth, @PathVariable UUID id,
                                                                       @RequestBody Map<String, String> body) {
        if (body.get("status") == null) throw new IllegalArgumentException("status is required");
        return bookings.update(currentUser.require(auth), id, body.get("status"));
    }
    @GetMapping("/payments") public List<PaymentView> paymentList(Authentication auth) {
        return payments.list(currentUser.require(auth));
    }
    @PostMapping("/payments/{bookingId}/demo") @ResponseStatus(HttpStatus.CREATED)
    public PaymentView demoPayment(Authentication auth, @PathVariable UUID bookingId) {
        return payments.simulate(currentUser.require(auth), bookingId);
    }
}
