package test.collective;

import io.collective.SimpleAgedCache;
import org.junit.Before;
import org.junit.Test;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

import static junit.framework.TestCase.*;

public class SimpleAgedCacheTest {
    SimpleAgedCache empty = new SimpleAgedCache();
    SimpleAgedCache nonempty = new SimpleAgedCache();

    @Before
    public void before() {
        nonempty.put("aKey", "aValue", 2000);
        nonempty.put("anotherKey", "anotherValue", 2000);
    }

    @Test
    public void isEmpty() {
        assertTrue(empty.isEmpty());
        assertFalse(nonempty.isEmpty());
    }

    @Test
    public void size() {
        assertEquals(0, empty.size());
        assertEquals(2, nonempty.size());
    }

    @Test
    public void get() {
        assertNull(empty.get("aKey"));
        assertEquals("aValue", nonempty.get("aKey"));
        assertEquals("anotherValue", nonempty.get("anotherKey"));
    }

    @Test
    public void getExpired() {
        TestClock clock = new TestClock();

        SimpleAgedCache expired = new SimpleAgedCache(clock);
        expired.put("aKey", "aValue", 2000);
        expired.put("anotherKey", "anotherValue", 4000);

        clock.offset(Duration.ofMillis(3000));

        assertEquals(1, expired.size());
        assertEquals("anotherValue", expired.get("anotherKey"));
    }

    static class TestClock extends Clock {
        Duration offset = Duration.ZERO;

        @Override
        public ZoneId getZone() {
            return Clock.systemDefaultZone().getZone();
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return Clock.offset(Clock.system(zone), offset);
        }

        @Override
        public Instant instant() {
            return Clock.offset(Clock.systemDefaultZone(), offset).instant();
        }

        public void offset(Duration offset) {
            this.offset = offset;
        }
    }

public static class SimpleAgedCacheTest {
    private static final int DEFAULT_CAPACITY = 16;
    private final Map<K, ExpirableEntry<K, V>>[] cache;
    private final Clock clock;
    private final long expirationDuration;
    private final TimeUnit expirationTimeUnit;
    private final ReadWriteLock lock;
    private int size;
    private Object key;

public SimpleAgedCacheTest(Clock clock, long expirationDuration, TimeUnit expirationTimeUnit) {
    this.clock = clock;
    this.expirationDuration = expirationDuration;
    this.expirationTimeUnit = expirationTimeUnit;
    this.cache = new HashMap[DEFAULT_CAPACITY];
    this.lock = new ReentrantReadWriteLock(true);
    this.size = 0;
}

/**
 * Adds the specified key-value pair to the cache. If the cache already has the specified key, the value has been updated.
 */
public.void put(K key, V value) {
    try {
        lock.writeLock(),lock();
        Map<K, ExpirableEntry<K, V>> cache = this.cache[0];
        ExpirableEntry<K, V> entry = cache.get(key);
        if (entry!= null) {
            entry.update(value, expirationDuration, expirationTimeUnit, clock, null));
        } else {
            cache.put(key, newExpirableEntry<>(key, value, expirationDuration, expirationTimeUnit, clock, null));
        }

    } finally {
        lock.writeLock().unlock();
    }
}

/**
 * Returns the value to which the specified key is mapped or if this map doesn't contain mapping for the key.
 */

 public V get(K key) {
    Map<K, ExpirableEntry<K, V>> cache = this.cache[0];
    ExpirableEntry<K, V> entry = cache.get(key);
    if (entry == null) {
        return null;
    }
    if (entry.isExpired(clock.millis())) {
        cache.remove(key);
        return null;
    }
    return entry.getValue();
 }