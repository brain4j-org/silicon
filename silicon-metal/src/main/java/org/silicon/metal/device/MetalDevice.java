package org.silicon.metal.device;

import org.silicon.SiliconException;
import org.silicon.device.ComputeDevice;
import org.silicon.device.DeviceFeature;
import org.silicon.metal.MetalObject;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;

public record MetalDevice(MemorySegment handle) implements MetalObject, ComputeDevice {

    public static final MethodHandle METAL_DEVICE_NAME = MetalObject.find(
        "metal_device_name",
        FunctionDescriptor.of(
            ValueLayout.ADDRESS, // device name
            ValueLayout.ADDRESS // device pointer
        )
    );
    public static final MethodHandle METAL_FREE_NATIVE = MetalObject.find(
        "metal_free_native",
        FunctionDescriptor.ofVoid(
            ValueLayout.ADDRESS // device name
        )
    );
    public static final MethodHandle METAL_MEMORY_SIZE = MetalObject.find(
        "metal_memory_size",
        FunctionDescriptor.of(
            ValueLayout.JAVA_LONG, // memory size (int64)
            ValueLayout.ADDRESS // device pointer
        )
    );
    
    @Override
    public MetalContext createContext() {
        return new MetalContext(this);
    }

    public String name() {
        try {
            MemorySegment nameHandle = (MemorySegment) METAL_DEVICE_NAME.invokeExact(handle);
            String name = nameHandle.reinterpret(Long.MAX_VALUE).getString(0);

            METAL_FREE_NATIVE.invokeExact(nameHandle);

            return name;
        } catch (Throwable e) {
            throw new SiliconException("name() failed", e);
        }
    }

    @Override
    public String vendor() {
        return "Apple";
    }

    @Override
    public long memorySize() {
        try {
            return (long) METAL_MEMORY_SIZE.invokeExact(handle);
        } catch (Throwable e) {
            throw new SiliconException("memorySize() failed", e);
        }
    }

    @Override
    public boolean supports(DeviceFeature feature) {
        return switch (feature) {
            case FP16 -> true;
            case FP64 -> false;
        };
    }
}
