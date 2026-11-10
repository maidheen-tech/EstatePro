package com.estatepro.repository;

import com.estatepro.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FavoriteRepository extends JpaRepository<Favorite, UUID> {
    List<Favorite> findByUserIdOrderByCreatedAtDesc(UUID userId);
    Optional<Favorite> findByUserIdAndPropertyId(UUID userId, UUID propertyId);
}
