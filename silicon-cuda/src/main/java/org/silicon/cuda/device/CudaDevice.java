package org.silicon.cuda.device;

import org.silicon.SiliconException;
import org.silicon.cuda.CudaObject;
import org.silicon.device.ComputeDevice;
import org.silicon.device.DeviceFeature;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;

public record CudaDevice(MemorySegment handle, int index) implements CudaObject, ComputeDevice {
    
    public static final MethodHandle CUDA_DEVICE_NAME = LINKER.downcallHandle(
        LOOKUP.find("cuda_device_name").orElse(null),
        FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS)
    );
    public static final MethodHandle CUDA_CREATE_CONTEXT = LINKER.downcallHandle(
        LOOKUP.find("cuda_create_context").orElse(null),
        FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS)
    );
    
    @Override
    public CudaContext createContext() {
        try {
            MemorySegment ctx = (MemorySegment) CUDA_CREATE_CONTEXT.invoke(handle());

            if (ctx == null || ctx.address() == 0) {
                throw new RuntimeException("Failed to create CUDA context");
            }

            return new CudaContext(ctx, this).setCurrent();
        } catch (Throwable e){
            throw new SiliconException("createContext() failed", e);
        }
    }
    
    @Override
    public String name() {
        try {
            MemorySegment nameHandle = (MemorySegment) CUDA_DEVICE_NAME.invokeExact(handle);
            return nameHandle.reinterpret(Long.MAX_VALUE).getString(0);
        } catch (Throwable e) {
            throw new SiliconException("getName() failed", e);
        }
    }

    @Override
    public String vendor() {
        return ""; // TODO
    }

    @Override
    public long memorySize() {
        return 0; // TODO
    }

    @Override
    public boolean supports(DeviceFeature feature) {
        return false; // TODO
    }

}
