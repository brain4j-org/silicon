package org.silicon.api.cache;

import org.silicon.api.device.ComputeBuffer;

/**
 * Wrapper for a value borrowed from a {@link MemoryPool}.
 * <p>
 * Use in try-with-resources to ensure the value is returned to the pool on {@link #close()}.
 */
public final class Pooled implements AutoCloseable {

    private final MemoryPool<?> pool;
    private final Object key;
    private final ComputeBuffer value;
    private boolean released;

    Pooled(MemoryPool<?> pool, Object key, ComputeBuffer value) {
        this.pool = pool;
        this.key = key;
        this.value = value;
    }

    /**
     * Returns the pooled value.
     * @return the borrowed value
     */
    public ComputeBuffer value() {
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
