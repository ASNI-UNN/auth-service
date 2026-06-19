package org.asni.auth.domain.port.out;

import org.asni.auth.domain.model.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository {

    User save(User user);

    Optional<User> findById(UUID id);

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    List<User> findAll();

    void deleteById(UUID id);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
