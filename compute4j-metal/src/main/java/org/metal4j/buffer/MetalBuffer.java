package org.metal4j.buffer;

import org.metal4j.MetalObject;
import org.metal4j.state.MetalDevice;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public record MetalBuffer(MemorySegment handle, int length) implements MetalObject {

    public static final MethodHandle METAL_NEW_BUFFER = LINKER.downcallHandle(
        LOOKUP.find("metal_new_buffer").orElse(null),
        FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT)
    );
    public static final MethodHandle METAL_BUFFER_CONTENTS = LINKER.downcallHandle(
        LOOKUP.find("metal_buffer_contents").orElse(null),
        FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS)
    );

    public static MetalBuffer makeBuffer(MetalDevice device, int length) throws Throwable {
        MemorySegment ptr = (MemorySegment) METAL_NEW_BUFFER.invokeExact(device.handle(), length);
        return new MetalBuffer(ptr, length);
    }

    public MemorySegment getContents() throws Throwable {
        return (MemorySegment) METAL_BUFFER_CONTENTS.invokeExact(handle());
    }

    public MetalBuffer put(byte[] ref) throws Throwable {
        asByteBuffer().put(ref);
        return this;
    }

    public MetalBuffer put(float[] ref) throws Throwable {
        asByteBuffer().asFloatBuffer().put(ref);
        return this;
    }

    public MetalBuffer put(double[] ref) throws Throwable {
        asByteBuffer().asDoubleBuffer().put(ref);
        return this;
    }

    public MetalBuffer put(int[] v) throws Throwable {
        asByteBuffer().asIntBuffer().put(v);
        return this;
    }

    public MetalBuffer put(short[] ref) throws Throwable {
        asByteBuffer().asShortBuffer().put(ref);
        return this;
    }

    public MetalBuffer put(ByteBuffer ref) throws Throwable {
        asByteBuffer().put(ref);
        return this;
    }

    public MetalBuffer get(byte[] ref) throws Throwable {
        asByteBuffer().get(ref);
        return this;
    }

    public MetalBuffer get(float[] ref) throws Throwable {
        asByteBuffer().asFloatBuffer().get(ref);
        return this;
    }

    public MetalBuffer get(double[] ref) throws Throwable {
        asByteBuffer().asDoubleBuffer().get(ref);
        return this;
    }

    public MetalBuffer get(int[] ref) throws Throwable {
        asByteBuffer().asIntBuffer().get(ref);
        return this;
    }

    public MetalBuffer get(short[] ref) throws Throwable {
        asByteBuffer().asShortBuffer().get(ref);
        return this;
    }

    public ByteBuffer asByteBuffer() throws Throwable {
        return getContents()
            .reinterpret(length)
            .asByteBuffer()
            .order(ByteOrder.nativeOrder());
    }
}
