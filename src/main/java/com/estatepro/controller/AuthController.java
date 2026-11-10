package com.estatepro.controller;

import com.estatepro.dto.ApiDtos.*;
import com.estatepro.service.AuthService;
import com.estatepro.service.CurrentUserService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService auth;
    private final CurrentUserService currentUser;
    public AuthController(AuthService auth, CurrentUserService currentUser) {
        this.auth = auth; this.currentUser = currentUser;
    }
    @PostMapping("/register") @org.springframework.web.bind.annotation.ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return auth.register(request);
    }
    @PostMapping("/login") public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return auth.login(request);
    }
    @GetMapping("/me") public UserView me(Authentication authentication) {
        return UserView.from(currentUser.require(authentication));
    }
}
