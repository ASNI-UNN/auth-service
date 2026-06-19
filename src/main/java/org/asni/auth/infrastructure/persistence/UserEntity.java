package org.asni.auth.infrastructure.persistence;

import jakarta.persistence.*;
import org.asni.auth.domain.model.Role;
import org.asni.auth.domain.model.User;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users")
class UserEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected UserEntity() {}

    static UserEntity fromDomain(User user) {
        UserEntity entity = new UserEntity();
        entity.id = user.getId();
        entity.username = user.getUsername();
        entity.email = user.getEmail();
        entity.passwordHash = user.getPasswordHash();
        entity.role = user.getRole();
        entity.active = user.isActive();
        entity.createdAt = user.getCreatedAt();
        entity.updatedAt = user.getUpdatedAt();
        return entity;
    }

    User toDomain() {
        return User.reconstitute(id, username, email, passwordHash, role, active, createdAt, updatedAt);
    }
}
