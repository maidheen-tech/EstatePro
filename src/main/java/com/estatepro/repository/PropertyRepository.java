package com.estatepro.repository;

import com.estatepro.entity.ListingStatus;
import com.estatepro.entity.Property;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.UUID;

public interface PropertyRepository extends JpaRepository<Property, UUID>, JpaSpecificationExecutor<Property> {
    long countByStatus(ListingStatus status);
    long countByOwnerId(UUID ownerId);
    long countByAgentId(UUID agentId);
    java.util.List<Property> findByOwnerIdOrderByCreatedAtDesc(UUID ownerId);
    java.util.List<Property> findByAgentIdOrderByCreatedAtDesc(UUID agentId);
    java.util.List<Property> findByStatusOrderByCreatedAtDesc(ListingStatus status);
}
