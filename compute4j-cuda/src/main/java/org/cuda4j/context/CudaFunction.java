package org.cuda4j.context;

import org.compute4j.kernel.ComputeFunction;
import org.cuda4j.CudaObject;

import java.lang.foreign.MemorySegment;

public record CudaFunction(MemorySegment handle) implements CudaObject, ComputeFunction {
}
