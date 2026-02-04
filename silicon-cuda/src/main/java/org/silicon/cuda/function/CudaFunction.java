package org.silicon.cuda.function;

import org.silicon.api.SiliconException;
import org.silicon.api.function.ComputeFunction;
import org.silicon.cuda.CudaObject;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;

public record CudaFunction(MemorySegment handle) implements CudaObject, ComputeFunction {
    
    private static final MethodHandle CUDA_FUNC_MAX_THREADS = CudaObject.find(
        "cuda_function_max_threads_per_block",
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS)
    );
    
    @Override
    public int maxWorkGroupSize() {
        try {
            return (int) CUDA_FUNC_MAX_THREADS.invokeExact(handle);
        } catch (Throwable e) {
            throw new SiliconException("maxWorkGroupSize() failed", e);
        }
    }
}
