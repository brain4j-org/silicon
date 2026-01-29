package org.silicon.metal.buffer;

import org.silicon.SiliconException;
import org.silicon.metal.MetalObject;
import org.silicon.metal.computing.MetalEncoder;
import org.silicon.metal.kernel.MetalPipeline;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;

public record MetalCommandBuffer(MemorySegment handle) implements MetalObject {

    public static final MethodHandle METAL_MAKE_ENCODER = MetalObject.find(
        "metal_make_encoder",
        FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS)
    );
    public static final MethodHandle METAL_COMMIT = MetalObject.find(
        "metal_commit",
        FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
    );
    public static final MethodHandle METAL_WAIT_UNTIL_COMPLETED = MetalObject.find(
        "metal_wait_until_completed",
        FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
    );

    public MetalEncoder makeEncoder(MetalPipeline pipeline) {
        try {
            MemorySegment ptr = (MemorySegment) METAL_MAKE_ENCODER.invokeExact(handle, pipeline.handle());
           
            if (ptr == null || ptr.address() == 0) {
                throw new RuntimeException("makeComputeCommandEncoder failed!");
            }
            
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
