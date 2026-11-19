package edu.eci.dosw.oficioya_users.client;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Component
public class ServiceTokenProvider {

    private static final long EXPIRATION_MS = 60_000;

    // sub en UUID que pide Workers
    private static final String SERVICE_SUBJECT =
            UUID.nameUUIDFromBytes("oficioya-users".getBytes(StandardCharsets.UTF_8)).toString();

    private final SecretKey key;
    private final String rolesClaim;

    public ServiceTokenProvider(@Value("${jwt.secret}") String secret,
                                @Value("${jwt.roles-claim}") String rolesClaim) {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.rolesClaim = rolesClaim;
    }

    /* Token SERVICE propio del microservicio de Usuarios */
    public String bearer() {
        long now = System.currentTimeMillis();
        return "Bearer " + Jwts.builder()
                .subject(SERVICE_SUBJECT)
                .claim(rolesClaim, List.of("SERVICE"))
                .expiration(new Date(now + EXPIRATION_MS))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }
}