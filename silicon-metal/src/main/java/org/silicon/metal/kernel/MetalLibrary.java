package org.silicon.metal.kernel;

import org.silicon.kernel.ComputeModule;
import org.silicon.metal.MetalObject;

import java.lang.foreign.MemorySegment;

public record MetalLibrary(MemorySegment handle) implements MetalObject, ComputeModule {

    @Override
    public MetalFunction getFunction(String name) {
        return MetalFunction.create(this, name);
    }
}
