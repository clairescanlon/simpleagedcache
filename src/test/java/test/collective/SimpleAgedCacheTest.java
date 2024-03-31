package io.collective;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


/**
 * A simple implementation of an aged cache.
 */
public class SimpleAgedCache {
    private SimpleAgedCache<String, String> cache;
    private Clock clock;

    @BeforeEach
    public void setup() {
       clock= Clock.systemDefaultZone();
       cache = new SimpleAgedCache(clock, 1, TimeUnit.MINUTES);
    }

    @Test
    public void testPut() {
        cache.put("key", "value");
        assertEquals("value", cache.get("key"));
    }

@Test 
public void testPutUpdatesExpirationTime() {
    Instant now = clock.instant();
    cache.put("key", "value");
    ExpirableEntry entry = cache.get("key");
    long expirationTime = entry.expirationTime;
    assertTrue(now.plus(Duration.ofMinutes(1)).isAfter(Instant.ofEpochMilli(expirationTime)));
    }

@Test 
public void testGetReturnsNullForExpiredKey() {
    cache.put("key", "value", 1, TimeUnit.MILLISECONDS);
    clock.offset(clock, Duration.ofSeconds(2));
    assertNull(cache.get("key"));
    }

@Test 
public void testIsEmpty() {
    assertTrue(cache.isEmpty());
    cache.put("key", "value");
    assertFalse(cache.isEmpty());
    }
}