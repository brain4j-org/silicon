package org.silicon.metal.state;

import org.silicon.metal.MetalObject;
import org.silicon.metal.buffer.MetalCommandBuffer;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;

public record MetalCommandQueue(MemorySegment handle) implements MetalObject {

    public static final MethodHandle METAL_CREATE_COMMAND_QUEUE = LINKER.downcallHandle(
        LOOKUP.find("metal_create_command_queue").orElse(null),
        FunctionDescriptor.of(ValueLayout.ADDRESS, // return MTLCommandQueue*
            ValueLayout.ADDRESS)                   // device (MTLDevice*)
    );

    public static MetalCommandQueue makeCommandQueue(MetalDevice device) throws Throwable {
        MemorySegment queuePtr = (MemorySegment) METAL_CREATE_COMMAND_QUEUE.invokeExact(device.handle());

        if (queuePtr == null) {
            throw new RuntimeException("Failed to create Metal command queue");
        }

        return new MetalCommandQueue(queuePtr);
    }

    public MetalCommandBuffer makeCommandBuffer() throws Throwable {
        return MetalCommandBuffer.make(this);
    }
}

