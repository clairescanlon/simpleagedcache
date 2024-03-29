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

   public SimpleAgedCache(long expirationDuration, TimeUnit expirationTimeUnit) {
    this(Clock.systemDefaultZone(), expirationDuration, expirationTimeUnit);
   }

   public void put(K key, V value){
    cache.put(key, new ExpirableEntry<>(key, value, expirationDuration, expirationTimeUnit, clock));
   }

   public boolean isEmpty() {
    return cache.isEmpty();
   }

   public int size() {
    return cache.size();
   }

   public V get(K key) {
    Expirableentry<K, V> entry = cache.get(key);
    if (entry != null && !entry.isExpired(clock.millis())) {
        return entry.getValue();
    }
    return null;
   }

private static class ExpirableEntry<K, V> {
    private final K key;
    private final V value;
    private final long expirationTime;

    ExpirableEntry(K key, V value, long expirationDuration, TimeUnit expirationTimeUnit, Clock clock) {
        this.key = key;
        this.value = value;
        this.expirationTime = clock.millis() + expirationTimeUnit.toMillis(expirationDuration); 
    }
    K getKey() {
        return key;
    }

    V getValue() {
        return value;
    }

    boolean isExpired(long currentTime) {
        return currentTime >= expirationTime;
    }
}