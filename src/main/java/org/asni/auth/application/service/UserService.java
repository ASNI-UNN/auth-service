package org.asni.auth.application.service;

import org.asni.auth.application.command.CreateUserCommand;
import org.asni.auth.application.command.UpdateUserCommand;
import org.asni.auth.domain.exception.UserAlreadyExistsException;
import org.asni.auth.domain.exception.UserNotFoundException;
import org.asni.auth.domain.model.User;
import org.asni.auth.domain.port.in.UserUseCase;
import org.asni.auth.domain.port.out.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class UserService implements UserUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User createUser(CreateUserCommand command) {
        if (userRepository.existsByUsername(command.username())) {
            throw new UserAlreadyExistsException("username", command.username());
        }
        if (userRepository.existsByEmail(command.email())) {
            throw new UserAlreadyExistsException("email", command.email());
        }
        User user = User.create(
                command.username(),
                command.email(),
                passwordEncoder.encode(command.password()),
                command.role()
        );
        return userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public User findById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public User update(UUID id, UpdateUserCommand command) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        if (command.username() != null && !command.username().equals(user.getUsername())) {
            if (userRepository.existsByUsername(command.username())) {
                throw new UserAlreadyExistsException("username", command.username());
            }
            user.updateUsername(command.username());
        }
        if (command.email() != null && !command.email().equals(user.getEmail())) {
            if (userRepository.existsByEmail(command.email())) {
                throw new UserAlreadyExistsException("email", command.email());
            }
            user.updateEmail(command.email());
        }
        if (command.role() != null) {
            user.updateRole(command.role());
        }
        if (command.active() != null) {
            if (command.active()) user.activate(); else user.deactivate();
        }

        return userRepository.save(user);
    }

    @Override
    public void delete(UUID id) {
        if (userRepository.findById(id).isEmpty()) {
            throw new UserNotFoundException(id);
        }
        userRepository.deleteById(id);
    }
}
