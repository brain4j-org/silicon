package org.silicon.metal.device;

import org.silicon.api.SiliconException;
import org.silicon.api.memory.MemoryState;
import org.silicon.api.kernel.ComputeQueue;
import org.silicon.api.device.ComputeBuffer;
import org.silicon.metal.MetalObject;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class MetalBuffer implements MetalObject, ComputeBuffer {

    public static final MethodHandle METAL_BUFFER_CONTENTS = MetalObject.find(
        "metal_buffer_contents",
        FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS)
    );

    private final MemorySegment handle;
    private final MetalContext context;
    private final long size;
    private MemoryState state;

    public MetalBuffer(MemorySegment handle, MetalContext context, long size) {
        this.handle = handle;
        this.context = context;
        this.size = size;
        this.state = MemoryState.ALIVE;
    }

    @Override
    public MetalBuffer copy() {
        ensureAlive();

        MetalBuffer buffer = context.allocateBytes(size);
        copyInto(buffer);
        return buffer;
    }

    @Override
    public MetalBuffer copyInto(ComputeBuffer other) {
        if (state != MemoryState.ALIVE) {
            throw new IllegalStateException("Buffer is not ALIVE! Current buffer state: " + state);
        }

        if (!(other instanceof MetalBuffer dst)) {
            throw new IllegalArgumentException("Both buffers must be Metal buffers");
        }

        MemorySegment srcSeg = getContents().reinterpret(size);
        MemorySegment dstSeg = dst.getContents().reinterpret(size);

        MemorySegment.copy(srcSeg, 0, dstSeg, 0, size);

        return dst;
    }
    
    @Override
    public MemoryState state() {
        return state;
    }

    @Override
    public void free() {
        if (!isAlive()) return;

        try {
            METAL_RELEASE_OBJECT.invokeExact(handle);
            state = MemoryState.FREE;
        } catch (Throwable t) {
            throw new SiliconException("free() failed", t);
        }
    }

    @Override
    public void get(byte[] data) {
        long bytes = data.length;
        checkData(bytes);
        get(MemorySegment.ofArray(data), bytes);
    }

    @Override
    public void get(double[] data) {
        long bytes = data.length * 8L;
        checkData(bytes);
        get(MemorySegment.ofArray(data), bytes);
    }

    @Override
    public void get(float[] data) {
        long bytes = data.length * 4L;
        checkData(bytes);
        get(MemorySegment.ofArray(data), bytes);
    }

    @Override
    public void get(long[] data) {
        long bytes = data.length * 8L;
        checkData(bytes);
        get(MemorySegment.ofArray(data), bytes);
    }

    @Override
    public void get(int[] data) {
        long bytes = data.length * 4L;
        checkData(bytes);
        get(MemorySegment.ofArray(data), bytes);
    }

    @Override
    public void get(short[] data) {
        long bytes = data.length * 2L;
        checkData(bytes);
        get(MemorySegment.ofArray(data), bytes);
    }

    private void checkData(long bytes) {
        ensureAlive();

        if (bytes > size) {
            throw new IllegalArgumentException("Destination array too large");
        }
    }

    private void get(MemorySegment dst, long bytes) {
        MemorySegment src = getContents().reinterpret(size);
        MemorySegment.copy(src, 0, dst, 0, bytes);
    }

    public ByteBuffer asByteBuffer() {
        if (state != MemoryState.ALIVE) {
            throw new IllegalStateException("Buffer is not ALIVE! Current buffer state: " + state);
        }

        return getContents()
            .reinterpret(size)
            .asByteBuffer()
            .order(ByteOrder.nativeOrder());
    }

    @Override
    public MemorySegment handle() {
        return handle;
    }

    public MetalContext context() {
        return context;
    }

    public long size() {
        return size;
    }

    public MemorySegment getContents() {
        try {
            return (MemorySegment) METAL_BUFFER_CONTENTS.invokeExact(handle());
        } catch (Throwable e) {
            throw new SiliconException("getContents() failed", e);
        }
    }

    @Override
    public String toString() {
        return "MetalBuffer{" +
            "handle=" + handle +
            ", context=" + context +
            ", size=" + size +
            ", state=" + state +
            '}';
    }
}
