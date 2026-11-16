package edu.eci.dosw.oficioya_users.client;

import edu.eci.dosw.oficioya_users.exception.ServiceUnavailableException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
public class WorkerClient {
    public static final String ACTIVE = "ACTIVE";
    public static final String PAUSED = "PAUSED";
    private static final Duration TIMEOUT = Duration.ofSeconds(3);
    private static final String UNAVAILABLE = "El servicio de trabajadores no está disponible. Intenta más tarde";

    private final RestClient restClient;
    private final SecretKey key;
    private final String rolesClaim;

    @Autowired
    public WorkerClient(RestClient.Builder builder,
                        @Value("${workers.url:http://localhost:8082}") String url,
                        @Value("${jwt.secret}") String secret,
                        @Value("${jwt.roles-claim}") String rolesClaim) {
        this(builder.baseUrl(url).requestFactory(timeoutFactory()).build(), secret, rolesClaim);
    }

    WorkerClient(RestClient restClient, String secret, String rolesClaim) {
        this.restClient = restClient;
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.rolesClaim = rolesClaim;
    }

    public Optional<String> findWorkerIdByUser(UUID userId) {
        Map<?, ?> worker;
        try {
            worker = restClient.get()
                    .uri("/api/v1/workers/by-user/{userId}", userId)
                    .header(HttpHeaders.AUTHORIZATION, serviceToken())
                    .retrieve()
                    .body(Map.class);
        } catch (HttpClientErrorException.NotFound exception) {
            return Optional.empty();
        } catch (RestClientException exception) {
            throw unavailable(exception);
        }
        if (worker == null || worker.get("id") == null) {
            throw new ServiceUnavailableException(UNAVAILABLE);
        }
        return Optional.of(String.valueOf(worker.get("id")));
    }

    public void updateStatus(String workerId, String status) {
        try {
            restClient.put()
                    .uri("/api/v1/workers/{id}/status", workerId)
                    .header(HttpHeaders.AUTHORIZATION, serviceToken())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("status", status))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException exception) {
            throw unavailable(exception);
        }
        log.info("Perfil de trabajador {} cambiado a {}", workerId, status);
    }

    private String serviceToken() {
        long now = System.currentTimeMillis();
        return "Bearer " + Jwts.builder()
                .subject("oficioya-users")
                .claim(rolesClaim, List.of("SERVICE"))
                .expiration(new Date(now + 60_000))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    private static SimpleClientHttpRequestFactory timeoutFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(TIMEOUT);
        factory.setReadTimeout(TIMEOUT);
        return factory;
    }

    private ServiceUnavailableException unavailable(RestClientException exception) {
        log.warn("Worker no respondió: {}", exception.getMessage());
        return new ServiceUnavailableException(UNAVAILABLE);
    }
}