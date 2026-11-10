package com.estatepro;

import com.estatepro.entity.ListingStatus;
import com.estatepro.entity.Property;
import com.estatepro.repository.PropertyRepository;
import com.estatepro.service.RecommendationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceTest {
    @Mock PropertyRepository properties;
    @InjectMocks RecommendationService recommendations;

    @Test
    void scoresExactPreferenceMatchesAndRanksThemFirst() {
        Property match = property("Chennai", "Villa", "8000000", 3);
        Property partial = property("Mumbai", "Apartment", "25000000", 2);
        when(properties.findByStatusOrderByCreatedAtDesc(ListingStatus.PUBLISHED)).thenReturn(List.of(partial, match));

        var result = recommendations.recommend("Chennai", "Villa", new BigDecimal("10000000"), 3);

        assertEquals(2, result.size());
        assertEquals(100, result.get(0).get("match"));
        assertEquals(match.getTitle(), ((com.estatepro.dto.ApiDtos.PropertyView) result.get(0).get("property")).title());
    }

    private Property property(String city, String type, String price, int beds) {
        return new Property(type + " in " + city, "A considered home.", type, "For Sale", city,
                "Central", "Central, " + city, new BigDecimal(price), beds, 2, 1400,
                "/assets/images/properties/villa-01.jpg", ListingStatus.PUBLISHED, null, false);
    }
}
