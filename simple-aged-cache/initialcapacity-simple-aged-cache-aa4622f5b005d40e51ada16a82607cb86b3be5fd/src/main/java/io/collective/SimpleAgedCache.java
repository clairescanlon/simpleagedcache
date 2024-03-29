package io.collective;

import java.time.Clock;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class SimpleAgedCache<K,V> {
    private final Map<K, ExpirableEntry<K,V>> cache;
    private final Clock clock;
    private final long expirationDuration;
    private final TimeUnit expirationTimeUnit;

    public SimpleAgedCache(Clock clock, long expirationDuration, TimeUnit expirationTimeUnit) {
        this.clock = clock;
        this.expirationDuration = expirationDuration;
        this.expirationTimeUnit = expirationTimeUnit;
        this.cache = new LinkedHashMap<>();
    }

    public SimpleAgedCache(long expirationDuration, TimeUnit expirationTimeUnit) {
        this(Clock.systemDefaultZone(), expirationDuration, expirationTimeUnit);
    }

   