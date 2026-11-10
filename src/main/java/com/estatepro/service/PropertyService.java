package com.estatepro.service;

import com.estatepro.dto.ApiDtos.PropertyRequest;
import com.estatepro.dto.ApiDtos.PropertyView;
import com.estatepro.entity.*;
import com.estatepro.exception.ResourceNotFoundException;
import com.estatepro.repository.PropertyRepository;
import com.estatepro.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.core.io.ClassPathResource;
import java.math.BigDecimal;
import java.util.UUID;

@Service
public class PropertyService {
    private final PropertyRepository properties;
    private final UserRepository users;
    public PropertyService(PropertyRepository properties, UserRepository users) {
        this.properties = properties; this.users = users;
    }
    @Transactional(readOnly = true)
    public Page<PropertyView> search(String keyword, String city, String type, String listing, BigDecimal minPrice,
                                     BigDecimal maxPrice, Integer bedrooms, Pageable pageable) {
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0)
            throw new IllegalArgumentException("Minimum price cannot exceed maximum price");
        Specification<Property> spec = (root, query, cb) -> cb.equal(root.get("status"), ListingStatus.PUBLISHED);
        if (has(keyword)) {
            String pattern = "%" + keyword.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("title")), pattern),
                    cb.like(cb.lower(root.get("locality")), pattern),
                    cb.like(cb.lower(root.get("city")), pattern),
                    cb.like(cb.lower(root.get("description")), pattern)));
        }
        if (has(city)) spec = spec.and((r, q, cb) -> cb.like(cb.lower(r.get("city")), "%" + city.trim().toLowerCase() + "%"));
        if (has(type)) spec = spec.and((r, q, cb) -> cb.equal(cb.lower(r.get("propertyType")), type.trim().toLowerCase()));
        if (has(listing)) spec = spec.and((r, q, cb) -> cb.equal(cb.lower(r.get("listingType")), listing.trim().toLowerCase()));
        if (minPrice != null) spec = spec.and((r, q, cb) -> cb.greaterThanOrEqualTo(r.get("price"), minPrice));
        if (maxPrice != null) spec = spec.and((r, q, cb) -> cb.lessThanOrEqualTo(r.get("price"), maxPrice));
        if (bedrooms != null) spec = spec.and((r, q, cb) -> cb.greaterThanOrEqualTo(r.get("bedrooms"), bedrooms));
        return properties.findAll(spec, pageable).map(PropertyView::from);
    }
    @Transactional(readOnly = true)
    public PropertyView get(UUID id, User viewer) {
        Property p = properties.findById(id).orElseThrow(() -> new ResourceNotFoundException("Property not found"));
        if (p.getStatus() != ListingStatus.PUBLISHED && (viewer == null
                || viewer.getRole() != Role.ADMIN && !owns(p, viewer)))
            throw new ResourceNotFoundException("Property not found");
        return PropertyView.from(p);
    }
    @Transactional(readOnly = true)
    public java.util.List<PropertyView> managed(User user) {
        if (user.getRole() == Role.ADMIN) return properties.findAll().stream().map(PropertyView::from).toList();
        if (user.getRole() == Role.AGENT) return properties.findByAgentIdOrderByCreatedAtDesc(user.getId()).stream().map(PropertyView::from).toList();
        return properties.findByOwnerIdOrderByCreatedAtDesc(user.getId()).stream().map(PropertyView::from).toList();
    }
    @Transactional
    public PropertyView create(User user, PropertyRequest request) {
        requireRole(user, Role.ADMIN, Role.AGENT, Role.SELLER);
        Property property = new Property(request.title().trim(), request.description().trim(),
                request.propertyType(), request.listingType(), request.city().trim(), request.locality().trim(),
                request.address(), request.price(), request.bedrooms(), request.bathrooms(), request.areaSqft(),
                safeImagePath(request.imageUrl()), ListingStatus.PENDING,
                user.getRole() == Role.SELLER ? user : null, false);
        if (user.getRole() == Role.AGENT) property.setAgent(user);
        return PropertyView.from(properties.save(property));
    }
    @Transactional
    public PropertyView update(User user, UUID id, PropertyRequest request) {
        Property p = owned(id, user);
        if (p.getStatus() == ListingStatus.SOLD || p.getStatus() == ListingStatus.RENTED)
            throw new IllegalArgumentException("Completed listings cannot be edited");
        p.update(request.title().trim(), request.description().trim(), request.propertyType(), request.listingType(),
                request.city().trim(), request.locality().trim(), request.address(), request.price(),
                request.bedrooms(), request.bathrooms(), request.areaSqft(), safeImagePath(request.imageUrl()));
        if (user.getRole() != Role.ADMIN) p.setStatus(ListingStatus.PENDING);
        return PropertyView.from(properties.save(p));
    }
    @Transactional
    public void archive(User user, UUID id) {
        Property p = owned(id, user);
        if (p.getStatus() == ListingStatus.SOLD || p.getStatus() == ListingStatus.RENTED)
            throw new IllegalArgumentException("Completed listings must remain in the property history");
        p.setStatus(ListingStatus.ARCHIVED);
        properties.save(p);
    }
    @Transactional
    public PropertyView moderate(User admin, UUID id, ListingStatus status, String reason) {
        requireRole(admin, Role.ADMIN);
        if (status != ListingStatus.PUBLISHED && status != ListingStatus.REJECTED)
            throw new IllegalArgumentException("Admin moderation accepts PUBLISHED or REJECTED only");
        Property p = properties.findById(id).orElseThrow(() -> new ResourceNotFoundException("Property not found"));
        p.setStatus(status);
        p.setModerationNote(status == ListingStatus.REJECTED ? reason : null);
        return PropertyView.from(properties.save(p));
    }
    @Transactional
    public PropertyView assign(UUID id, UUID agentId) {
        Property p = properties.findById(id).orElseThrow(() -> new ResourceNotFoundException("Property not found"));
        User agent = users.findById(agentId).orElseThrow(() -> new ResourceNotFoundException("Agent not found"));
        if (agent.getRole() != Role.AGENT) throw new IllegalArgumentException("The selected user is not an agent");
        p.setAgent(agent);
        return PropertyView.from(properties.save(p));
    }
    public Property requirePublished(UUID id) {
        Property property = properties.findById(id).orElseThrow(() -> new ResourceNotFoundException("Property not found"));
        if (property.getStatus() != ListingStatus.PUBLISHED) throw new IllegalArgumentException("Property is not available");
        return property;
    }
    public Property requireManaged(UUID id, User user) { return owned(id, user); }
    private Property owned(UUID id, User user) {
        Property p = properties.findById(id).orElseThrow(() -> new ResourceNotFoundException("Property not found"));
        if (user.getRole() == Role.ADMIN) return p;
        boolean allowed = owns(p, user);
        if (!allowed) throw new SecurityException("You do not have access to this property");
        return p;
    }
    private boolean owns(Property p, User user) {
        return user.getRole() == Role.SELLER && p.getOwner() != null && p.getOwner().getId().equals(user.getId())
                || user.getRole() == Role.AGENT && p.getAgent() != null && p.getAgent().getId().equals(user.getId());
    }
    private void requireRole(User user, Role... roles) {
        for (Role role : roles) if (user.getRole() == role) return;
        throw new SecurityException("Your account does not have permission for this action");
    }
    private boolean has(String value) { return value != null && !value.isBlank(); }
    private String safeImagePath(String image) {
        if (!image.matches("/assets/images/properties/[a-z0-9-]+\\.jpg"))
            throw new IllegalArgumentException("Choose a local EstatePro image");
        if (!new ClassPathResource("static" + image).exists())
            throw new IllegalArgumentException("The selected property image is not available locally");
        return image;
    }
}
