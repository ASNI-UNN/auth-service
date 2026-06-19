package org.asni.auth.infrastructure.cache;

import org.asni.auth.domain.port.out.TokenBlacklistPort;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
class RedisTokenBlacklistAdapter implements TokenBlacklistPort {

    private static final String PREFIX = "blacklist:";

    private final StringRedisTemplate redis;

    RedisTokenBlacklistAdapter(StringRedisTemplate redis) {
        this.redis = redis;
    }

    @Override
    public void blacklist(String token, long ttlMillis) {
        redis.opsForValue().set(PREFIX + token, "1", Duration.ofMillis(ttlMillis));
    }

    @Override
    public boolean isBlacklisted(String token) {
        return Boolean.TRUE.equals(redis.hasKey(PREFIX + token));
    }
}
