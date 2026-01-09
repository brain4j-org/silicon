package org.silicon.opencl.device;

import org.lwjgl.opencl.CL10;
import org.silicon.backend.BufferState;
import org.silicon.computing.ComputeQueue;
import org.silicon.device.ComputeBuffer;
import org.silicon.opencl.computing.CLCommandQueue;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class CLBuffer implements ComputeBuffer {

    private final CLContext context;
    private final long handle;
    private final long size;
    private BufferState state;

    public CLBuffer(long handle, CLContext context, long size) {
        this.handle = handle;
        this.context = context;
        this.size = size;
        this.state = BufferState.ALIVE;
    }

    @Override
    public BufferState getState() {
        return state;
    }

    @Override
    public CLBuffer copy() {
        return copyAsync(null);
    }

    @Override
    public CLBuffer copyInto(ComputeBuffer other) {
        return copyIntoAsync(other, null);
    }

    @Override
    public CLBuffer copyAsync(ComputeQueue queue) {
        CLBuffer buffer = context.allocateBytes(size);
        return copyIntoAsync(buffer, queue);
    }

    @Override
    public CLBuffer copyIntoAsync(ComputeBuffer other, ComputeQueue queue) {
        if (!(other instanceof CLBuffer buffer)) {
            throw new IllegalArgumentException("Other buffer must be an OpenCL buffer!");
        }

        boolean newQueue = queue == null;
        if (newQueue) queue = context.createQueue();

        CLCommandQueue clQueue = (CLCommandQueue) queue;

        int res = CL10.clEnqueueCopyBuffer(
            clQueue.handle(), buffer.getHandle(), handle,
            0, 0, size, null, null
        );
        if (res != 0) throw new RuntimeException("clEnqueueCopyBuffer failed: " + res);

        if (newQueue) {
            clQueue.awaitCompletion();
            clQueue.release();
        }

        return buffer;
    }

    @Override
    public void free() {
        if (state != BufferState.ALIVE) {
            throw new IllegalStateException("Buffer is not ALIVE and cannot be freed! Current buffer state: " + state);
        }

        this.state = BufferState.PENDING_FREE;

        int res = CL10.clReleaseMemObject(handle);
        if (res != 0) throw new RuntimeException("clReleaseMemObject failed: " + res);

        this.state = BufferState.FREE;
    }

    @Override
    public void get(byte[] data) {
        Result result = readBuffer(size);
        result.buffer().get(data);

        result.queue().awaitCompletion();
        result.queue().release();
    }

    @Override
    public void get(double[] data) {
        long required = (long) data.length * Double.BYTES;

        Result result = readBuffer(required);
        result.buffer().asDoubleBuffer().get(data);

        result.queue().awaitCompletion();
        result.queue().release();
    }

    @Override
    public void get(float[] data) {
        long required = (long) data.length * Float.BYTES;

        Result result = readBuffer(required);
        result.buffer().asFloatBuffer().get(data);

        result.queue().awaitCompletion();
        result.queue().release();
    }

    @Override
    public void get(long[] data) {
        long required = (long) data.length * Long.BYTES;

        Result result = readBuffer(required);
        result.buffer().asLongBuffer().get(data);

        result.queue().awaitCompletion();
        result.queue().release();
    }

    @Override
    public void get(int[] data) {
        long required = (long) data.length * Integer.BYTES;

        Result result = readBuffer(required);
        result.buffer().asIntBuffer().get(data);

        result.queue().awaitCompletion();
        result.queue().release();
    }

    @Override
    public void get(short[] data) {
        long required = (long) data.length * Short.BYTES;

        Result result = readBuffer(required);
        result.buffer().asShortBuffer().get(data);

        result.queue().awaitCompletion();
        result.queue().release();
    }

    private Result readBuffer(long required) {
        if (state != BufferState.ALIVE) {
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
        if (res != 0) throw new RuntimeException("clEnqueueReadBuffer failed: " + res);

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

    @Override
    public String toString() {
        return "CLBuffer[" +
            "handle=" + handle + ", " +
            "context=" + context + ", " +
            "size=" + size + ']';
    }
}
