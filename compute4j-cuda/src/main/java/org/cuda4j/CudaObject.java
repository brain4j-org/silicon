package org.cuda4j;

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
    
    default long bytesOf(int[] array) {
        return (long) array.length * Integer.BYTES;
    }
    
    default long bytesOf(float[] array) {
        return (long) array.length * Float.BYTES;
    }
    
    default long bytesOf(double[] array) {
        return (long) array.length * Double.BYTES;
    }
    
    default long bytesOf(short[] array) {
        return (long) array.length * Short.BYTES;
    }
    
    default void release() throws Throwable {
        CUDA_RELEASE_OBJECT.invokeExact(handle());
    }

    MemorySegment handle();
}