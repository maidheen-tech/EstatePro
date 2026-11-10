package com.estatepro.service;

import com.estatepro.entity.User;
import com.estatepro.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {
    private final UserRepository users;
    public CurrentUserService(UserRepository users) { this.users = users; }
    public User require(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated())
            throw new SecurityException("Authentication is required");
        return users.findByEmail(authentication.getName())
                .orElseThrow(() -> new SecurityException("Account is unavailable"));
    }
    public void requireRole(User user, com.estatepro.entity.Role... roles) {
        for (var role : roles) if (user.getRole() == role) return;
        throw new SecurityException("Your account does not have permission for this action");
    }
}
