package org.metal4j.state;

import org.metal4j.MetalObject;
import org.metal4j.buffer.MetalBuffer;
import org.metal4j.kernel.MetalLibrary;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;

public record MetalDevice(MemorySegment handle) implements MetalObject {

    public static final MethodHandle METAL_DEVICE_NAME = LINKER.downcallHandle(
        LOOKUP.find("metal_device_name").orElse(null),
        FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS)
    );

    public String getName() throws Throwable {
        MemorySegment nameHandle = (MemorySegment) METAL_DEVICE_NAME.invokeExact(handle);
        return nameHandle.reinterpret(Long.MAX_VALUE).getString(0);
    }

    public MetalLibrary makeLibrary(String source) throws Throwable {
        return MetalLibrary.makeLibrary(this, source);
    }

    public MetalCommandQueue makeCommandQueue() throws Throwable {
        return MetalCommandQueue.makeCommandQueue(this);
    }

    public MetalBuffer makeBuffer(int length) throws Throwable {
        return MetalBuffer.makeBuffer(this, length);
    }
}
