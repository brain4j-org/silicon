package org.cuda4j.device;

import org.cuda4j.CudaObject;
import org.cuda4j.context.CudaFunction;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;

public record CudaModule(MemorySegment handle) implements CudaObject {
    
    public static final MethodHandle CUDA_MODULE_UNLOAD = LINKER.downcallHandle(
        LOOKUP.find("cuda_module_unload").orElse(null),
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS)
    );
    private static final MethodHandle CUDA_MODULE_GET_FUNCTION = LINKER.downcallHandle(
        LOOKUP.find("cuda_module_get_function").orElse(null),
        FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS)
    );
    
    public CudaFunction getFunction(String name) throws Throwable {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment cName = arena.allocateFrom(name);
            MemorySegment funcHandle = (MemorySegment) CUDA_MODULE_GET_FUNCTION.invoke(handle, cName);
            
            if (funcHandle == null || funcHandle.address() == 0) {
                throw new RuntimeException("Failed to get function: " + name);
            }
            
            return new CudaFunction(funcHandle);
        }
    }
    
    public void unload() throws Throwable {
        int result = (int) CUDA_MODULE_UNLOAD.invoke(handle);
        
        if (result != 0) {
            throw new RuntimeException("Failed to unload CUDA module (error code " + result + ")");
        }
    }
}
