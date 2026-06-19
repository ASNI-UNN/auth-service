package org.asni.auth.infrastructure.config;

import org.asni.auth.domain.model.Role;
import org.asni.auth.domain.model.User;
import org.asni.auth.domain.port.out.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
class AdminInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.default.username}")
    private String username;

    @Value("${admin.default.password}")
    private String password;

    @Value("${admin.default.email}")
    private String email;

    AdminInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (userRepository.findByUsername(username).isPresent()) {
            return;
        }
        userRepository.save(User.create(username, email, passwordEncoder.encode(password), Role.ADMIN));
    }
}
