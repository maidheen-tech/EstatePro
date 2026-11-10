package com.estatepro.controller;

import com.estatepro.dto.ApiDtos.*;
import com.estatepro.entity.ListingStatus;
import com.estatepro.service.CurrentUserService;
import com.estatepro.service.PropertyService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/properties")
public class PropertyController {
    private final PropertyService properties;
    private final CurrentUserService currentUser;
    public PropertyController(PropertyService properties, CurrentUserService currentUser) {
        this.properties = properties; this.currentUser = currentUser;
    }
    @GetMapping
    public PageView<PropertyView> search(
            @RequestParam(required = false) String q, @RequestParam(required = false) String city,
            @RequestParam(required = false) String type, @RequestParam(required = false) String listing,
            @RequestParam(required = false) BigDecimal minPrice, @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Integer bedrooms, @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size, @RequestParam(defaultValue = "latest") String sort) {
        if (page < 0 || size < 1 || size > 48) throw new IllegalArgumentException("Page must be >= 0 and size between 1 and 48");
        Sort order = switch (sort) {
            case "price-low" -> Sort.by("price").ascending();
            case "price-high" -> Sort.by("price").descending();
            case "area" -> Sort.by("areaSqft").descending();
            default -> Sort.by("createdAt").descending();
        };
        return PageView.from(properties.search(q, city, type, listing, minPrice, maxPrice, bedrooms,
                PageRequest.of(page, size, order)));
    }
    @GetMapping("/{id}") public PropertyView get(@PathVariable UUID id, Authentication auth) {
        return properties.get(id, auth == null || "anonymousUser".equals(auth.getName()) ? null : currentUser.require(auth));
    }
    @GetMapping("/mine") public List<PropertyView> mine(Authentication auth) {
        return properties.managed(currentUser.require(auth));
    }
    @PostMapping @org.springframework.web.bind.annotation.ResponseStatus(org.springframework.http.HttpStatus.CREATED)
    public PropertyView create(Authentication auth, @Valid @RequestBody PropertyRequest request) {
        return properties.create(currentUser.require(auth), request);
    }
    @PutMapping("/{id}") public PropertyView update(Authentication auth, @PathVariable UUID id,
                                                     @Valid @RequestBody PropertyRequest request) {
        return properties.update(currentUser.require(auth), id, request);
    }
    @DeleteMapping("/{id}") public org.springframework.http.ResponseEntity<Void> archive(Authentication auth, @PathVariable UUID id) {
        properties.archive(currentUser.require(auth), id);
        return org.springframework.http.ResponseEntity.noContent().build();
    }
}
