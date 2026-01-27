package org.silicon.metal.kernel;

import org.silicon.kernel.ComputeFunction;
import org.silicon.metal.MetalObject;

import java.lang.foreign.MemorySegment;

public final class MetalFunction implements MetalObject, ComputeFunction {

    private final MemorySegment handle;
    private final MetalPipeline pipeline;

    public MetalFunction(MemorySegment handle) {
        this.handle = handle;
        this.pipeline = makePipeline();
    }

    public MetalPipeline makePipeline() {
        return MetalPipeline.makePipeline(this);
    }

    @Override
    public MemorySegment handle() {
        return handle;
    }

    public MetalPipeline getPipeline() {
        return pipeline;
    }
}
