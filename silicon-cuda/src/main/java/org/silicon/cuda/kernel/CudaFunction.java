package org.silicon.cuda.kernel;

import org.silicon.cuda.CudaObject;
import org.silicon.kernel.ComputeFunction;

import java.lang.foreign.MemorySegment;

public record CudaFunction(MemorySegment handle) implements CudaObject, ComputeFunction {
    @Override
    public int maxWorkGroupSize() {
        return 0; // TODO
    }
}
