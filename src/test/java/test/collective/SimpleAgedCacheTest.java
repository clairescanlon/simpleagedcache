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
    public void
