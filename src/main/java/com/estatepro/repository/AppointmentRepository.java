package com.estatepro.repository;

import com.estatepro.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {
    boolean existsByAgentIdAndVisitDateAndVisitTimeAndStatusNot(UUID agentId, LocalDate date, LocalTime time, String status);
    List<Appointment> findByBuyerIdOrderByVisitDateAscVisitTimeAsc(UUID buyerId);
    List<Appointment> findByAgentIdOrderByVisitDateAscVisitTimeAsc(UUID agentId);
    long countByStatus(String status);
}
