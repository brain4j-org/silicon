package org.silicon.metal.state;

import org.silicon.metal.MetalObject;
import org.silicon.metal.buffer.MetalBuffer;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;

public record MetalEncoder(MemorySegment handle) implements MetalObject, AutoCloseable {

    public static final MethodHandle METAL_ENCODER_SET_BUFFER = LINKER.downcallHandle(
            LOOKUP.find("metal_encoder_set_buffer").orElse(null),
            FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT)
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

    public void setBuffer(MetalBuffer buf, int index) throws Throwable {
        METAL_ENCODER_SET_BUFFER.invokeExact(handle, buf.handle(), index);
    }

    public void dispatchThreads(int globalX, int globalY, int globalZ, int groupX, int groupY, int groupZ) throws Throwable {
        METAL_DISPATCH.invokeExact(handle, globalX, globalY, globalZ, groupX, groupY, groupZ);
    }

    public void endEncoding() throws Throwable {
        METAL_END_ENCODING.invokeExact(handle);
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
}
