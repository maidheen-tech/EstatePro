package com.estatepro.security;

import com.estatepro.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {
    private final SecretKey key;
    private final long expiration;
    public JwtService(@Value("${estatepro.jwt.secret}") String secret,
                      @Value("${estatepro.jwt.expiration-ms:86400000}") long expiration) {
        if (secret.getBytes(StandardCharsets.UTF_8).length < 32)
            throw new IllegalArgumentException("estatepro.jwt.secret must contain at least 32 bytes");
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiration = expiration;
    }
    public String create(User user) {
        long now = System.currentTimeMillis();
        return Jwts.builder().subject(user.getEmail()).claim("role", user.getRole().name())
                .issuedAt(new Date(now)).expiration(new Date(now + expiration)).signWith(key).compact();
    }
    public Claims parse(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }
}
