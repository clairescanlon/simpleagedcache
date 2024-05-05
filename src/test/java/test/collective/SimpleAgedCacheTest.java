
import org.junit.Before;
import org.junit.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.concurrent.TimeUnit;

import static junit.framework.TestCase.*;

public class SimpleAgedCacheTest {
    SimpleAgedCache<String, String> empty = new SimpleAgedCache<>(new TestClock(), 2000, TimeUnit.MILLISECONDS);
    SimpleAgedCache<String, String> nonempty = new SimpleAgedCache<>(new TestClock(), 2000, TimeUnit.MILLISECONDS);

    @Before
    public void before() {
        nonempty.put("aKey", "aValue");
        nonempty.put("anotherKey", "anotherValue");
    }

    // Test if the cache is empty
    @Test
    public void isEmpty_emptyCache() {
        assertTrue(empty.isEmpty());
    }

    // Test if the cache is not empty
    @Test
    public void isEmpty_nonEmptyCache() {
        assertFalse(nonempty.isEmpty());
    }

    // Test the size of the cache
    @Test
    public void size() {
        assertEquals(0, empty.size());
        assertEquals(2, nonempty.size());
    }

    // Test getting a value from an empty cache
    @Test
    public void get_emptyCache() {
        assertNull(empty.get("aKey"));
    }

    // Test getting a value from a non-empty cache
    @Test
    public void get_nonEmptyCache() {
        assertEquals("aValue", nonempty.get("aKey"));
        assertEquals("anotherValue", nonempty.get("anotherKey"));
    }

    // Test getting an expired value
    @Test
    public void getExpired() {
        TestClock clock = new TestClock();

        SimpleAgedCache<String, String> expired = new SimpleAgedCache<>(clock, 2000, TimeUnit.MILLISECONDS);
        expired.put("aKey", "aValue");
        expired.put("anotherKey", "anotherValue");

        clock.offset(Duration.ofMillis(3000));

        assertEquals(1, expired.size());
        assertEquals("anotherValue", expired.get("anotherKey"));
    }

    // Test putting a value in the cache
    @Test
    public void put() {
        empty.put("newKey", "newValue");
        assertEquals("newValue", empty.get("newKey"));
    }

    // Test getting a value that doesn't exist in the cache
    @Test
    public void get_nonExistentKey() {
        assertNull(nonempty.get("nonExistentKey"));
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
}