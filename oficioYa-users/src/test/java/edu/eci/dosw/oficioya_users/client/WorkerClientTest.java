package edu.eci.dosw.oficioya_users.client;

import edu.eci.dosw.oficioya_users.exception.ServiceUnavailableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.UUID;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class WorkerClientTest {

    private static final String SECRET = "IadhiSp/AVyloXuE15l29oZzpucys4NEfqpHn6PEMeQ=";
    private static final String ME = "http://worker/api/v1/workers/me";

    private final UUID userId = UUID.randomUUID();
    private MockRestServiceServer server;
    private WorkerClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://worker");
        server = MockRestServiceServer.bindTo(builder).build();
        client = new WorkerClient(builder.build(), new ServiceTokenProvider(SECRET, "roles"));
    }

    @Test
    void findWorkerIdByUserReturnsIdUsingServiceToken() {
        server.expect(requestTo(ME))
                .andExpect(header(HttpHeaders.AUTHORIZATION, startsWith("Bearer ")))
                .andRespond(withSuccess("{\"id\":\"w-1\"}", MediaType.APPLICATION_JSON));

        assertThat(client.findWorkerIdByUser(userId)).contains("w-1");
    }

    @Test
    void findWorkerIdByUserReturnsEmptyOn404() {
        server.expect(requestTo(ME)).andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertThat(client.findWorkerIdByUser(userId)).isEmpty();
    }

    @Test
    void findWorkerIdByUserThrows503WhenWorkerFails() {
        server.expect(requestTo(ME)).andRespond(withServerError());

        assertThrows(ServiceUnavailableException.class, () -> client.findWorkerIdByUser(userId));
    }

    @Test
    void updateStatusSendsNewStatus() {
        server.expect(requestTo("http://worker/api/v1/workers/w-1/status"))
                .andExpect(method(HttpMethod.PUT))
                .andExpect(content().json("{\"status\":\"PAUSED\"}"))
                .andRespond(withSuccess());

        client.updateStatus("w-1", WorkerClient.PAUSED);
        server.verify();
    }

    // Método auxiliar que toma el JWT del cliente y devuelva el sub (usuario dentro del token)
    // y verifica que el header no empiece por Bearer
    private static String subjectOf(String bearer) {
        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET));
        return Jwts.parser().verifyWith(key).build()
                .parseSignedClaims(bearer.substring("Bearer ".length()))
                .getPayload().getSubject();
    }
}