package learn.videogameletterbox.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import learn.videogameletterbox.models.AppRole;
import learn.videogameletterbox.models.AppUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class JwtConverter {

    private final Key key;
    private final String issuer;
    private final long expirationMillis;

    public JwtConverter(@Value("${jwt.secret}") String secret,
                        @Value("${jwt.issuer}") String issuer,
                        @Value("${jwt.expiration-minutes}") int expirationMinutes) {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("JWT_SECRET must contain at least 32 characters.");
        }

        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.issuer = issuer;
        this.expirationMillis = expirationMinutes * 60L * 1000L;
    }

    public String getTokenFromUser(AppUser appUser) {
        String authorities = appUser.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .collect(Collectors.joining(","));

        return Jwts.builder()
                .setIssuer(issuer)
                .setSubject(appUser.getEmail())
                .claim("userId", appUser.getUserId())
                .claim("username", appUser.getDisplayUsername())
                .claim("authorities", authorities)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMillis))
                .signWith(key)
                .compact();
    }

    public AppUser getUserFromToken(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }

        try {
            Jws<Claims> jwt = Jwts.parserBuilder()
                    .requireIssuer(issuer)
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);

            Claims claims = jwt.getBody();
            AppUser appUser = new AppUser();
            appUser.setUserId(((Number) claims.get("userId")).longValue());
            appUser.setEmail(claims.getSubject());
            appUser.setDisplayUsername((String) claims.get("username"));
            appUser.setEnabled(true);
            appUser.setRoles(readRoles((String) claims.get("authorities")));
            return appUser;
        } catch (JwtException | IllegalArgumentException | ClassCastException ex) {
            return null;
        }
    }

    private Set<AppRole> readRoles(String authorities) {
        if (authorities == null || authorities.isBlank()) {
            return new HashSet<>();
        }

        return Arrays.stream(authorities.split(","))
                .map(AppRole::new)
                .collect(Collectors.toSet());
    }
}
