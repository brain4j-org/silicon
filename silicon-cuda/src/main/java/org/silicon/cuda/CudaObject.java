package org.silicon.cuda;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.util.Optional;

public interface CudaObject {

    Linker LINKER = Linker.nativeLinker();

    static MethodHandle find(String callName, FunctionDescriptor descriptor) {
        Optional<MemorySegment> call = CUDA.LOOKUP.find(callName);

        if (call.isEmpty()) throw new NullPointerException("%s is not present".formatted(callName));

        return LINKER.downcallHandle(call.get(), descriptor);
    }

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
}