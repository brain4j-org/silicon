package org.silicon.api.device;

import org.silicon.api.BitUtils;
import org.silicon.api.kernel.ComputeQueue;
import org.silicon.api.memory.Freeable;

import java.util.ArrayList;
import java.util.List;

/**
 * Allocation arena that owns and frees compute resources.
 * <p>
 * Resources created via this arena are registered and released in {@link #close()}.
 * Intended for use with try-with-resources to simplify lifetime management.
 */
public class ComputeArena implements AutoCloseable {
    
    private final ComputeContext context;
    private final List<Freeable> retained;

    /**
     * Creates an arena associated with a compute context.
     * @param context context used to create resources
     */
    public ComputeArena(ComputeContext context) {
        this.context = context;
        this.retained = new ArrayList<>();
    }

    @Override
    public void close() {
        for (int i = retained.size() - 1; i >= 0; i--) {
            retained.get(i).free();
        }
    }

    /**
     * Registers a resource in this arena.
     * @param freeable resource to register
     * @return the same resource, for chaining
     */
    public <T extends Freeable> T retain(T freeable) {
        retained.add(freeable);
        return freeable;
    }
    
    /**
     * Creates a compute queue and registers it in the arena.
     * @return registered queue
     */
    public ComputeQueue createQueue() {
        return retain(context.createQueue());
    }

    /**
     * Allocates an uninitialized byte buffer and registers it.
     * @param size size in bytes
     * @return registered buffer
     */
    public ComputeBuffer allocateBytes(long size) {
        return retain(context.allocateBytes(size));
    }

    /**
     * Allocates and initializes a byte buffer and registers it.
     * @param data source data
     * @return registered buffer
     */
    public ComputeBuffer allocateArray(byte[] data) {
        return retain(context.allocateArray(data));
    }
    
    /**
     * Allocates and initializes a double buffer and registers it.
     * @param data source data
     * @return registered buffer
     */
    public ComputeBuffer allocateArray(double[] data) {
        return retain(context.allocateArray(data));
    }
    
    /**
     * Allocates an FP16 buffer from float data (converted to half).
     * @param data float values to convert
     * @return registered buffer
     */
    public ComputeBuffer allocateHalf(float[] data) {
        short[] result = new short[data.length];
        BitUtils.float2Half(data, result);
        return allocateArray(result);
    }
    
    /**
     * Allocates and initializes a float buffer and registers it.
     * @param data source data
     * @return registered buffer
     */
    public ComputeBuffer allocateArray(float[] data) {
        return retain(context.allocateArray(data));
    }
    
    /**
     * Allocates and initializes a long buffer and registers it.
     * @param data source data
     * @return registered buffer
     */
    public ComputeBuffer allocateArray(long[] data) {
        return retain(context.allocateArray(data));
    }

    /**
     * Allocates and initializes an int buffer and registers it.
     * @param data source data
     * @return registered buffer
     */
    public ComputeBuffer allocateArray(int[] data) {
        return retain(context.allocateArray(data));
    }

    /**
     * Allocates and initializes a short buffer and registers it.
     * @param data source data
     * @return registered buffer
     */
    public ComputeBuffer allocateArray(short[] data) {
        return retain(context.allocateArray(data));
    }
}
