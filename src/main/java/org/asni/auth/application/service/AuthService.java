package org.asni.auth.application.service;

import org.asni.auth.application.command.LoginCommand;
import org.asni.auth.application.result.AuthResult;
import org.asni.auth.application.result.TokenInfo;
import org.asni.auth.domain.exception.InvalidCredentialsException;
import org.asni.auth.domain.model.User;
import org.asni.auth.domain.port.in.AuthUseCase;
import org.asni.auth.domain.port.out.TokenBlacklistPort;
import org.asni.auth.domain.port.out.UserRepository;
import org.asni.auth.infrastructure.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthService implements AuthUseCase {

    private final UserRepository userRepository;
    private final TokenBlacklistPort tokenBlacklist;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository,
                       TokenBlacklistPort tokenBlacklist,
                       JwtService jwtService,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.tokenBlacklist = tokenBlacklist;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public AuthResult login(LoginCommand command) {
        User user = userRepository.findByUsername(command.username())
                .orElseThrow(InvalidCredentialsException::new);

        if (!user.isActive()) {
            throw new InvalidCredentialsException();
        }

        if (!passwordEncoder.matches(command.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        String token = jwtService.generateToken(user);
        return new AuthResult(token, user.getId(), user.getUsername(), user.getRole());
    }

    @Override
    public void logout(String token) {
        if (!jwtService.isValid(token)) {
            return;
        }
        long ttl = jwtService.extractExpiration(token) - System.currentTimeMillis();
        if (ttl > 0) {
            tokenBlacklist.blacklist(token, ttl);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public TokenInfo validateToken(String token) {
        if (!jwtService.isValid(token) || tokenBlacklist.isBlacklisted(token)) {
            throw new InvalidCredentialsException();
        }
        return new TokenInfo(
                jwtService.extractUserId(token),
                jwtService.extractUsername(token),
                jwtService.extractRole(token)
        );
    }
}
