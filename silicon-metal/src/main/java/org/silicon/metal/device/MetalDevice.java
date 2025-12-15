package org.silicon.metal.device;

import org.silicon.device.ComputeDevice;
import org.silicon.metal.MetalObject;
import org.silicon.metal.kernel.MetalLibrary;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;

public record MetalDevice(MemorySegment handle) implements MetalObject, ComputeDevice {

    public static final MethodHandle METAL_DEVICE_NAME = LINKER.downcallHandle(
        LOOKUP.find("metal_device_name").orElse(null),
        FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS)
    );

    @Override
    public MetalContext createContext() {
        return new MetalContext(this);
    }

    public String getName() throws Throwable {
        MemorySegment nameHandle = (MemorySegment) METAL_DEVICE_NAME.invokeExact(handle);
        return nameHandle.reinterpret(Long.MAX_VALUE).getString(0);
    }

    public MetalLibrary makeLibrary(String source) throws Throwable {
        return MetalLibrary.makeLibrary(this, source);
    }

    public MetalBuffer makeBuffer(int length) throws Throwable {
        return MetalBuffer.makeBuffer(this, length);
    }
}
