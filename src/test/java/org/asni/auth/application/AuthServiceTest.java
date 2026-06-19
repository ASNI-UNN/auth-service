package org.asni.auth.application;

import org.asni.auth.application.command.LoginCommand;
import org.asni.auth.application.result.AuthResult;
import org.asni.auth.application.service.AuthService;
import org.asni.auth.domain.exception.InvalidCredentialsException;
import org.asni.auth.domain.model.Role;
import org.asni.auth.domain.model.User;
import org.asni.auth.domain.port.out.TokenBlacklistPort;
import org.asni.auth.domain.port.out.UserRepository;
import org.asni.auth.infrastructure.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private TokenBlacklistPort tokenBlacklist;
    @Mock
    private JwtService jwtService;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.create("analyst1", "analyst@test.com", "hashed", Role.ANALYST);
    }

    @Test
    void login_withValidCredentials_returnsToken() {
        when(userRepository.findByUsername("analyst1")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password", "hashed")).thenReturn(true);
        when(jwtService.generateToken(testUser)).thenReturn("jwt.token.here");

        AuthResult result = authService.login(new LoginCommand("analyst1", "password"));

        assertThat(result.token()).isEqualTo("jwt.token.here");
        assertThat(result.role()).isEqualTo(Role.ANALYST);
        assertThat(result.username()).isEqualTo("analyst1");
    }

    @Test
    void login_withUnknownUsername_throwsInvalidCredentials() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new LoginCommand("nobody", "pass")))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void login_withWrongPassword_throwsInvalidCredentials() {
        when(userRepository.findByUsername("analyst1")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        assertThatThrownBy(() -> authService.login(new LoginCommand("analyst1", "wrong")))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void login_withInactiveUser_throwsInvalidCredentials() {
        testUser.deactivate();
        when(userRepository.findByUsername("analyst1")).thenReturn(Optional.of(testUser));

        assertThatThrownBy(() -> authService.login(new LoginCommand("analyst1", "password")))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(passwordEncoder, never()).matches(any(), any());
    }

    @Test
    void logout_withValidToken_blacklistsIt() {
        long futureExpiry = System.currentTimeMillis() + 3_600_000;
        when(jwtService.isValid("valid.token")).thenReturn(true);
        when(jwtService.extractExpiration("valid.token")).thenReturn(futureExpiry);

        authService.logout("valid.token");

        verify(tokenBlacklist).blacklist(eq("valid.token"), longThat(ttl -> ttl > 0));
    }

    @Test
    void logout_withInvalidToken_doesNothing() {
        when(jwtService.isValid("bad.token")).thenReturn(false);

        authService.logout("bad.token");

        verify(tokenBlacklist, never()).blacklist(any(), anyLong());
    }

    @Test
    void validateToken_withBlacklistedToken_throwsInvalidCredentials() {
        when(jwtService.isValid("blacklisted")).thenReturn(true);
        when(tokenBlacklist.isBlacklisted("blacklisted")).thenReturn(true);

        assertThatThrownBy(() -> authService.validateToken("blacklisted"))
                .isInstanceOf(InvalidCredentialsException.class);
    }
}
