package com.travelnest.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final JwtProperties jwtProperties;

    public JwtService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    public String generateAccessToken(AuthenticatedUser user) {
        return buildToken(user, jwtProperties.accessTokenExpiration(), "access");
    }

    public String generateRefreshToken(AuthenticatedUser user) {
        return buildToken(user, jwtProperties.refreshTokenExpiration(), "refresh");
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    public boolean isTokenValid(String token, AuthenticatedUser user) {
        Claims claims = extractAllClaims(token);
        return claims.getSubject().equals(user.getUsername()) && claims.getExpiration().after(new Date());
    }

    private String buildToken(AuthenticatedUser user, long expiration, String tokenType) {
        Date now = new Date();
        return Jwts.builder()
                .subject(user.getUsername())
                .claim("uid", user.getUserId())
                .claim("name", user.getDisplayName())
                .claim("role", user.getRole())
                .claim("type", tokenType)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expiration))
                .signWith(getSigningKey())
                .compact();
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith((javax.crypto.SecretKey) getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private Key getSigningKey() {
        String secret = jwtProperties.secret();
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}
