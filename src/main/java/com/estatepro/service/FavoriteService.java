package com.estatepro.service;

import com.estatepro.dto.ApiDtos.PropertyView;
import com.estatepro.entity.Favorite;
import com.estatepro.entity.Role;
import com.estatepro.entity.User;
import com.estatepro.repository.FavoriteRepository;
import com.estatepro.repository.PropertyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

@Service
public class FavoriteService {
    private final FavoriteRepository favorites;
    private final PropertyRepository properties;
    private final PropertyService propertyService;
    public FavoriteService(FavoriteRepository favorites, PropertyRepository properties, PropertyService propertyService) {
        this.favorites = favorites; this.properties = properties; this.propertyService = propertyService;
    }
    @Transactional(readOnly = true)
    public List<PropertyView> list(User user) {
        requireBuyer(user);
        return favorites.findByUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .map(f -> PropertyView.from(f.getProperty())).toList();
    }
    @Transactional
    public void add(User user, UUID propertyId) {
        requireBuyer(user);
        if (favorites.findByUserIdAndPropertyId(user.getId(), propertyId).isPresent()) return;
        favorites.save(new Favorite(user, propertyService.requirePublished(propertyId)));
    }
    @Transactional
    public void remove(User user, UUID propertyId) {
        requireBuyer(user);
        favorites.findByUserIdAndPropertyId(user.getId(), propertyId).ifPresent(favorites::delete);
    }
    private void requireBuyer(User user) {
        if (user.getRole() != Role.BUYER) throw new SecurityException("This feature is available to buyer accounts");
    }
}
