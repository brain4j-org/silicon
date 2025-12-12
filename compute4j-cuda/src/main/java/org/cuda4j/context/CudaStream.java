package org.cuda4j.context;

import org.cuda4j.CudaObject;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;

public record CudaStream(MemorySegment handle) implements CudaObject {
    
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
    
    public void sync() throws Throwable {
        int res = (int) CUDA_STREAM_SYNC.invoke(handle);
    
        if (res != 0) {
            throw new RuntimeException("cuStreamSynchronized failed: " + res);
        }
    }
    
    public boolean isCompleted() throws Throwable {
        int res = (int) CUDA_STREAM_QUERY.invoke();
        return res == 0;
    }

    public void destroy() throws Throwable {
        int res = (int) CUDA_STREAM_DESTROY.invoke(handle);

        if (res != 0) {
            throw new RuntimeException("cuStreamDestroy_v2 failed: " + res);
        }
    }
}
