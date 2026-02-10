package org.silicon.api.cache;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Simple keyed pool that reuses values across allocations.
 * <p>
 * Values are grouped by key and returned via {@link Pooled#close()}.
 * <p>
 * This is useful for caching memory portions and to reduce allocations.
 */
public class MemoryPool<K extends Record, V> {

    private final Map<K, ArrayDeque<V>> free = new HashMap<>();

    /**
     * Acquires a value for the given key, reusing a cached instance if available.
     * @param key grouping key used for reuse
     * @param allocator creates a new value when no cached instance exists
     * @return a pooled wrapper that returns the value on close
     */
    public Pooled<V> acquire(K key, Supplier<V> allocator) {
        ArrayDeque<V> q = free.get(key);

        V value = (q != null && !q.isEmpty())
            ? q.pollFirst() // if there is a match get it
            : allocator.get(); // allocate the new value

        return new Pooled<>(this, key, value);
    }

    /**
     * Returns a value to the pool for reuse under the given key.
     * @param key grouping key for reuse
     * @param value value to return
     */
    void release(Object key, V value) {
        free.computeIfAbsent((K) key, k -> new ArrayDeque<>())
            .addLast(value);
    }
}
