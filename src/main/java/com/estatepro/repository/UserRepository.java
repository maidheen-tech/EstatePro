package com.estatepro.repository;

import com.estatepro.entity.Role;
import com.estatepro.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    boolean existsByEmail(String email);
    java.util.Optional<User> findByEmail(String email);
    long countByRole(Role role);
}
