package io.collective;

import java.time.Clock;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class SimpleAgedCache<K, V> {
    private static final int DEFAULT_CAPACITY = 16;
    private final HashMap<K, V> cache;
    private final Clock clock;
    private final long expirationDuration;
    private final TimeUnit expirationTimeUnit;
    private final ReadWriteLock lock;
    private int size;

    public SimpleAgedCache(Clock clock, long expirationDuration, TimeUnit expirationTimeUnit) {
        this.clock = clock;
        this.expirationDuration = expirationDuration;
        this.expirationTimeUnit = expirationTimeUnit;
        int newHashMap;
        this.cache = new HashMap<K, V>(DEFAULT_CAPACITY);        
        this.lock = new ReentrantReadWriteLock();
        this.size = 0;
    }
/**
 * Puts the specified key-value pair into the cache. If the cache already contains the specified key, the value is updated.
 */
    public void put(K key, V value) {
        try {
            lock.writeLock().lock();
            ExpirableEntry<K, V> entry = cache.get(key); // Update the type of 'entry' variable
            if (entry != null) {
                entry.update(value, expirationDuration, expirationTimeUnit, clock);
            } else {
                cache.put(key, new ExpirableEntry<K, V>(key, value, expirationDuration, expirationTimeUnit, clock, null));
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    public boolean isEmpty() {
        try {
            lock.readLock().lock();
            return size == 0;
        } finally {
            lock.readLock().unlock();
        }
    }

    public V get(K key) {
        try {
            lock.readLock().lock();
            int index = hash(key) % cache.length;
            ExpirableEntry<K, V> entry = cache.get(key);
            if (entry != null && !entry.isExpired(clock.millis())) {
                return entry.getValue();
            }
            return null;
        } finally {
            lock.readLock().unlock();
        }
    }

    private static int hash(Object key) {
        int h = key.hashCode();
        h ^= (h >>> 20) ^ (h >>> 12);
        return h ^ (h >>> 7) ^ (h >>> 4);
    }

    private static class ExpirableEntry<K, V> {
        private final K key;
        private V value;
        private final long expirationTime;

        ExpirableEntry(K key, V value, long expirationDuration, TimeUnit expirationTimeUnit, Clock clock, ExpirableEntry<K, V> next) {
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

        void update(V newValue, long expirationDuration, TimeUnit expirationTimeUnit, Clock clock) {
            this.value = newValue;
            this.expirationTime = clock.millis() + expirationTimeUnit.toMillis(expirationDuration);
        }

        boolean isExpired(long currentTime) {
            return currentTime >= expirationTime;
        }
    }
