package org.silicon.device;

import org.silicon.BitUtils;
import org.silicon.computing.ComputeQueue;
import org.silicon.memory.Freeable;

import java.util.ArrayList;
import java.util.List;

public class ComputeArena implements AutoCloseable {
    
    private final ComputeContext context;
    private final List<Freeable> retained;

    public ComputeArena(ComputeContext context) {
        this.context = context;
        this.retained = new ArrayList<>();
    }

    @Override
    public void close() {
        for (Freeable object : retained) {
            object.free();
        }
    }

    public <T extends Freeable> T retain(T buffer) {
        retained.add(buffer);
        return buffer;
    }
    
    public ComputeQueue createQueue() {
        return retain(context.createQueue());
    }

    public ComputeBuffer allocateBytes(long size) {
        return retain(context.allocateBytes(size));
    }

    public ComputeBuffer allocateArray(byte[] data, long size) {
        return retain(context.allocateArray(data, size));
    }

    public ComputeBuffer allocateArray(byte[] data, long size, ComputeQueue queue) {
        return retain(context.allocateArray(data, size, queue));
    }

    public ComputeBuffer allocateArray(double[] data, long size) {
        return retain(context.allocateArray(data, size));
    }

    public ComputeBuffer allocateArray(double[] data, long size, ComputeQueue queue) {
        return retain(context.allocateArray(data, size, queue));
    }

    public ComputeBuffer allocateArray(float[] data, long size) {
        return retain(context.allocateArray(data, size));
    }

    public ComputeBuffer allocateArray(float[] data, long size, ComputeQueue queue) {
        return retain(context.allocateArray(data, size, queue));
    }

    public ComputeBuffer allocateArray(long[] data, long size) {
        return retain(context.allocateArray(data, size));
    }

    public ComputeBuffer allocateArray(long[] data, long size, ComputeQueue queue) {
        return retain(context.allocateArray(data, size, queue));
    }

    public ComputeBuffer allocateArray(int[] data, long size) {
        return retain(context.allocateArray(data, size));
    }

    public ComputeBuffer allocateArray(int[] data, long size, ComputeQueue queue) {
        return retain(context.allocateArray(data, size, queue));
    }

    public ComputeBuffer allocateArray(short[] data, long size) {
        return retain(context.allocateArray(data, size));
    }

    public ComputeBuffer allocateArray(short[] data, long size, ComputeQueue queue) {
        return retain(context.allocateArray(data, size, queue));
    }

    public ComputeBuffer allocateArray(byte[] data) {
        return allocateArray(data, data.length);
    }

    public ComputeBuffer allocateArray(byte[] data, ComputeQueue queue) {
        return allocateArray(data, data.length, queue);
    }

    public ComputeBuffer allocateArray(double[] data) {
        return allocateArray(data, data.length * 8L);
    }

    public ComputeBuffer allocateArray(double[] data, ComputeQueue queue) {
        return allocateArray(data, data.length * 8L, queue);
    }
    
    public ComputeBuffer allocateHalf(float[] data) {
        short[] result = new short[data.length];
        BitUtils.float2Half(data, result);
        return allocateArray(result);
    }
    
    public ComputeBuffer allocateArray(float[] data) {
        return allocateArray(data, data.length * 4L);
    }

    public ComputeBuffer allocateArray(float[] data, ComputeQueue queue) {
        return allocateArray(data, data.length * 4L, queue);
    }

    public ComputeBuffer allocateArray(long[] data) {
        return allocateArray(data, data.length * 8L);
    }

    public ComputeBuffer allocateArray(long[] data, ComputeQueue queue) {
        return allocateArray(data, data.length * 8L, queue);
    }

    public ComputeBuffer allocateArray(int[] data) {
        return allocateArray(data, data.length * 4L);
    }

    public ComputeBuffer allocateArray(int[] data, ComputeQueue queue) {
        return allocateArray(data, data.length * 4L, queue);
    }

    public ComputeBuffer allocateArray(short[] data) {
        return allocateArray(data, data.length * 2L);
    }

    public ComputeBuffer allocateArray(short[] data, ComputeQueue queue) {
        return allocateArray(data, data.length * 2L, queue);
    }
}
