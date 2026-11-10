package com.estatepro.repository;

import com.estatepro.entity.Enquiry;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface EnquiryRepository extends JpaRepository<Enquiry, UUID> {
    List<Enquiry> findByBuyerIdOrderByCreatedAtDesc(UUID buyerId);
    List<Enquiry> findByAgentIdOrderByCreatedAtDesc(UUID agentId);
    long countByStatus(String status);
    long countByAgentId(UUID agentId);
}
