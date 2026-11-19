package edu.eci.dosw.oficioya_users.client;

import edu.eci.dosw.oficioya_users.exception.ServiceUnavailableException;
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

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
public class WorkerClient {
    public static final String ACTIVE = "ACTIVE";
    public static final String PAUSED = "PAUSED";
    private static final String UNAVAILABLE = "El servicio de trabajadores no está disponible. Intenta más tarde";

    private final RestClient restClient;
    private final ServiceTokenProvider tokenProvider;

    @Autowired
    public WorkerClient(RestClient.Builder builder,
                        @Value("${workers.url:http://localhost:8081}") String url,
                        @Value("${http.client.timeout-seconds:15}") long timeoutSeconds,
                        ServiceTokenProvider tokenProvider) {
        this(builder.baseUrl(url).requestFactory(timeoutFactory(Duration.ofSeconds(timeoutSeconds))).build(),
                tokenProvider);
    }

    WorkerClient(RestClient restClient, ServiceTokenProvider tokenProvider) {
        this.restClient = restClient;
        this.tokenProvider = tokenProvider;
    }

    public Optional<String> findWorkerIdByUser(UUID userId) {
        Map<?, ?> worker;
        try {
            worker = restClient.get()
                    .uri("/api/v1/workers/by-user/{userId}", userId)
                    .header(HttpHeaders.AUTHORIZATION, tokenProvider.bearer())
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
                    .header(HttpHeaders.AUTHORIZATION, tokenProvider.bearer())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("status", status))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException exception) {
            throw unavailable(exception);
        }
        log.info("Perfil de trabajador {} cambiado a {}", workerId, status);
    }

    private static SimpleClientHttpRequestFactory timeoutFactory(Duration timeout) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(timeout);
        factory.setReadTimeout(timeout);
        return factory;
    }

    private ServiceUnavailableException unavailable(RestClientException exception) {
        log.warn("Worker no respondió: {}", exception.getMessage());
        return new ServiceUnavailableException(UNAVAILABLE);
    }
}