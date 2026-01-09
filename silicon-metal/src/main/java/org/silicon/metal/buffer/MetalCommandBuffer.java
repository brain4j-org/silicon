package org.silicon.metal.buffer;

import org.silicon.SiliconException;
import org.silicon.metal.MetalObject;
import org.silicon.metal.kernel.MetalPipeline;
import org.silicon.metal.computing.MetalCommandQueue;
import org.silicon.metal.computing.MetalEncoder;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;

public record MetalCommandBuffer(MemorySegment handle) implements MetalObject {

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

    public MetalEncoder makeEncoder(MetalPipeline pipeline) {
        try {
            MemorySegment ptr = (MemorySegment) METAL_MAKE_ENCODER.invokeExact(handle, pipeline.handle());
            return new MetalEncoder(ptr);
        } catch (Throwable e) {
            throw new SiliconException("makeEncoder(MetalPipeline) failed", e);
        }
    }

    public void commit() {
        try {
            METAL_COMMIT.invokeExact(handle);
        } catch (Throwable e) {
            throw new SiliconException("commit() failed", e);
        }
    }

    public void waitUntilCompleted() {
        try {
            METAL_WAIT_UNTIL_COMPLETED.invokeExact(handle);
        } catch (Throwable e) {
            throw new SiliconException("waitUntilCompleted() failed", e);
        }
    }
}

