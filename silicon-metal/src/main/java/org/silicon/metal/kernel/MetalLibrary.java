package org.silicon.metal.kernel;

import org.silicon.kernel.ComputeFunction;
import org.silicon.kernel.ComputeModule;
import org.silicon.metal.MetalObject;
import org.silicon.metal.device.MetalDevice;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;

public record MetalLibrary(MemorySegment handle) implements MetalObject, ComputeModule {

    public static MetalLibrary makeLibrary(MetalDevice device, String source) throws Throwable {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment src = arena.allocateFrom(source);
            MemorySegment libPtr = (MemorySegment) METAL_CREATE_LIBRARY.invokeExact(device.handle(), src);

            if (libPtr == null) {
                throw new RuntimeException("Failed to compile Metal library");
            }

            return new MetalLibrary(libPtr);
        }
    }

    @Override
    public MetalFunction getFunction(String name) throws Throwable {
        return MetalFunction.create(this, name);
    }
}
