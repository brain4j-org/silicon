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

    public ComputeBuffer allocateArray(byte[] data) {
        return retain(context.allocateArray(data));
    }
    
    public ComputeBuffer allocateArray(double[] data) {
        return retain(context.allocateArray(data));
    }
    
    public ComputeBuffer allocateHalf(float[] data) {
        short[] result = new short[data.length];
        BitUtils.float2Half(data, result);
        return allocateArray(result);
    }
    
    public ComputeBuffer allocateArray(float[] data) {
        return retain(context.allocateArray(data));
    }
    
    public ComputeBuffer allocateArray(long[] data) {
        return retain(context.allocateArray(data));
    }

    public ComputeBuffer allocateArray(int[] data) {
        return retain(context.allocateArray(data));
    }

    public ComputeBuffer allocateArray(short[] data) {
        return retain(context.allocateArray(data));
    }
}
