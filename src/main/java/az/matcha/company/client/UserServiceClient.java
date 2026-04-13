package az.matcha.company.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Map;
import java.util.UUID;

@Component
public class UserServiceClient {

    private static final Logger log = LoggerFactory.getLogger(UserServiceClient.class);

    private final RestClient userServiceRestClient;

    public UserServiceClient(@Qualifier("userServiceRestClient") RestClient userServiceRestClient) {
        this.userServiceRestClient = userServiceRestClient;
    }

    /**
     * Notifies user-service to set companyId on the employer profile.
     * Non-fatal: logs error instead of propagating if call fails.
     */
    public void assignCompanyToEmployer(UUID userId, UUID companyId) {
        String token = extractBearerToken();
        try {
            userServiceRestClient.put()
                    .uri("/api/v1/internal/profiles/employer/{userId}/company", userId)
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("companyId", companyId))
                    .retrieve()
                    .toBodilessEntity();
            log.debug("Assigned companyId={} to employer userId={}", companyId, userId);
        } catch (RestClientException ex) {
            log.error("Failed to assign companyId={} to employer userId={}: {}", companyId, userId, ex.getMessage());
        }
    }

    private String extractBearerToken() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            return jwtAuth.getToken().getTokenValue();
        }
        throw new IllegalStateException("No JWT token found in security context");
    }
}
