package org.silicon.metal.device;

import org.silicon.SiliconException;
import org.silicon.backend.BufferState;
import org.silicon.computing.ComputeQueue;
import org.silicon.device.ComputeBuffer;
import org.silicon.metal.MetalObject;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Objects;

public class MetalBuffer implements MetalObject, ComputeBuffer {

    public static final MethodHandle METAL_BUFFER_CONTENTS = LINKER.downcallHandle(
        LOOKUP.find("metal_buffer_contents").orElse(null),
        FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS)
    );
    private final MemorySegment handle;
    private final MetalContext context;
    private final long size;
    private BufferState state;

    public MetalBuffer(MemorySegment handle, MetalContext context, long size) {
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
    public MetalBuffer copy() {
        if (state != BufferState.ALIVE) {
            throw new IllegalStateException("Buffer is not ALIVE and cannot be copied! Current buffer state: " + state);
        }

        MetalBuffer buffer = context.allocateBytes(size);
        copyInto(buffer);
        return buffer;
    }

    @Override
    public MetalBuffer copyInto(ComputeBuffer other) {
        if (state != BufferState.ALIVE) {
            throw new IllegalStateException("Buffer is not ALIVE! Current buffer state: " + state);
        }

        if (!(other instanceof MetalBuffer dst)) {
            throw new IllegalArgumentException("Both buffers must be Metal buffers!");
        }

        MemorySegment srcSeg = getContents().reinterpret(size);
        MemorySegment dstSeg = dst.getContents().reinterpret(size);

        MemorySegment.copy(
            srcSeg,
            0,
            dstSeg,
            0,
            size
        );

        return dst;
    }

    @Override
    public MetalBuffer copyAsync(ComputeQueue queue) {
        return copy();
    }

    @Override
    public MetalBuffer copyIntoAsync(ComputeBuffer other, ComputeQueue queue) {
        return copyInto(other);
    }

    @Override
    public void free() {
        if (state != BufferState.ALIVE) {
            throw new IllegalStateException("Buffer is not ALIVE and cannot be freed! Current buffer state: " + state);
        }

        this.state = BufferState.PENDING_FREE;
        release();
        this.state = BufferState.FREE;
    }

    @Override
    public void get(byte[] data) {
        asByteBuffer().get(data);
    }

    @Override
    public void get(double[] data) {
        asByteBuffer().asDoubleBuffer().get(data);
    }

    @Override
    public void get(float[] data) {
        asByteBuffer().asFloatBuffer().get(data);
    }

    @Override
    public void get(long[] data) {
        asByteBuffer().asLongBuffer().get(data);
    }

    @Override
    public void get(int[] data) {
        asByteBuffer().asIntBuffer().get(data);
    }

    @Override
    public void get(short[] data) {
        asByteBuffer().asShortBuffer().get(data);
    }

    public ByteBuffer asByteBuffer() {
        if (state != BufferState.ALIVE) {
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

    public MetalContext getContext() {
        return context;
    }

    public long getSize() {
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
        return "MetalBuffer[" +
            "handle=" + handle + ", " +
            "context=" + context + ", " +
            "size=" + size + ']';
    }

}
