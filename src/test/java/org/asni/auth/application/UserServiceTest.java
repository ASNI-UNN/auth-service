package org.asni.auth.application;

import org.asni.auth.application.command.CreateUserCommand;
import org.asni.auth.application.command.UpdateUserCommand;
import org.asni.auth.application.service.UserService;
import org.asni.auth.domain.exception.UserAlreadyExistsException;
import org.asni.auth.domain.exception.UserNotFoundException;
import org.asni.auth.domain.model.Role;
import org.asni.auth.domain.model.User;
import org.asni.auth.domain.port.out.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User existingUser;

    @BeforeEach
    void setUp() {
        existingUser = User.create("specialist1", "spec@test.com", "hashed", Role.SPECIALIST);
    }

    @Test
    void createUser_withUniqueData_savesAndReturns() {
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@test.com")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        var command = new CreateUserCommand("newuser", "new@test.com", "password", Role.RESPONDENT);
        User created = userService.createUser(command);

        assertThat(created.getUsername()).isEqualTo("newuser");
        assertThat(created.getRole()).isEqualTo(Role.RESPONDENT);
        assertThat(created.isActive()).isTrue();
    }

    @Test
    void createUser_withDuplicateUsername_throwsConflict() {
        when(userRepository.existsByUsername("specialist1")).thenReturn(true);

        var command = new CreateUserCommand("specialist1", "other@test.com", "pass", Role.SPECIALIST);

        assertThatThrownBy(() -> userService.createUser(command))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("username");
    }

    @Test
    void createUser_withDuplicateEmail_throwsConflict() {
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("spec@test.com")).thenReturn(true);

        var command = new CreateUserCommand("newuser", "spec@test.com", "pass", Role.SPECIALIST);

        assertThatThrownBy(() -> userService.createUser(command))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("email");
    }

    @Test
    void findById_withExistingId_returnsUser() {
        UUID id = existingUser.getId();
        when(userRepository.findById(id)).thenReturn(Optional.of(existingUser));

        User found = userService.findById(id);

        assertThat(found.getId()).isEqualTo(id);
    }

    @Test
    void findById_withMissingId_throwsNotFound() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findById(id))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void findAll_returnsList() {
        when(userRepository.findAll()).thenReturn(List.of(existingUser));

        assertThat(userService.findAll()).hasSize(1);
    }

    @Test
    void update_changesRoleAndDeactivates() {
        UUID id = existingUser.getId();
        when(userRepository.findById(id)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var command = new UpdateUserCommand(null, null, Role.ANALYST, false);
        User updated = userService.update(id, command);

        assertThat(updated.getRole()).isEqualTo(Role.ANALYST);
        assertThat(updated.isActive()).isFalse();
    }

    @Test
    void delete_withMissingId_throwsNotFound() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.delete(id))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository, never()).deleteById(any());
    }
}
