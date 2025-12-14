package org.silicon.cuda.context;

import org.silicon.kernel.ComputeFunction;
import org.silicon.cuda.CudaObject;

import java.lang.foreign.MemorySegment;

public record CudaFunction(MemorySegment handle) implements CudaObject, ComputeFunction {
}
