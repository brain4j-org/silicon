package org.metal4j.buffer;

import org.metal4j.MetalObject;
import org.metal4j.kernel.MetalPipeline;
import org.metal4j.state.MetalCommandQueue;
import org.metal4j.state.MetalEncoder;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;

public record MetalCommandBuffer(MemorySegment handle) implements MetalObject {

    public static final MethodHandle METAL_CREATE_COMMAND_BUFFER = LINKER.downcallHandle(
        LOOKUP.find("metal_create_command_buffer").orElse(null),
        FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS)
    );
    public static final MethodHandle METAL_MAKE_ENCODER = LINKER.downcallHandle(
        LOOKUP.find("metal_make_encoder").orElse(null),
        FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS)
    );
    public static final MethodHandle METAL_COMMIT = LINKER.downcallHandle(
        LOOKUP.find("metal_commit").orElse(null),
        FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
    );
    public static final MethodHandle METAL_WAIT_UNTIL_COMPLETED = LINKER.downcallHandle(
        LOOKUP.find("metal_wait_until_completed").orElse(null),
        FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
    );

    public static MetalCommandBuffer make(MetalCommandQueue queue) throws Throwable {
        MemorySegment ptr = (MemorySegment) METAL_CREATE_COMMAND_BUFFER.invokeExact(queue.handle());
        return new MetalCommandBuffer(ptr);
    }

    public MetalEncoder makeEncoder(MetalPipeline pipeline) throws Throwable {
        MemorySegment ptr = (MemorySegment) METAL_MAKE_ENCODER.invokeExact(handle(), pipeline.handle());
        return new MetalEncoder(ptr);
    }

    public void commit() throws Throwable {
        METAL_COMMIT.invokeExact(handle);
    }

    public void waitUntilCompleted() throws Throwable {
        METAL_WAIT_UNTIL_COMPLETED.invokeExact(handle);
    }
}

