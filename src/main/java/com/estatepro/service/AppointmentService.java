package com.estatepro.service;

import com.estatepro.dto.ApiDtos.*;
import com.estatepro.entity.*;
import com.estatepro.exception.ResourceNotFoundException;
import com.estatepro.repository.AppointmentRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Service
public class AppointmentService {
    private final AppointmentRepository appointments;
    private final PropertyService propertyService;
    public AppointmentService(AppointmentRepository appointments, PropertyService propertyService) {
        this.appointments = appointments; this.propertyService = propertyService;
    }
    @Transactional
    public AppointmentView create(User user, AppointmentRequest request) {
        if (user.getRole() != Role.BUYER) throw new SecurityException("Only buyer accounts can request visits");
        Property property = propertyService.requirePublished(request.propertyId());
        if (request.date().isBefore(LocalDate.now())) throw new IllegalArgumentException("Visit date must be in the future");
        if (request.time().isBefore(LocalTime.of(9, 0)) || !request.time().isBefore(LocalTime.of(18, 0))
                || request.time().getMinute() % 30 != 0)
            throw new IllegalArgumentException("Choose a 30-minute slot between 09:00 and 17:30");
        if (property.getAgent() != null && appointments.existsByAgentIdAndVisitDateAndVisitTimeAndStatusNot(
                property.getAgent().getId(), request.date(), request.time(), "CANCELLED"))
            throw new IllegalArgumentException("That agent already has an appointment at this time");
        try {
            return AppointmentView.from(appointments.save(new Appointment(property, user, request.date(), request.time())));
        } catch (DataIntegrityViolationException ex) {
            throw new IllegalArgumentException("That appointment slot was just booked. Please choose another time");
        }
    }
    @Transactional(readOnly = true)
    public List<AppointmentView> list(User user) {
        return switch (user.getRole()) {
            case ADMIN -> appointments.findAll().stream().map(AppointmentView::from).toList();
            case AGENT -> appointments.findByAgentIdOrderByVisitDateAscVisitTimeAsc(user.getId()).stream().map(AppointmentView::from).toList();
            case BUYER -> appointments.findByBuyerIdOrderByVisitDateAscVisitTimeAsc(user.getId()).stream().map(AppointmentView::from).toList();
            default -> List.of();
        };
    }
    @Transactional
    public AppointmentView update(User user, UUID id, AppointmentStatusRequest request) {
        Appointment a = appointments.findById(id).orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));
        if (user.getRole() == Role.BUYER && !a.getBuyer().getId().equals(user.getId()))
            throw new SecurityException("You do not have access to this appointment");
        if (user.getRole() == Role.AGENT && (a.getAgent() == null || !a.getAgent().getId().equals(user.getId())))
            throw new SecurityException("You do not have access to this appointment");
        if (user.getRole() != Role.BUYER && user.getRole() != Role.AGENT && user.getRole() != Role.ADMIN)
            throw new SecurityException("Your account does not have permission for this action");
        String status = request.status().toUpperCase();
        List<String> allowed = user.getRole() == Role.BUYER ? List.of("CANCELLED")
                : List.of("CONFIRMED", "CANCELLED", "COMPLETED");
        if (!allowed.contains(status)) throw new IllegalArgumentException("Unsupported appointment status");
        a.setStatus(status);
        return AppointmentView.from(appointments.save(a));
    }
}
