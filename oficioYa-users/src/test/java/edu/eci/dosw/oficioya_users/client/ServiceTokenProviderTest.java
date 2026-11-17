package edu.eci.dosw.oficioya_users.client;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ServiceTokenProviderTest {

    private static final String SECRET = "IadhiSp/AVyloXuE15l29oZzpucys4NEfqpHn6PEMeQ=";

    @Test
    void bearerIsSignedServiceTokenWithConfiguredRolesClaim() {
        String bearer = new ServiceTokenProvider(SECRET, "roles").bearer();

        assertThat(bearer).startsWith("Bearer ");
        Claims claims = Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET)))
                .build()
                .parseSignedClaims(bearer.substring("Bearer ".length()))
                .getPayload();
        assertThat(claims.getSubject()).isEqualTo("oficioya-users");
        assertThat(claims.get("roles", List.class)).containsExactly("SERVICE");
        assertThat(claims.getExpiration()).isInTheFuture();
    }
}