package org.metal4j.kernel;

import org.metal4j.MetalObject;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;

public record MetalPipeline(MemorySegment handle) implements MetalObject {

    public static final MethodHandle METAL_MAKE_PIPELINE = LINKER.downcallHandle(
        LOOKUP.find("metal_make_pipeline").orElse(null),
        FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS)
    );

    public static MetalPipeline makePipeline(MetalFunction function) throws Throwable {
        MemorySegment ptr = (MemorySegment) METAL_MAKE_PIPELINE.invokeExact(function.handle());
        return new MetalPipeline(ptr);
    }
}
