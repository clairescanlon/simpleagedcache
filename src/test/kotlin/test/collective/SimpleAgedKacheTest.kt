import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.test.*

class SimpleAgedCacheTest {
    private val empty = SimpleAgedCache()
    private val nonempty = SimpleAgedCache()

    @BeforeTest
    fun before() {
        nonempty.put("aKey", "aValue", 2000)
        nonempty.put("anotherKey", "anotherValue", 2000)
    }

    @Test
    fun isEmpty() {
        assertTrue(empty.isEmpty())
        assertFalse(nonempty.isEmpty())
    }

    @Test
    fun size() {
        assertEquals(0, empty.size())
        assertEquals(2, nonempty.size())
    }

    @Test
    fun get() {
        assertNull(empty.get("aKey"))
        assertEquals("aValue", nonempty.get("aKey"))
        assertEquals("anotherValue", nonempty.get("anotherKey"))
    }

    @Test
    fun getExpired() {
        val clock = TestClock()

        val expired = SimpleAgedCache(clock)
        expired.put("aKey", "aValue", 2000)
        expired.put("anotherKey", "anotherValue", 4000)

        clock.offset(Duration.ofMillis(3000))

        assertEquals(1, expired.size())
        assertEquals("anotherValue", expired.get("anotherKey"))
    }

    class TestClock : Clock() {
        var offset = Duration.ZERO

        override fun getZone(): ZoneId {
            return systemDefaultZone().zone
        }

        override fun withZone(zone: ZoneId): Clock {
            return offset(system(zone), offset)
        }

        override fun instant(): Instant {
            return offset(systemDefaultZone(), offset).instant()
        }

        fun offset(offset: Duration) {
            this.offset = offset
        }
    }
}

class SimpleAgedCache(private val clock: Clock, private val expirationDuration: Long, private val expirationTimeUnit: TimeUnit) {
    private val DEFAULT_CAPACITY = 16
    private val cache: Array<MutableMap<Any, ExpirableEntry<Any, Any>>> = arrayOf(mutableMapOf())
    private val lock = ReentrantReadWriteLock(true)

    fun put(key: Any, value: Any) {
        lock.writeLock().lock()
        try {
            val cache = this.cache[0]
            val entry = cache[key]
            if (entry != null) {
                entry.update(value, expirationDuration, expirationTimeUnit, clock)
            } else {
                cache[key] = ExpirableEntry(key, value, expirationDuration, expirationTimeUnit, clock)
            }
        } finally {
            lock.writeLock().unlock()
        }
    }

    fun get(key: Any): Any? {
        val cache = this.cache[0]
        val entry = cache[key] ?: return null
        if (entry.isExpired(clock.millis())) {
            cache.remove(key)
            return null
        }
        return entry.value
    }

    fun size(): Int {
        return cache[0].size
    }

    fun isEmpty(): Boolean {
        return cache[0].isEmpty()
    }

    inner class ExpirableEntry<K, V>(private val key: K, var value: V, private var expirationDuration: Long, private var expirationTimeUnit: TimeUnit, private var clock: Clock) {
        private var expirationTime: Long = 0

        init {
            update(value, expirationDuration, expirationTimeUnit, clock)
        }

        fun update(value: V, expirationDuration: Long, expirationTimeUnit: TimeUnit, clock: Clock) {
            this.value = value
            this.expirationTime = clock.millis() + expirationTimeUnit.toMillis(expirationDuration)
        }

        fun isExpired(currentTimeMillis: Long): Boolean {
            return currentTimeMillis > expirationTime
        }
    }
}
