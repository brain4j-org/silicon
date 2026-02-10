package org.silicon.opencl.device;

import org.lwjgl.opencl.CL10;
import org.silicon.api.SiliconException;
import org.silicon.api.device.ComputeBuffer;
import org.silicon.api.memory.MemoryState;
import org.silicon.opencl.computing.CLCommandQueue;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class CLBuffer implements ComputeBuffer {

    private final CLContext context;
    private final long handle;
    private final long size;
    private MemoryState state;

    public CLBuffer(long handle, CLContext context, long size) {
        this.handle = handle;
        this.context = context;
        this.size = size;
        this.state = MemoryState.ALIVE;
    }

    @Override
    public CLBuffer copy() {
        CLBuffer buffer = context.allocateBytes(size);
        return copyInto(buffer);
    }

    @Override
    public CLBuffer copyInto(ComputeBuffer other) {
        if (!(other instanceof CLBuffer buffer)) {
            throw new IllegalArgumentException("Other buffer must be an OpenCL buffer");
        }
        
        CLCommandQueue queue = context.createQueue();
        
        int res = CL10.clEnqueueCopyBuffer(
            queue.handle(), buffer.getHandle(), handle,
            0, 0, size, null, null
        );
        if (res != 0) throw new SiliconException("clEnqueueCopyBuffer failed: " + res);
        
        queue.await();
        queue.free();
        
        return buffer;
    }
    
    @Override
    public long size() {
        return size;
    }
    
    @Override
    public MemoryState state() {
        return state;
    }

    @Override
    public void free() {
        if (state != MemoryState.ALIVE) return;

        int res = CL10.clReleaseMemObject(handle);
        if (res != 0) throw new SiliconException("clReleaseMemObject failed: " + res);

        state = MemoryState.FREE;
    }

    @Override
    public void get(byte[] data) {
        Result result = readBuffer(size);
        result.buffer().get(data);

        result.queue().await();
        result.queue().free();
    }

    @Override
    public void get(double[] data) {
        long required = (long) data.length * Double.BYTES;

        Result result = readBuffer(required);
        result.buffer().asDoubleBuffer().get(data);

        result.queue().await();
        result.queue().free();
    }

    @Override
    public void get(float[] data) {
        long required = (long) data.length * Float.BYTES;

        Result result = readBuffer(required);
        result.buffer().asFloatBuffer().get(data);

        result.queue().await();
        result.queue().free();
    }

    @Override
    public void get(long[] data) {
        long required = (long) data.length * Long.BYTES;

        Result result = readBuffer(required);
        result.buffer().asLongBuffer().get(data);

        result.queue().await();
        result.queue().free();
    }

    @Override
    public void get(int[] data) {
        long required = (long) data.length * Integer.BYTES;

        Result result = readBuffer(required);
        result.buffer().asIntBuffer().get(data);

        result.queue().await();
        result.queue().free();
    }

    @Override
    public void get(short[] data) {
        long required = (long) data.length * Short.BYTES;

        Result result = readBuffer(required);
        result.buffer().asShortBuffer().get(data);

        result.queue().await();
        result.queue().free();
    }

    private Result readBuffer(long required) {
        if (state != MemoryState.ALIVE) {
            throw new IllegalStateException("Buffer is not ALIVE! Current buffer state: " + state);
        }

        if (required > size) {
            throw new IllegalArgumentException("Requested read of " + required + " bytes, but buffer size is " + size);
        }

        CLCommandQueue queue = context.createQueue();
        ByteBuffer buffer = ByteBuffer
            .allocateDirect((int) required)
            .order(ByteOrder.nativeOrder());

        int res = CL10.clEnqueueReadBuffer(queue.handle(), handle, true, 0, buffer, null, null);
        if (res != 0) throw new SiliconException("clEnqueueReadBuffer failed: " + res);

        return new Result(queue, buffer);
    }

    private record Result(CLCommandQueue queue, ByteBuffer buffer) {
    }

    public long getHandle() {
        return handle;
    }

    public CLContext getContext() {
        return context;
    }

    public long getSize() {
        return size;
    }
}
