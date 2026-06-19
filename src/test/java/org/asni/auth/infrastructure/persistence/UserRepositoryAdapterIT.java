package org.asni.auth.infrastructure.persistence;

import org.asni.auth.TestcontainersConfig;
import org.asni.auth.domain.model.Role;
import org.asni.auth.domain.model.User;
import org.asni.auth.domain.port.out.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({UserRepositoryAdapter.class, TestcontainersConfig.class})
@ActiveProfiles("test")
class UserRepositoryAdapterIT {

    @Autowired
    private UserRepository userRepository;

    private User savedUser;

    @BeforeEach
    void setUp() {
        savedUser = userRepository.save(
                User.create("testuser", "test@test.com", "hash", Role.ANALYST)
        );
    }

    @Test
    void save_andFindById_roundtrip() {
        Optional<User> found = userRepository.findById(savedUser.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("testuser");
        assertThat(found.get().getEmail()).isEqualTo("test@test.com");
        assertThat(found.get().getRole()).isEqualTo(Role.ANALYST);
        assertThat(found.get().isActive()).isTrue();
    }

    @Test
    void findByUsername_returnsUser() {
        Optional<User> found = userRepository.findByUsername("testuser");
        assertThat(found).isPresent();
    }

    @Test
    void findByEmail_returnsUser() {
        Optional<User> found = userRepository.findByEmail("test@test.com");
        assertThat(found).isPresent();
    }

    @Test
    void existsByUsername_returnsTrue() {
        assertThat(userRepository.existsByUsername("testuser")).isTrue();
        assertThat(userRepository.existsByUsername("nobody")).isFalse();
    }

    @Test
    void existsByEmail_returnsTrue() {
        assertThat(userRepository.existsByEmail("test@test.com")).isTrue();
        assertThat(userRepository.existsByEmail("nope@test.com")).isFalse();
    }

    @Test
    void findAll_returnsSavedUsers() {
        List<User> all = userRepository.findAll();
        assertThat(all).isNotEmpty();
    }

    @Test
    void deleteById_removesUser() {
        userRepository.deleteById(savedUser.getId());
        assertThat(userRepository.findById(savedUser.getId())).isEmpty();
    }

    @Test
    void update_persistsChanges() {
        savedUser.updateRole(Role.ADMIN);
        savedUser.deactivate();
        userRepository.save(savedUser);

        User updated = userRepository.findById(savedUser.getId()).orElseThrow();
        assertThat(updated.getRole()).isEqualTo(Role.ADMIN);
        assertThat(updated.isActive()).isFalse();
    }
}
