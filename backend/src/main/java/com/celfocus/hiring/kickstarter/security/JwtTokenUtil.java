package com.celfocus.hiring.kickstarter.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

@Component
public class JwtTokenUtil {

    private final Key key;
    private static final String SECRET_KEY = "b2df428b9929d3ace7c598bbf4e496b2";

    public JwtTokenUtil() {
        this.key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    Claims extractAllClaims(String token) {
        return Jwts.parser()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public List<String> extractRoles(String token) {
        Claims claims = extractAllClaims(token);
        Object rolesClaim = claims.get("roles");  // Assuming roles is the claim name

        if (rolesClaim instanceof List) {
            return (List<String>) rolesClaim;
        } else if (rolesClaim instanceof String[]) {
            return Arrays.asList((String[]) rolesClaim);
        } else {
            // Handle other cases, or throw an exception if the claim is unexpected
            throw new IllegalArgumentException("Unexpected type for 'roles' claim");
        }
    }


    public String generateToken(String username, String[] roles) {
        return Jwts.builder()
                .setSubject(username)
                .claim("roles", roles)  // Ensure roles are passed here correctly
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)) // 10 hours
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }



    public Boolean validateToken(String token, UserDetails userDetails) {
        final String extractedUsername = extractUsername(token);
        final List<String> extractedRoles = extractRoles(token);

        // Dynamically check if the token contains any valid role from userDetails
        boolean hasValidRole = userDetails.getAuthorities().stream()
                .anyMatch(authority -> {
                    String role = authority.getAuthority().replace("ROLE_", "");
                    return extractedRoles.contains(role) || extractedRoles.contains("ROLE_" + role);
                });

        return (extractedUsername.equals(userDetails.getUsername()) && !isTokenExpired(token) && hasValidRole);
    }
}