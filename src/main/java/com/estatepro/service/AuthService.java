package com.estatepro.service;

import com.estatepro.dto.ApiDtos.*;
import com.estatepro.entity.Role;
import com.estatepro.entity.User;
import com.estatepro.repository.UserRepository;
import com.estatepro.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final UserRepository users;
    private final PasswordEncoder passwords;
    private final JwtService jwt;
    public AuthService(UserRepository users, PasswordEncoder passwords, JwtService jwt) {
        this.users = users; this.passwords = passwords; this.jwt = jwt;
    }
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (request.role() != Role.BUYER && request.role() != Role.SELLER)
            throw new IllegalArgumentException("Self-registration is limited to buyer and seller accounts");
        String email = request.email().trim().toLowerCase();
        if (users.existsByEmail(email)) throw new IllegalArgumentException("An account already exists for this email");
        User user = users.save(new User(email, passwords.encode(request.password()), request.fullName().trim(), request.role()));
        return new AuthResponse(jwt.create(user), UserView.from(user));
    }
    public AuthResponse login(LoginRequest request) {
        User user = users.findByEmail(request.email().trim().toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("Email or password is incorrect"));
        if (!user.isEnabled() || !passwords.matches(request.password(), user.getPasswordHash()))
            throw new IllegalArgumentException("Email or password is incorrect");
        return new AuthResponse(jwt.create(user), UserView.from(user));
    }
}
