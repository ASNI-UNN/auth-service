package org.asni.auth.infrastructure.web;

import org.asni.auth.TestcontainersConfig;
import org.asni.auth.domain.model.Role;
import org.asni.auth.domain.model.User;
import org.asni.auth.domain.port.out.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfig.class)
class AuthControllerIT {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        if (userRepository.findByUsername("ituser").isEmpty()) {
            userRepository.save(
                    User.create("ituser", "it@test.com", passwordEncoder.encode("password123"), Role.ANALYST)
            );
        }
    }

    @Test
    void login_withValidCredentials_returns200AndToken() {
        var request = Map.of("username", "ituser", "password", "password123");

        ResponseEntity<Map> response = restTemplate.postForEntity("/api/v1/auth/login", request, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsKey("token");
        assertThat(response.getBody()).containsKey("role");
    }

    @Test
    void login_withWrongPassword_returns401() {
        var request = Map.of("username", "ituser", "password", "wrong");

        ResponseEntity<Map> response = restTemplate.postForEntity("/api/v1/auth/login", request, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void login_withMissingFields_returns400() {
        var request = Map.of("username", "ituser");

        ResponseEntity<Map> response = restTemplate.postForEntity("/api/v1/auth/login", request, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void validateToken_withValidToken_returns200() {
        var loginRequest = Map.of("username", "ituser", "password", "password123");
        ResponseEntity<Map> loginResponse = restTemplate.postForEntity("/api/v1/auth/login", loginRequest, Map.class);
        String token = (String) loginResponse.getBody().get("token");

        var validateRequest = Map.of("token", token);
        ResponseEntity<Map> validateResponse = restTemplate.postForEntity("/api/v1/auth/validate", validateRequest, Map.class);

        assertThat(validateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(validateResponse.getBody()).containsKey("userId");
        assertThat(validateResponse.getBody()).containsEntry("username", "ituser");
    }

    @Test
    void logout_invalidatesToken() {
        var loginRequest = Map.of("username", "ituser", "password", "password123");
        ResponseEntity<Map> loginResponse = restTemplate.postForEntity("/api/v1/auth/login", loginRequest, Map.class);
        String token = (String) loginResponse.getBody().get("token");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        restTemplate.exchange("/api/v1/auth/logout", HttpMethod.POST, new HttpEntity<>(headers), Void.class);

        var validateRequest = Map.of("token", token);
        ResponseEntity<Map> validateResponse = restTemplate.postForEntity("/api/v1/auth/validate", validateRequest, Map.class);
        assertThat(validateResponse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
