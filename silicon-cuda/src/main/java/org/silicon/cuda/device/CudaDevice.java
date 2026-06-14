package org.silicon.cuda.device;

import org.silicon.api.SiliconException;
import org.silicon.api.device.ComputeDevice;
import org.silicon.api.device.DeviceFeature;
import org.silicon.cuda.CudaObject;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.nio.charset.StandardCharsets;

public record CudaDevice(MemorySegment handle, int index) implements CudaObject, ComputeDevice {

    public static final MethodHandle CUDA_DEVICE_NAME = CudaObject.find(
        "cuda_device_name",
        FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS)
    );
    public static final MethodHandle CUDA_CREATE_CONTEXT = CudaObject.find(
        "cuda_create_context",
        FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS)
    );
    public static final MethodHandle CUDA_DEVICE_MEMORY_SIZE = CudaObject.find(
        "cuda_device_memory_size",
        FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.ADDRESS)
    );
    public static final MethodHandle CUDA_DEVICE_SUPPORTS = CudaObject.find(
        "cuda_device_supports",
        FunctionDescriptor.of(
            ValueLayout.JAVA_BOOLEAN,
            ValueLayout.ADDRESS,
            ValueLayout.JAVA_INT
        )
    );
    
    @Override
    public CudaContext createContext() {
        try {
            MemorySegment ctx = (MemorySegment) CUDA_CREATE_CONTEXT.invoke(handle());

            if (ctx == null || ctx.address() == 0) {
                throw new SiliconException("Failed to create CUDA context");
            }

            return new CudaContext(ctx, this).setCurrent();
        } catch (Throwable e) {
            throw new SiliconException("createContext() failed", e);
        }
    }

    @Override
    public String name() {
        try {
            MemorySegment nameHandle = (MemorySegment) CUDA_DEVICE_NAME.invokeExact(handle);
            long maxLen = 256;
            byte[] nameBytes = new byte[(int) maxLen];
            int i = 0;
            while (i < maxLen) {
                byte b = nameHandle.get(ValueLayout.JAVA_BYTE, i);
                if (b == 0) break;
                nameBytes[i] = b;
                i++;
            }
            return new String(nameBytes, 0, i, StandardCharsets.UTF_8);
        } catch (Throwable e) {
            throw new SiliconException("name() failed", e);
        }
    }

    @Override
    public String vendor() {
        return "NVIDIA";
    }
    
    @Override
    public long memorySize() {
        try {
            return (long) CUDA_DEVICE_MEMORY_SIZE.invokeExact(handle);
        } catch (Throwable e) {
            throw new SiliconException("memorySize() failed", e);
        }
    }
    
    @Override
    public boolean supports(DeviceFeature feature) {
        try {
            return (boolean) CUDA_DEVICE_SUPPORTS.invokeExact(handle, feature.ordinal());
        } catch (Throwable e) {
            throw new SiliconException("supports(" + feature + ") failed", e);
        }
    }
}
