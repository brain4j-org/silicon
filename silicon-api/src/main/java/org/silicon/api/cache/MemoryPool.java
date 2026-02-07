package org.silicon.api.cache;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class MemoryPool<K extends Record, V> {

    private final Map<K, ArrayDeque<V>> free = new HashMap<>();

    public Pooled<V> acquire(K key, Supplier<V> allocator) {
        ArrayDeque<V> q = free.get(key);

        V value = (q != null && !q.isEmpty())
            ? q.pollFirst() // if there is a match get it
            : allocator.get(); // allocate the new value

        return new Pooled<>(this, key, value);
    }

    void release(Object key, V value) {
        free.computeIfAbsent((K) key, k -> new ArrayDeque<>())
            .addLast(value);
    }
}

