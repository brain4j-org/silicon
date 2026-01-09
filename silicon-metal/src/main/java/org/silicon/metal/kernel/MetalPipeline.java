package org.silicon.metal.kernel;

import org.silicon.SiliconException;
import org.silicon.metal.MetalObject;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;

public record MetalPipeline(MemorySegment handle) implements MetalObject {

    public static final MethodHandle METAL_MAKE_PIPELINE = LINKER.downcallHandle(
        LOOKUP.find("metal_make_pipeline").orElse(null),
        FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS)
    );

    public static MetalPipeline makePipeline(MetalFunction function) {
        try {
            MemorySegment ptr = (MemorySegment) METAL_MAKE_PIPELINE.invokeExact(function.handle());

            if (ptr == null) {
                throw new IllegalArgumentException("metalMakePipeline failed!");
            }

            return new MetalPipeline(ptr);
        } catch (Throwable e) {
            throw new SiliconException("makePipeline(MetalFunction) failed", e);
        }
    }
}
