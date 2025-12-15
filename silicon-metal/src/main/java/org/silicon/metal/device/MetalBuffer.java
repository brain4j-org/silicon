package org.silicon.metal.device;

import org.silicon.computing.ComputeQueue;
import org.silicon.device.ComputeBuffer;
import org.silicon.metal.MetalObject;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public record MetalBuffer(MemorySegment handle, MetalContext context, long size) implements MetalObject, ComputeBuffer {

    public static final MethodHandle METAL_BUFFER_CONTENTS = LINKER.downcallHandle(
        LOOKUP.find("metal_buffer_contents").orElse(null),
        FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS)
    );

    public MemorySegment getContents() throws Throwable {
        return (MemorySegment) METAL_BUFFER_CONTENTS.invokeExact(handle());
    }

    @Override
    public MetalBuffer copy() throws Throwable {
        MetalBuffer buffer = context.allocateBytes(size);
        copyInto(buffer);
        return buffer;
    }

    @Override
    public MetalBuffer copyInto(ComputeBuffer other) throws Throwable {
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
    public MetalBuffer copyAsync(ComputeQueue queue) throws Throwable {
        return copy();
    }

    @Override
    public MetalBuffer copyIntoAsync(ComputeBuffer other, ComputeQueue queue) throws Throwable {
        return copyInto(other);
    }

    @Override
    public void free() throws Throwable {
        release();
    }

    @Override
    public void get(byte[] data) throws Throwable {
        asByteBuffer().get(data);
    }

    @Override
    public void get(double[] data) throws Throwable {
        asByteBuffer().asDoubleBuffer().get(data);
    }

    @Override
    public void get(float[] data) throws Throwable {
        asByteBuffer().asFloatBuffer().get(data);
    }

    @Override
    public void get(long[] data) throws Throwable {
        asByteBuffer().asLongBuffer().get(data);
    }

    @Override
    public void get(int[] data) throws Throwable {
        asByteBuffer().asIntBuffer().get(data);
    }

    @Override
    public void get(short[] data) throws Throwable {
        asByteBuffer().asShortBuffer().get(data);
    }

    public ByteBuffer asByteBuffer() throws Throwable {
        return getContents()
            .reinterpret(size)
            .asByteBuffer()
            .order(ByteOrder.nativeOrder());
    }
}
