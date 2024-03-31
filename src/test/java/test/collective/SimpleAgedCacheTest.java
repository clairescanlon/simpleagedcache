package io.collective;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Clock;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;


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
    public void get() {
        cache.put("key", "value");
        assertEquals("value", cache.get("key"));
    }

    @Test 
    public void getExpired() {
        cache.put("key", "value", 1, TimeUnit.MILLISECONDS);
        clock.offset(clock, Duration.ofSeconds(2));
        assertNull(cache.get("key"));
        }
        

    @Test 
    public void size() {
        cache.put("key", "value");
        assertEquals(1, cache.size());
    }

    @Test 
    public void isEmpty() {
        assertTrue(cache.isEmpty());
        cache.put("key", "value");
        assertFalse(cache.isEmpty());
    }

    private static class ExpirableEntry {
    String key;
    String value;
    long expirationTime;

    ExpirableEntry(String key, String value, long expirationTime) {
        this.key = key;
        this.value = value;
        this.expirationTime = expirationTime;
    }
