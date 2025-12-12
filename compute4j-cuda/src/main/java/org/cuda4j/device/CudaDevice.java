package org.cuda4j.device;

import org.cuda4j.CudaObject;
import org.cuda4j.context.CudaContext;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;

public record CudaDevice(MemorySegment handle, int index) implements CudaObject {
    
    public static final MethodHandle CUDA_DEVICE_NAME = LINKER.downcallHandle(
        LOOKUP.find("cuda_device_name").orElse(null),
        FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS)
    );
    
    public String getName() throws Throwable {
        MemorySegment nameHandle = (MemorySegment) CUDA_DEVICE_NAME.invokeExact(handle);
        return nameHandle.reinterpret(Long.MAX_VALUE).getString(0);
    }
    
    public CudaContext createContext() throws Throwable {
        return CudaContext.create(this);
    }
}
