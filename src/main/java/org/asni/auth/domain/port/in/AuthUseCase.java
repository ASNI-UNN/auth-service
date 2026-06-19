package org.asni.auth.domain.port.in;

import org.asni.auth.application.command.LoginCommand;
import org.asni.auth.application.result.AuthResult;
import org.asni.auth.application.result.TokenInfo;

public interface AuthUseCase {

    AuthResult login(LoginCommand command);

    void logout(String token);

    TokenInfo validateToken(String token);
}
