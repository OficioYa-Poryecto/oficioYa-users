package edu.eci.dosw.oficioya_users.client;

import edu.eci.dosw.oficioya_users.enums.RoleName;
import edu.eci.dosw.oficioya_users.exception.ServiceUnavailableException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

/** Integración con Servicios **/
@Slf4j
@Component
public class RequestsClient {
    private static final Duration TIMEOUT = Duration.ofSeconds(3);
    private static final String UNAVAILABLE = "El servicio de solicitudes no está disponible. Intenta más tarde";

    private final RestClient restClient;
    private final ServiceTokenProvider tokenProvider;

    @Autowired
    public RequestsClient(RestClient.Builder builder,
                          @Value("${requests.url:http://localhost:8083}") String url,
                          ServiceTokenProvider tokenProvider) {
        this(builder.baseUrl(url).requestFactory(timeoutFactory()).build(), tokenProvider);
    }

    RequestsClient(RestClient restClient, ServiceTokenProvider tokenProvider) {
        this.restClient = restClient;
        this.tokenProvider = tokenProvider;
    }

    record ActiveRequests(Long active) {
    }

    public long countActive(UUID userId, RoleName role) {
        ActiveRequests body;
        try {
            body = restClient.get()
                    .uri(uri -> uri.path("/api/v1/requests/active")
                            .queryParam("userId", userId)
                            .queryParam("role", role.name())
                            .build())
                    .header(HttpHeaders.AUTHORIZATION, tokenProvider.bearer())
                    .retrieve()
                    .body(ActiveRequests.class);
        } catch (RestClientException exception) {
            log.warn("Servicios no respondió: {}", exception.getMessage());
            throw new ServiceUnavailableException(UNAVAILABLE);
        }
        if (body == null || body.active() == null) {
            log.warn("Servicios respondió sin el campo active");
            throw new ServiceUnavailableException(UNAVAILABLE);
        }
        return body.active();
    }

    private static SimpleClientHttpRequestFactory timeoutFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(TIMEOUT);
        factory.setReadTimeout(TIMEOUT);
        return factory;
    }
}