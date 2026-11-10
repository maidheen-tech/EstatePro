package com.estatepro.repository;

import com.estatepro.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    java.util.List<Payment> findByBookingBuyerIdOrderByCreatedAtDesc(UUID buyerId);
}
