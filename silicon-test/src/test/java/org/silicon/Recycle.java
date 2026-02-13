package org.silicon;

import org.silicon.api.Silicon;
import org.silicon.api.cache.MemoryPool;
import org.silicon.api.cache.Pooled;
import org.silicon.api.device.ComputeBuffer;
import org.silicon.api.device.ComputeContext;
import org.silicon.api.device.ComputeDevice;

import java.util.function.Supplier;

public class Recycle {
    public static void main(String[] args) {
        ComputeDevice device = Silicon.createDevice();
        ComputeContext context = device.createContext();

        // pools require RECORDS as KEYS
        MemoryPool<TensorKey> pool = context.createPool();
        TensorKey key = new TensorKey(1, 2, 3);

        Supplier<ComputeBuffer> allocator = () -> context.allocateBytes(key.size());

        // searches for an object with the same descriptor
        // if no match is found then it creates the object through the allocator
        try (Pooled pooled = pool.acquire(key, allocator)) {
            ComputeBuffer buffer = pooled.value();
            Tensor tensor = new Tensor(buffer, key.shape);
            // ... stuff ...
        }
    }

    static final class Tensor {

        private final ComputeBuffer buffer;
        private final int[] shape;

        public Tensor(ComputeBuffer buffer, int[] shape) {
            this.buffer = buffer;
            this.shape = shape;
        }
    }

    public record TensorKey(int... shape) {
        long size() {
            long prod = 1;
            for (int x : shape) prod *= x;
            return prod * Integer.BYTES;
        }
    }
}
