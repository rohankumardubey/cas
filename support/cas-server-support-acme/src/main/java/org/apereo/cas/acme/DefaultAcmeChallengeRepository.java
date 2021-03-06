package org.apereo.cas.acme;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * This is {@link DefaultAcmeChallengeRepository}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Slf4j
public class DefaultAcmeChallengeRepository implements AcmeChallengeRepository {
    private final Cache<String, String> cache = Caffeine.newBuilder()
        .initialCapacity(100)
        .maximumSize(1000)
        .expireAfterAccess(2, TimeUnit.SECONDS)
        .build();


    @Synchronized
    @Override
    public void add(final String token, final String challenge) {
        LOGGER.debug("Adding ACME token [{}] linked to challenge [{}]", token, challenge);
        cache.put(token, challenge);
    }

    @Synchronized
    @Override
    public String get(final String token) {
        LOGGER.debug("Fetching ACME token [{}]...", token);
        return cache.getIfPresent(token);
    }
}
