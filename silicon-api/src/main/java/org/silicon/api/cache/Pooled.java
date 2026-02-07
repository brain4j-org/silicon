package org.silicon.api.cache;

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
