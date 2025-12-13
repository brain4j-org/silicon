package org.cuda4j.context;

import org.compute4j.computing.ComputeArgs;
import org.compute4j.computing.ComputeQueue;
import org.compute4j.computing.ComputeSize;
import org.compute4j.kernel.ComputeFunction;
import org.cuda4j.CudaObject;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;

public record CudaStream(MemorySegment handle) implements CudaObject, ComputeQueue {
    
    public static final MethodHandle CUDA_STREAM_DESTROY = LINKER.downcallHandle(
        LOOKUP.find("cuda_stream_destroy").orElse(null),
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS)
    );
    public static final MethodHandle CUDA_STREAM_SYNC = LINKER.downcallHandle(
        LOOKUP.find("cuda_stream_sync").orElse(null),
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS)
    );
    public static final MethodHandle CUDA_STREAM_QUERY = LINKER.downcallHandle(
        LOOKUP.find("cuda_stream_query").orElse(null),
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS)
    );
    private static final MethodHandle CUDA_LAUNCH_KERNEL = LINKER.downcallHandle(
        LOOKUP.find("cuda_launch_kernel").orElse(null),
        FunctionDescriptor.of(
            ValueLayout.JAVA_INT, // return
            ValueLayout.ADDRESS, // function
            ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, // grid
            ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, // block
            ValueLayout.JAVA_INT, // shared mem
            ValueLayout.ADDRESS, // stream
            ValueLayout.ADDRESS // kernel params
        )
    );
    
    public void destroy() throws Throwable {
        int res = (int) CUDA_STREAM_DESTROY.invoke(handle);

        if (res != 0) {
            throw new RuntimeException("cuStreamDestroy_v2 failed: " + res);
        }
    }
    
    @Override
    public void dispatch(ComputeFunction function, ComputeSize globalSize, ComputeSize groupSize, ComputeArgs args) {
        // TODO
    }
    
    @Override
    public void awaitCompletion() throws Throwable {
        int res = (int) CUDA_STREAM_SYNC.invoke(handle);
        
        if (res != 0) {
            throw new RuntimeException("cuStreamSynchronized failed: " + res);
        }
    }
    
    @Override
    public boolean isCompleted() throws Throwable {
        int res = (int) CUDA_STREAM_QUERY.invoke();
        return res == 0;
    }
}
