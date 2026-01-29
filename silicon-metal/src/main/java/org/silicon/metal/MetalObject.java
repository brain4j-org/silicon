package org.silicon.metal;

import org.silicon.SiliconException;
import org.silicon.memory.Freeable;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.util.Optional;

public interface MetalObject extends Freeable {
    
    Linker LINKER = Metal.LINKER;
    SymbolLookup LOOKUP = Metal.LOOKUP;
    MethodHandle METAL_RELEASE_OBJECT = MetalObject.find(
        "metal_release_object",
        FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
    );

    default void free() {
        try {
            METAL_RELEASE_OBJECT.invokeExact(handle());
        } catch (Throwable e) {
            throw new SiliconException("free() failed", e);
        }
    }
    
    static MethodHandle find(String callName, FunctionDescriptor descriptor) {
        Optional<MemorySegment> call = LOOKUP.find(callName);
        
        if (call.isEmpty()) throw new NullPointerException("%s is not present!".formatted(callName));
        
        return LINKER.downcallHandle(call.get(), descriptor);
    }
    
    MemorySegment handle();
}
