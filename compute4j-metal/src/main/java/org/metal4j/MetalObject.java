package org.metal4j;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;

public interface MetalObject {
    
    Linker LINKER = Metal.LINKER;
    SymbolLookup LOOKUP = Metal.LOOKUP;
    MethodHandle METAL_RELEASE_OBJECT = LINKER.downcallHandle(
        LOOKUP.find("metal_release_object").orElse(null),
        FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
    );

    default void release() throws Throwable {
        METAL_RELEASE_OBJECT.invokeExact(handle());
    }
    
    MemorySegment handle();
}
