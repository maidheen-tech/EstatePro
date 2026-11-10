package com.estatepro.service;

import com.estatepro.dto.ApiDtos.PropertyView;
import com.estatepro.entity.ListingStatus;
import com.estatepro.entity.Property;
import com.estatepro.repository.PropertyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Service
public class RecommendationService {
    private final PropertyRepository properties;
    public RecommendationService(PropertyRepository properties) { this.properties = properties; }
    @Transactional(readOnly = true)
    public java.util.List<Map<String, Object>> recommend(String city, String type, BigDecimal maxPrice, Integer bedrooms) {
        return properties.findByStatusOrderByCreatedAtDesc(ListingStatus.PUBLISHED).stream()
                .map(p -> scored(p, city, type, maxPrice, bedrooms))
                .filter(row -> (Integer) row.get("match") >= 35)
                .sorted((a, b) -> Integer.compare((Integer) b.get("match"), (Integer) a.get("match")))
                .limit(6).toList();
    }
    private Map<String, Object> scored(Property p, String city, String type, BigDecimal maxPrice, Integer bedrooms) {
        int score = 35;
        if (city != null && !city.isBlank() && p.getCity().equalsIgnoreCase(city)) score += 30;
        if (type != null && !type.isBlank() && p.getPropertyType().equalsIgnoreCase(type)) score += 20;
        if (maxPrice != null && p.getPrice().compareTo(maxPrice) <= 0) score += 10;
        if (bedrooms != null && p.getBedrooms() != null && p.getBedrooms() >= bedrooms) score += 5;
        return Map.of("property", PropertyView.from(p), "match", score,
                "reason", score >= 80 ? "Strong match for your selected preferences"
                        : score >= 55 ? "Matches several of your preferences" : "A great place to explore");
    }
}
