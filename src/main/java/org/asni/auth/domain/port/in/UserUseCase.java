package org.asni.auth.domain.port.in;

import org.asni.auth.application.command.CreateUserCommand;
import org.asni.auth.application.command.UpdateUserCommand;
import org.asni.auth.domain.model.User;

import java.util.List;
import java.util.UUID;

public interface UserUseCase {

    User createUser(CreateUserCommand command);

    User findById(UUID id);

    List<User> findAll();

    User update(UUID id, UpdateUserCommand command);

    void delete(UUID id);
}
