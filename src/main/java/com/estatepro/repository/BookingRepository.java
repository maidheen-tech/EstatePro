package com.estatepro.repository;

import com.estatepro.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface BookingRepository extends JpaRepository<Booking, UUID> {
    List<Booking> findByBuyerIdOrderByCreatedAtDesc(UUID buyerId);
    List<Booking> findByAgentIdOrderByCreatedAtDesc(UUID agentId);
    long countByStatus(String status);
}
