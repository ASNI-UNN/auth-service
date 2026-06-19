package org.asni.auth.infrastructure.cache;

import org.asni.auth.TestcontainersConfig;
import org.asni.auth.domain.port.out.TokenBlacklistPort;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Import(TestcontainersConfig.class)
class RedisTokenBlacklistAdapterIT {

    @Autowired
    private TokenBlacklistPort tokenBlacklist;

    @Test
    void blacklist_andCheck_returnsTrue() {
        String token = "test.jwt.token." + System.nanoTime();

        assertThat(tokenBlacklist.isBlacklisted(token)).isFalse();

        tokenBlacklist.blacklist(token, 60_000);

        assertThat(tokenBlacklist.isBlacklisted(token)).isTrue();
    }

    @Test
    void tokenNotBlacklisted_returnsFalse() {
        assertThat(tokenBlacklist.isBlacklisted("never.blacklisted.token")).isFalse();
    }
}
