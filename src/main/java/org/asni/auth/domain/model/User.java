package org.asni.auth.domain.model;

import java.time.Instant;
import java.util.UUID;

public class User {

    private final UUID id;
    private String username;
    private String email;
    private String passwordHash;
    private Role role;
    private boolean active;
    private final Instant createdAt;
    private Instant updatedAt;

    private User(UUID id, String username, String email, String passwordHash,
                 Role role, boolean active, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static User create(String username, String email, String passwordHash, Role role) {
        Instant now = Instant.now();
        return new User(UUID.randomUUID(), username, email, passwordHash, role, true, now, now);
    }

    public static User reconstitute(UUID id, String username, String email, String passwordHash,
                                    Role role, boolean active, Instant createdAt, Instant updatedAt) {
        return new User(id, username, email, passwordHash, role, active, createdAt, updatedAt);
    }

    public void updateUsername(String username) {
        this.username = username;
        this.updatedAt = Instant.now();
    }

    public void updateEmail(String email) {
        this.email = email;
        this.updatedAt = Instant.now();
    }

    public void updateRole(Role role) {
        this.role = role;
        this.updatedAt = Instant.now();
    }

    public void changePassword(String passwordHash) {
        this.passwordHash = passwordHash;
        this.updatedAt = Instant.now();
    }

    public void deactivate() {
        this.active = false;
        this.updatedAt = Instant.now();
    }

    public void activate() {
        this.active = true;
        this.updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public Role getRole() { return role; }
    public boolean isActive() { return active; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
