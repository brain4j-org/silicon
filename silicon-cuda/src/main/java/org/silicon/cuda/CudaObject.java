package org.silicon.cuda;

import org.silicon.SiliconException;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;

public interface CudaObject {
    
    Linker LINKER = CUDA.LINKER;
    SymbolLookup LOOKUP = CUDA.LOOKUP;
    MethodHandle CUDA_RELEASE_OBJECT = LINKER.downcallHandle(
        LOOKUP.find("cuda_release_object").orElse(null),
        FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
    );
    
    default long bytesOf(byte[] array) {
        return array.length;
    }
    
    default long bytesOf(double[] array) {
        return (long) array.length * Double.BYTES;
    }
    
    default long bytesOf(float[] array) {
        return (long) array.length * Float.BYTES;
    }
    
    default long bytesOf(long[] array) {
        return (long) array.length * Long.BYTES;
    }
    
    default long bytesOf(int[] array) {
        return (long) array.length * Integer.BYTES;
    }
    
    default long bytesOf(short[] array) {
        return (long) array.length * Short.BYTES;
    }
    
    default void release() {
        try {
            CUDA_RELEASE_OBJECT.invokeExact(handle());
        } catch (Throwable e) {
            throw new SiliconException("release() failed", e);
        }
    }

    MemorySegment handle();
}