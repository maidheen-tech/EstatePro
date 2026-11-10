package com.estatepro.service;

import com.estatepro.entity.*;
import com.estatepro.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class DashboardService {
    private final UserRepository users;
    private final PropertyRepository properties;
    private final EnquiryRepository enquiries;
    private final AppointmentRepository appointments;
    private final BookingRepository bookings;
    private final FavoriteRepository favorites;
    private final PropertyService propertyService;
    private final EnquiryService enquiryService;
    private final AppointmentService appointmentService;
    private final BookingService bookingService;
    private final FavoriteService favoriteService;
    public DashboardService(UserRepository users, PropertyRepository properties, EnquiryRepository enquiries,
                            AppointmentRepository appointments, BookingRepository bookings, FavoriteRepository favorites,
                            PropertyService propertyService, EnquiryService enquiryService,
                            AppointmentService appointmentService, BookingService bookingService, FavoriteService favoriteService) {
        this.users = users; this.properties = properties; this.enquiries = enquiries; this.appointments = appointments;
        this.bookings = bookings; this.favorites = favorites; this.propertyService = propertyService;
        this.enquiryService = enquiryService; this.appointmentService = appointmentService;
        this.bookingService = bookingService; this.favoriteService = favoriteService;
    }
    @Transactional(readOnly = true)
    public Map<String, Object> forUser(User user) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("role", user.getRole());
        result.put("name", user.getFullName());
        switch (user.getRole()) {
            case ADMIN -> {
                result.put("stats", Map.of(
                        "users", users.count(), "buyers", users.countByRole(Role.BUYER),
                        "sellers", users.countByRole(Role.SELLER), "agents", users.countByRole(Role.AGENT),
                        "properties", properties.count(), "pending", properties.countByStatus(ListingStatus.PENDING),
                        "published", properties.countByStatus(ListingStatus.PUBLISHED),
                        "enquiries", enquiries.count(), "appointments", appointments.count(),
                        "bookings", bookings.count()));
                result.put("properties", properties.findAll().stream().map(com.estatepro.dto.ApiDtos.PropertyView::from).toList());
                result.put("enquiries", enquiries.findAll().stream().map(com.estatepro.dto.ApiDtos.EnquiryView::from).toList());
                result.put("appointments", appointments.findAll().stream().map(com.estatepro.dto.ApiDtos.AppointmentView::from).toList());
            }
            case AGENT -> {
                result.put("stats", Map.of("properties", properties.countByAgentId(user.getId()),
                        "enquiries", enquiries.countByAgentId(user.getId()),
                        "newLeads", enquiries.findByAgentIdOrderByCreatedAtDesc(user.getId()).stream().filter(e -> "NEW".equals(e.getStatus())).count(),
                        "upcomingVisits", appointments.findByAgentIdOrderByVisitDateAscVisitTimeAsc(user.getId()).stream().filter(a -> "REQUESTED".equals(a.getStatus()) || "CONFIRMED".equals(a.getStatus())).count()));
                result.put("properties", propertyService.managed(user));
                result.put("enquiries", enquiryService.list(user));
                result.put("appointments", appointmentService.list(user));
                result.put("bookings", bookingService.list(user));
            }
            case SELLER -> {
                var mine = propertyService.managed(user);
                result.put("stats", Map.of("properties", mine.size(),
                        "pending", mine.stream().filter(p -> p.status() == ListingStatus.PENDING).count(),
                        "published", mine.stream().filter(p -> p.status() == ListingStatus.PUBLISHED).count(),
                        "rejected", mine.stream().filter(p -> p.status() == ListingStatus.REJECTED).count()));
                result.put("properties", mine);
            }
            case BUYER -> {
                result.put("stats", Map.of("favorites", favorites.findByUserIdOrderByCreatedAtDesc(user.getId()).size(),
                        "enquiries", enquiries.findByBuyerIdOrderByCreatedAtDesc(user.getId()).size(),
                        "appointments", appointments.findByBuyerIdOrderByVisitDateAscVisitTimeAsc(user.getId()).size(),
                        "bookings", bookings.findByBuyerIdOrderByCreatedAtDesc(user.getId()).size()));
                result.put("favorites", favoriteService.list(user));
                result.put("enquiries", enquiryService.list(user));
                result.put("appointments", appointmentService.list(user));
                result.put("bookings", bookingService.list(user));
            }
        }
        return result;
    }
}
