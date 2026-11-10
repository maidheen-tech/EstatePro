package com.estatepro.controller;

import com.estatepro.dto.ApiDtos.*;
import com.estatepro.entity.Role;
import com.estatepro.exception.ResourceNotFoundException;
import com.estatepro.repository.UserRepository;
import com.estatepro.service.CurrentUserService;
import com.estatepro.service.PropertyService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final UserRepository users;
    private final PropertyService properties;
    private final CurrentUserService currentUser;
    public AdminController(UserRepository users, PropertyService properties, CurrentUserService currentUser) {
        this.users = users; this.properties = properties; this.currentUser = currentUser;
    }
    @GetMapping("/users")
    public List<UserView> users(@RequestParam(required = false) String q, @RequestParam(required = false) Role role) {
        return users.findAll().stream()
                .filter(u -> role == null || u.getRole() == role)
                .filter(u -> q == null || q.isBlank() || u.getFullName().toLowerCase().contains(q.toLowerCase())
                        || u.getEmail().toLowerCase().contains(q.toLowerCase()))
                .limit(250).map(UserView::from).toList();
    }
    @PatchMapping("/users/{id}")
    public UserView updateUser(Authentication auth, @PathVariable UUID id, @Valid @RequestBody UserUpdateRequest request) {
        var administrator = currentUser.require(auth);
        var user = users.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (user.getId().equals(administrator.getId()) && request.role() != Role.ADMIN)
            throw new IllegalArgumentException("You cannot remove your own administrator role");
        if (user.getId().equals(administrator.getId()) && Boolean.FALSE.equals(request.enabled()))
            throw new IllegalArgumentException("You cannot deactivate your own administrator account");
        user.setRole(request.role());
        if (request.enabled() != null) user.setEnabled(request.enabled());
        return UserView.from(users.save(user));
    }
    @PatchMapping("/properties/{id}/moderation")
    public PropertyView moderate(Authentication auth, @PathVariable UUID id,
                                 @Valid @RequestBody ModerationRequest request) {
        var administrator = currentUser.require(auth);
        if (request.status().name().equals("REJECTED") && (request.reason() == null || request.reason().isBlank()))
            throw new IllegalArgumentException("A rejection reason is required");
        return properties.moderate(administrator, id, request.status(), request.reason());
    }
    @PutMapping("/properties/{id}/agent/{agentId}")
    public PropertyView assignAgent(@PathVariable UUID id, @PathVariable UUID agentId) {
        return properties.assign(id, agentId);
    }
    @GetMapping(value = "/reports/properties.csv", produces = "text/csv")
    public ResponseEntity<byte[]> exportProperties(Authentication auth) {
        StringBuilder csv = new StringBuilder("Property,City,Locality,Price,Status\n");
        properties.managed(currentUser.require(auth)).forEach(p -> csv.append(csv(p.title())).append(',')
                .append(csv(p.city())).append(',').append(csv(p.locality())).append(',')
                .append(p.price()).append(',').append(p.status()).append('\n'));
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=estatepro-properties.csv")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(csv.toString().getBytes(StandardCharsets.UTF_8));
    }
    private String csv(String input) {
        String value = input == null ? "" : input;
        if (!value.isEmpty() && "=+-@".indexOf(value.charAt(0)) >= 0) value = "'" + value;
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}
