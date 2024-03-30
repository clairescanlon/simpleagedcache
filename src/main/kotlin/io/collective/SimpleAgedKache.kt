package io.collective

import java.time.Clock
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantReadWriteLock

class SimpleAgedKache<K, V>(
    private val clock: Clock,
    private val expirationDuration: Long, 
    private val expirationTimeUnit: TimeUnit
    ) {
    private val cache = arrayOfNulls<ExpirableEntry<K,V>?>(DEFAULT_CAPACITY)
    private val lock = ReentrantReadWriteLock()
    private var size = 0

    fun put(key: K, value: V) {
        lock.writeLock().lock()
        try {
            val index = hash(key) % cache.size
            var entry = cache[index]
            while (entry != null) {
                if (entry.key == key) {
                    entry.update(value, expirationDuration, expirationTimeUnit, clock)
                    return
                }
                entry = entry.next
            }
            cache[index] = ExpirableEntry(key, value, expirationDuration, expirationTimeUnit, clock, cache[index])
            size++
        } finally {
            lock.writeLock().unlock()
        }
    }

fun isEmpty(): Boolean {
    lock.readLock().lock()
    try {
        return size == 0
    } finally {
        lock.readLock().unlock()
    }
}

fun size(): Int{
    lock.readLock().lock()
    try {
        return size
    } finally {
        lock.readLock().unlock()
    }
}

fun get(key: K): V? {
    lock.readLock().lock()
    try {
        val index = hash(key) % cache.size
        var entry = cache[index]
        while (entry != null) {
            if (entry.key == key && !entry.isExpired(clock.millis())) {
                return entry.value
            }
            entry = entry.next
        }
        return null
    } finally {
        lock.readLock().unlock()
    }
}

private fun hash(key: K): Int {
    return key.hashCode() % cache.size
}

private inner class ExpirableEntry<K, V> {
    val key: K,
    var value: V,
    expirationDuration: Long, 
    expirationTimeUnit: TimeUnit,
    clock: Clock,
    var next: ExpirableEntry<K,V>?
) {
    private val expirationTime = clock.millis() + expirationTimeUnit.toMillis(expirationDuration)

    fun update(newValue: V, expirationDuration: Long, expirationTimeUnit: TimeUnit, clock: Clock) {
        value = newValue
        expirationTime = clock.millis() + expirationTimeUnit.toMillis(expirationDuration)
    }

    fun isExpired(currentTime: Long): Boolean {
        return currentTime >= expirationTime
    }
}

companion object {
    private const val DEFAULT_CAPACITY = 16
}

}

