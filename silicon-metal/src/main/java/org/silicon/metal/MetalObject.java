package org.silicon.metal;

import org.silicon.SiliconException;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;

public interface MetalObject {
    
    Linker LINKER = Metal.LINKER;
    SymbolLookup LOOKUP = Metal.LOOKUP;
    MethodHandle METAL_RELEASE_OBJECT = LINKER.downcallHandle(
        LOOKUP.find("metal_release_object").orElse(null),
        FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
    );

    default void release() {
        try {
            METAL_RELEASE_OBJECT.invokeExact(handle());
        } catch (Throwable e) {
            throw new SiliconException("release() failed", e);
        }
    }
    
    MemorySegment handle();
}
