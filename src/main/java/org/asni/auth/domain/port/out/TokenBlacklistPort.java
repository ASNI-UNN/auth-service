package org.asni.auth.domain.port.out;

public interface TokenBlacklistPort {

    void blacklist(String token, long ttlMillis);

    boolean isBlacklisted(String token);
}
