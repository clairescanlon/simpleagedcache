package io.collective;

import java.time.Clock;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class SimpleAgedCache<K, V> {
    private final Map<K, ExpirableEntry<K, V>> cache;
    private final Clock clock;
    private final long expirationDuration;
    private final TimeUnit expirationTimeUnit;
    private final ReadWriteLock lock;

    public SimpleAgedCache(Clock clock, long expirationDuration, TimeUnit expirationTimeUnit) {
        this.clock = clock;
        this.expirationDuration = expirationDuration;
        this.expirationTimeUnit = expirationTimeUnit;
        this.cache = new LinkedHashMap<>();
        this.lock = new ReentrantReadWriteLock();
    }

   public SimpleAgedCache(long expirationDuration, TimeUnit expirationTimeUnit) {
    this(Clock.systemDefaultZone(), expirationDuration, expirationTimeUnit);
   }

   public void put(K key, V value) {
    try {
        lock.writeLock().lock();
        cache.put(key, new ExpirableEntry<>(key, value, expirationDuration, expirationTimeUnit, clock))
        cleanupExpiredEntries();
    } finally {
        lock.writeLock().unlock();
    }
   }
   
   public boolean isEmpty() {
    try {
        lock.readLock().lock();
        return cache.isEmpty();
    } finally {
        lock.readLock().unlock();
    }
   }

   public int size() {
    try {
        lock.readLock().lock();
        return cache.size();
    } finally {
        lock.readLock().unlock();
    }
    
   }

   public V get (K key) {
    try {
        lock.readLock().lock();
        ExpirableEntry<K, V> entry = cache.get(key);
        if (entry != null && !entry.isExpired(clock.millis())) {
            return entry.getValue();
        }
        return null;
    } finally {
        lock.readLock().unlock();
    }
   }
private void cleanupExpiredEntries() {
    cache.values().removeIf(entry -> entry.isExpired(clock.millis()));
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
