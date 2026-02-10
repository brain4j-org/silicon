package org.silicon.api.cache;

/**
 * Wrapper for a value borrowed from a {@link MemoryPool}.
 * <p>
 * Use in try-with-resources to ensure the value is returned to the pool on {@link #close()}.
 */
public final class Pooled<V> implements AutoCloseable {

    private final MemoryPool<?, V> pool;
    private final Object key;
    private final V value;
    private boolean released;

    Pooled(MemoryPool<?, V> pool, Object key, V value) {
        this.pool = pool;
        this.key = key;
        this.value = value;
    }

    /**
     * Returns the pooled value.
     * @return the borrowed value
     */
    public V value() {
        return value;
    }

    @Override
    public void close() {
        if (!released) {
            released = true;
            pool.release(key, value);
        }
    }
}
