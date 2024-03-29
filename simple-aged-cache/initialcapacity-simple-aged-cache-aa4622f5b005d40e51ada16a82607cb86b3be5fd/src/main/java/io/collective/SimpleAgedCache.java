package io.collective;

import java.time.Clock;

public class SimpleAgedCache {
    private final ExpirableEntry[] entries;
    private final Clock clock;
    private int size;

    public SimpleAgedCache(Clock clock) {
        this.clock = clock;
        this.entries = new ExpirableEntry[16]; // initial capacity of 16
        this.size = 0;
    }

    public SimpleAgedCache() {
        this(Clock.systemDefaultZone());
    }

    public void put(Object key, Object value, int retentionInMillis) {
        ExpirableEntry<Object, Object> entry = new ExpirableEntry<>(key, value, retentionInMillis, clock);
  
        // Check to see if array needs to be resized
        if (size== entries.length) {
            ExpirableEntry[] newEntries = new ExpirableEntry[entries.length * 2];
            System.arraycopy(entries, 0, newEntries, 0, entries.length);
            entries = newEntries;
        }
        
        //add the new entry to the array
        entries[size++] = entry;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }

    public Object get(Object key) {
        for (int i = 0; i < size; i++) {
            ExpirableEntry<Object, Object> entry = entries[i];
            if (entry !=null && entry.getKey().equals(key) && !entry.isExpired(clock.millis())) 
            {
                return entry.getValue();
            
        }
    }
}