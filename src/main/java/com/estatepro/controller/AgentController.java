package com.estatepro.controller;

import com.estatepro.entity.Role;
import com.estatepro.repository.UserRepository;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/agents")
public class AgentController {
    private static final List<String> PORTRAITS = List.of(
            "/assets/images/agents/agent-01.jpg",
            "/assets/images/agents/agent-02.jpg",
            "/assets/images/agents/agent-03.jpg",
            "/assets/images/agents/agent-04.jpg");
    private final UserRepository users;
    public AgentController(UserRepository users) { this.users = users; }
    @GetMapping
    public List<AgentView> agents() {
        return users.findAll().stream().filter(user -> user.getRole() == Role.AGENT && user.isEnabled())
                .limit(4).map(user -> new AgentView(user.getId(), user.getFullName(),
                        PORTRAITS.get(Math.floorMod(user.getId().hashCode(), PORTRAITS.size())))).toList();
    }
    public record AgentView(UUID id, String fullName, String portraitUrl) {}
}
