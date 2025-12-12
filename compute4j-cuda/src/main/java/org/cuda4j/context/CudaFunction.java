package org.cuda4j.context;

import org.cuda4j.CudaObject;
import org.cuda4j.buffer.CudaPointer;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;

public record CudaFunction(MemorySegment handle) implements CudaObject {
    
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
    
    public int launch(
        int gridX, int gridY, int gridZ,
        int blockX, int blockY, int blockZ,
        int sharedMemBytes,
        CudaStream stream,
        CudaPointer kernelParams
    ) throws Throwable {
        return (int) CUDA_LAUNCH_KERNEL.invoke(
            handle,
            gridX, gridY, gridZ,
            blockX, blockY, blockZ,
            sharedMemBytes,
            stream == null ? MemorySegment.NULL : stream.handle(),
            kernelParams == null ? MemorySegment.NULL : kernelParams.segment()
        );
    }
}
