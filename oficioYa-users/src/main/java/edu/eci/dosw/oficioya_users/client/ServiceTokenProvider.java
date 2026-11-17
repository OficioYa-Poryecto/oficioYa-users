package edu.eci.dosw.oficioya_users.client;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;

@Component
public class ServiceTokenProvider {

    private static final long EXPIRATION_MS = 60_000;

    private final SecretKey key;
    private final String rolesClaim;

    public ServiceTokenProvider(@Value("${jwt.secret}") String secret,
                                @Value("${jwt.roles-claim}") String rolesClaim) {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.rolesClaim = rolesClaim;
    }

    public String bearer() {
        long now = System.currentTimeMillis();
        return "Bearer " + Jwts.builder()
                .subject("oficioya-users")
                .claim(rolesClaim, List.of("SERVICE"))
                .expiration(new Date(now + EXPIRATION_MS))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }
}