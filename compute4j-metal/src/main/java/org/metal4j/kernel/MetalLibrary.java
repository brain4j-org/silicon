package org.metal4j.kernel;

import org.metal4j.MetalObject;
import org.metal4j.state.MetalDevice;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;

public record MetalLibrary(MemorySegment handle) implements MetalObject {

    public static final MethodHandle METAL_CREATE_LIBRARY = LINKER.downcallHandle(
        LOOKUP.find("metal_create_library").orElse(null),
        FunctionDescriptor.of(ValueLayout.ADDRESS,  // return MTLLibrary*
            ValueLayout.ADDRESS,                    // device (MTLDevice*)
            ValueLayout.ADDRESS)                    // kernel source (char*)
    );

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

    public MetalFunction makeFunction(String name) throws Throwable {
        return MetalFunction.create(this, name);
    }
}
