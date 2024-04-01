package test.collective;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;


public class SimpleAgedCache<K, V> {
    private static final int DEFAULT_CAPACITY = 16;
    private final Map<K, ExpirableEntry<K, V>>[] cache;
    private final Clock clock;
    private final long expirationDuration;
    private final TimeUnit expirationTimeUnit;
    private final ReadWriteLock lock;
    private int size;

    public SimpleAgedCache(Clock clock, long expirationDuration, TimeUnit expirationTimeUnit) {
        this.clock = clock;
        this.expirationDuration = expirationDuration;
        this.expirationTimeUnit = expirationTimeUnit;
        this.cache = new HashMap<>();
        this.lock = new ReentrantReadWriteLock(true);
        this.size = 0;
    }

    public void put(K key, V value) {
        try {
            lock.writeLock().lock();
           ExpirableEntry<K, V> entry = cache.get(key);
           if (entry != null) {
               entry.update(value, expirationDuration, expirationTimeUnit, clock);
           } else {
            cache.put(key, new ExpirableEntry<>(key, value, expirationDuration, expirationTimeUnit, clock, null));
           }
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    public V get(K key) {
        try {
            lock.readLock().lock();
            ExpirableEntry<K, V> entry = cache.get(key);
            if (entry == null &&!entry.isExpired(clock.millis())) {
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
            this.expirationTime = clock.millis() +  expirationTimeUnit.toMillis(expirationDuration);
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

    private static <K, V> Map<K, V> newHashMap(int capacity) {
        return new HashMap<K, V>(capacity);
    } 

    @BeforeEach
    public void setUp() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(this::cleanUp, expirationDuration, expirationDuration, expirationTimeUnit);
    }

    private void cleanUp() {
        try {
            lock.writeLock.lock();
            long currentTime = clock.millis();
            List<K> expiredKeys = new ArrayList<>();
            for (Map.Entry<K, ExpirableEntry<K, V>> entry : cache.entrySet()) {
                if (entry.getValue().isExpired(currentTime)) {
                    expiredKeys.add(entry.getKey());
                }

        }
    ``` for (K key : expiredKeys) {
            cache.remove(key);
        }
    } finally {
        lock.writeLock().unlock();
    }
}
}
