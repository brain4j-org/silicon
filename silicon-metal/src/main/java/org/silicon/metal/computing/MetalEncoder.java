package org.silicon.metal.computing;

import org.silicon.metal.MetalObject;
import org.silicon.metal.device.MetalBuffer;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.util.Objects;
import java.util.function.Consumer;

public final class MetalEncoder implements MetalObject, AutoCloseable {

    public static final MethodHandle METAL_ENCODER_SET_BUFFER = LINKER.downcallHandle(
        LOOKUP.find("metal_encoder_set_buffer").orElse(null),
        FunctionDescriptor.ofVoid(
            ValueLayout.ADDRESS, // encoder
            ValueLayout.ADDRESS, // buffer pointer
            ValueLayout.JAVA_INT // index
        )
    );
    public static final MethodHandle METAL_ENCODER_SET_BYTES = LINKER.downcallHandle(
        LOOKUP.find("metal_encoder_set_bytes").orElse(null),
        FunctionDescriptor.ofVoid(
            ValueLayout.ADDRESS, // encoder
            ValueLayout.ADDRESS, // value
            ValueLayout.JAVA_INT, // length
            ValueLayout.JAVA_INT // index
        )
    );
    public static final MethodHandle METAL_DISPATCH = LINKER.downcallHandle(
        LOOKUP.find("metal_dispatch").orElse(null),
        FunctionDescriptor.ofVoid(ValueLayout.ADDRESS,
            ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, // grid X, grid Y, grid Z
            ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT // blockX, blockY, blockZ
        )
    );
    public static final MethodHandle METAL_END_ENCODING = LINKER.downcallHandle(
        LOOKUP.find("metal_end_encoding").orElse(null),
        FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
    );

    private final MemorySegment handle;
    private final Arena arena = Arena.ofConfined();

    public MetalEncoder(MemorySegment handle) {
        this.handle = handle;
    }

    public void setBuffer(MetalBuffer buf, int index) throws Throwable {
        METAL_ENCODER_SET_BUFFER.invokeExact(handle, buf.handle(), index);
    }

    public void setBytesRaw(MemorySegment data, long byteSize, int index) throws Throwable {
        METAL_ENCODER_SET_BYTES.invoke(handle, data, (int) byteSize, index);
    }

    public void dispatchThreads(int globalX, int globalY, int globalZ, int groupX, int groupY, int groupZ) throws Throwable {
        METAL_DISPATCH.invokeExact(handle, globalX, globalY, globalZ, groupX, groupY, groupZ);
    }

    public void endEncoding() throws Throwable {
        METAL_END_ENCODING.invokeExact(handle);
    }

    public void setBytes(byte[] data, int index) throws Throwable {
        MemorySegment seg = arena.allocate(data.length);
        seg.copyFrom(MemorySegment.ofArray(data));
        setBytesRaw(seg, data.length, index);
    }

    public void setInt(int value, int index) throws Throwable {
        MemorySegment seg = arena.allocate(ValueLayout.JAVA_INT);
        seg.set(ValueLayout.JAVA_INT, 0, value);
        setBytesRaw(seg, ValueLayout.JAVA_INT.byteSize(), index);
    }

    public void setLong(long value, int index) throws Throwable {
        MemorySegment seg = arena.allocate(ValueLayout.JAVA_LONG);
        seg.set(ValueLayout.JAVA_LONG, 0, value);
        setBytesRaw(seg, ValueLayout.JAVA_LONG.byteSize(), index);
    }

    public void setFloat(float value, int index) throws Throwable {
        MemorySegment seg = arena.allocate(ValueLayout.JAVA_FLOAT);
        seg.set(ValueLayout.JAVA_FLOAT, 0, value);
        setBytesRaw(seg, ValueLayout.JAVA_FLOAT.byteSize(), index);
    }

    public void setDouble(double value, int index) throws Throwable {
        MemorySegment seg = arena.allocate(ValueLayout.JAVA_DOUBLE);
        seg.set(ValueLayout.JAVA_DOUBLE, 0, value);
        setBytesRaw(seg, ValueLayout.JAVA_DOUBLE.byteSize(), index);
    }

    public void setShort(short value, int index) throws Throwable {
        MemorySegment seg = arena.allocate(ValueLayout.JAVA_SHORT);
        seg.set(ValueLayout.JAVA_SHORT, 0, value);
        setBytesRaw(seg, ValueLayout.JAVA_SHORT.byteSize(), index);
    }

    public void setStruct(MemoryLayout layout, Consumer<MemorySegment> writer, int index) throws Throwable {
        MemorySegment seg = arena.allocate(layout);
        writer.accept(seg);
        setBytesRaw(seg, layout.byteSize(), index);
    }

    @Override
    public void close() {
        try {
            endEncoding();
            release();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public MemorySegment handle() {
        return handle;
    }
}
