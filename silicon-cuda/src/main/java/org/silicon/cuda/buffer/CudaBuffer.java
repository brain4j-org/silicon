package org.silicon.cuda.buffer;

import org.silicon.computing.ComputeQueue;
import org.silicon.device.ComputeBuffer;
import org.silicon.cuda.CudaObject;
import org.silicon.cuda.context.CudaStream;
import org.silicon.cuda.device.CudaDevice;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Array;
import java.util.Map;

public record CudaBuffer(CudaDevice device, MemorySegment handle, long size) implements CudaObject, ComputeBuffer {

    private record HostType(ValueLayout layout, long elementSize) {}

    public static final MethodHandle CUDA_BUFFER_PTR = LINKER.downcallHandle(
        LOOKUP.find("cuda_buffer_ptr").orElse(null),
        FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.ADDRESS)
    );
    public static final MethodHandle CUDA_MEM_FREE = LINKER.downcallHandle(
        LOOKUP.find("cuda_mem_free").orElse(null),
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS)
    );
    public static final MethodHandle CUDA_MEMCPY_HTOD = LINKER.downcallHandle(
        LOOKUP.find("cuda_memcpy_htod").orElse(null),
        FunctionDescriptor.of(ValueLayout.JAVA_INT,
            ValueLayout.ADDRESS, // buffer pointer
            ValueLayout.ADDRESS, // host pointer
            ValueLayout.JAVA_LONG) // size
    );
    public static final MethodHandle CUDA_MEMCPY_DTOH = LINKER.downcallHandle(
        LOOKUP.find("cuda_memcpy_dtoh").orElse(null),
        FunctionDescriptor.of(ValueLayout.JAVA_INT,
            ValueLayout.ADDRESS, // host pointer
            ValueLayout.ADDRESS, // buffer pointer
            ValueLayout.JAVA_LONG) // size
    );
    public static final MethodHandle CUDA_MEMCPY_DTOD = LINKER.downcallHandle(
        LOOKUP.find("cuda_memcpy_dtod").orElse(null),
        FunctionDescriptor.of(ValueLayout.JAVA_INT,
            ValueLayout.ADDRESS, // destination pointer
            ValueLayout.ADDRESS, // source pointer
            ValueLayout.JAVA_LONG) // size
    );
    public static final MethodHandle CUDA_MEMCPY_HTOD_ASYNC = LINKER.downcallHandle(
        LOOKUP.find("cuda_memcpy_htod_async").orElse(null),
        FunctionDescriptor.of(ValueLayout.JAVA_INT,
            ValueLayout.ADDRESS, // buffer pointer
            ValueLayout.ADDRESS, // host pointer
            ValueLayout.JAVA_LONG, // size
            ValueLayout.ADDRESS) // stream
    );
    public static final MethodHandle CUDA_MEMCPY_DTOH_ASYNC = LINKER.downcallHandle(
        LOOKUP.find("cuda_memcpy_dtoh_async").orElse(null),
        FunctionDescriptor.of(ValueLayout.JAVA_INT,
            ValueLayout.ADDRESS, // host pointer
            ValueLayout.ADDRESS, // buffer pointer
            ValueLayout.JAVA_LONG, // size
            ValueLayout.ADDRESS) // stream
    );
    public static final MethodHandle CUDA_MEMCPY_DTOD_ASYNC = LINKER.downcallHandle(
        LOOKUP.find("cuda_memcpy_dtod_async").orElse(null),
        FunctionDescriptor.of(ValueLayout.JAVA_INT,
            ValueLayout.ADDRESS, // destination pointer
            ValueLayout.ADDRESS, // source pointer
            ValueLayout.JAVA_LONG, // size
            ValueLayout.ADDRESS) // stream
    );
    private static final Map<Class<?>, HostType> HOST_TYPES = Map.of(
        byte[].class,   new HostType(ValueLayout.JAVA_BYTE,   1),
        short[].class,  new HostType(ValueLayout.JAVA_SHORT,  2),
        int[].class,    new HostType(ValueLayout.JAVA_INT,    4),
        long[].class,   new HostType(ValueLayout.JAVA_LONG,   8),
        float[].class,  new HostType(ValueLayout.JAVA_FLOAT,  4),
        double[].class, new HostType(ValueLayout.JAVA_DOUBLE, 8)
    );


    @Override
    public CudaBuffer copy() throws Throwable {
        CudaBuffer buffer = device.allocateBytes(size);
        return copyInto(buffer);
    }

    @Override
    public CudaBuffer copyInto(ComputeBuffer other) throws Throwable {
        CudaBuffer buffer = (CudaBuffer) other;

        int res = (int) CUDA_MEMCPY_DTOD.invoke(buffer.handle, handle, size);
        if (res != 0) throw new RuntimeException("cuMemcpyDtoD failed: " + res);

        return buffer;
    }

    @Override
    public CudaBuffer copyAsync(ComputeQueue queue) throws Throwable {
        CudaBuffer buffer = device.allocateBytes(size);
        return copyIntoAsync(buffer, queue);
    }

    @Override
    public CudaBuffer copyIntoAsync(ComputeBuffer other, ComputeQueue queue) throws Throwable {
        CudaBuffer buffer = (CudaBuffer) other;
        CudaStream stream = (CudaStream) queue;

        int res = (int) CUDA_MEMCPY_DTOD_ASYNC.invoke(buffer.handle, handle, size, stream.handle());
        if (res != 0) throw new RuntimeException("cuMemcpyDtoD failed: " + res);

        return buffer;
    }

    @Override
    public void free() throws Throwable {
        int res = (int) CUDA_MEM_FREE.invoke(handle);
        if (res != 0) throw new RuntimeException("cuMemFree failed: " + res);
    }
    
    @Override
    public void get(byte[] data) throws Throwable {
        long size = bytesOf(data);
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment host = arena.allocate(ValueLayout.JAVA_BYTE, data.length);

            int res = (int) CUDA_MEMCPY_DTOH.invoke(host, handle, size);
            if (res != 0) throw new RuntimeException("cuMemcpyDtoH failed: " + res);

            MemorySegment.copy(host, 0, MemorySegment.ofArray(data), 0, size);
        }
    }

    @Override
    public void get(double[] data) throws Throwable {
        long size = bytesOf(data);
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment host = arena.allocate(ValueLayout.JAVA_DOUBLE, data.length);
            
            int res = (int) CUDA_MEMCPY_DTOH.invoke(host, handle, size);
            if (res != 0) throw new RuntimeException("cuMemcpyDtoH failed: " + res);
            
            MemorySegment.copy(host, 0, MemorySegment.ofArray(data), 0, size);
        }
    }

    @Override
    public void get(float[] data) throws Throwable {
        long size = bytesOf(data);
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment host = arena.allocate(ValueLayout.JAVA_FLOAT, data.length);
            
            int res = (int) CUDA_MEMCPY_DTOH.invoke(host, handle, size);
            if (res != 0) throw new RuntimeException("cuMemcpyDtoH failed: " + res);
            
            MemorySegment.copy(host, 0, MemorySegment.ofArray(data), 0, size);
        }
    }

    @Override
    public void get(long[] data) throws Throwable {
        long size = bytesOf(data);
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment host = arena.allocate(ValueLayout.JAVA_LONG, data.length);
            
            int res = (int) CUDA_MEMCPY_DTOH.invoke(host, handle, size);
            if (res != 0) throw new RuntimeException("cuMemcpyDtoH failed: " + res);
            
            MemorySegment.copy(host, 0, MemorySegment.ofArray(data), 0, size);
        }
    }

    @Override
    public void get(int[] data) throws Throwable {
        long size = bytesOf(data);
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment host = arena.allocate(ValueLayout.JAVA_INT, data.length);
            
            int res = (int) CUDA_MEMCPY_DTOH.invoke(host, handle, size);
            if (res != 0) throw new RuntimeException("cuMemcpyDtoH failed: " + res);
            
            MemorySegment.copy(host, 0, MemorySegment.ofArray(data), 0, size);
        }
    }

    @Override
    public void get(short[] data) throws Throwable {
        long size = bytesOf(data);
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment host = arena.allocate(ValueLayout.JAVA_SHORT, data.length);
            
            int res = (int) CUDA_MEMCPY_DTOH.invoke(host, handle, size);
            if (res != 0) throw new RuntimeException("cuMemcpyDtoH failed: " + res);
            
            MemorySegment.copy(host, 0, MemorySegment.ofArray(data), 0, size);
        }
    }

    // ========================= COPY TO DEVICE =========================
    public void copyToDevice(byte[] data) throws Throwable {
        long size = bytesOf(data);
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment host = arena.allocateFrom(ValueLayout.JAVA_BYTE, data);
            
            int res = (int) CUDA_MEMCPY_HTOD.invoke(handle, host, size);
            if (res != 0) throw new RuntimeException("cuMemcpyHtoD failed: " + res);
        }
    }
    
    public void copyToDevice(double[] data) throws Throwable {
        long size = bytesOf(data);
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment host = arena.allocateFrom(ValueLayout.JAVA_DOUBLE, data);
            
            int res = (int) CUDA_MEMCPY_HTOD.invoke(handle, host, size);
            if (res != 0) throw new RuntimeException("cuMemcpyHtoD failed: " + res);
        }
    }
    
    public void copyToDevice(float[] data) throws Throwable {
        long size = bytesOf(data);
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment host = arena.allocateFrom(ValueLayout.JAVA_FLOAT, data);
            
            int res = (int) CUDA_MEMCPY_HTOD.invoke(handle, host, size);
            if (res != 0) throw new RuntimeException("cuMemcpyHtoD failed: " + res);
        }
    }
    
    public void copyToDevice(long[] data) throws Throwable {
        long size = bytesOf(data);
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment host = arena.allocateFrom(ValueLayout.JAVA_LONG, data);
            
            int res = (int) CUDA_MEMCPY_HTOD.invoke(handle, host, size);
            if (res != 0) throw new RuntimeException("cuMemcpyHtoD failed: " + res);
        }
    }
    
    public void copyToDevice(int[] data) throws Throwable {
        long size = bytesOf(data);
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment host = arena.allocateFrom(ValueLayout.JAVA_INT, data);
            
            int res = (int) CUDA_MEMCPY_HTOD.invoke(handle, host, size);
            if (res != 0) throw new RuntimeException("cuMemcpyHtoD failed: " + res);
        }
    }
    
    public void copyToDevice(short[] data) throws Throwable {
        long size = bytesOf(data);
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment host = arena.allocateFrom(ValueLayout.JAVA_SHORT, data);
            
            int res = (int) CUDA_MEMCPY_HTOD.invoke(handle, host, size);
            if (res != 0) throw new RuntimeException("cuMemcpyHtoD failed: " + res);
        }
    }
    
    // ========================= ASYNC COPY TO DEVICE =========================
    public void copyToDeviceAsync(byte[] data, CudaStream stream) throws Throwable {
        long size = bytesOf(data);
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment host = arena.allocateFrom(ValueLayout.JAVA_BYTE, data);
            
            int res = (int) CUDA_MEMCPY_HTOD_ASYNC.invoke(handle, host, size, stream.handle());
            if (res != 0) throw new RuntimeException("cuMemcpyHtoDAsync failed: " + res);
        }
    }
    
    public void copyToDeviceAsync(double[] data, CudaStream stream) throws Throwable {
        long size = bytesOf(data);
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment host = arena.allocateFrom(ValueLayout.JAVA_DOUBLE, data);
            
            int res = (int) CUDA_MEMCPY_HTOD_ASYNC.invoke(handle, host, size, stream.handle());
            if (res != 0) throw new RuntimeException("cuMemcpyHtoDAsync failed: " + res);
        }
    }
    
    public void copyToDeviceAsync(float[] data, CudaStream stream) throws Throwable {
        long size = bytesOf(data);
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment host = arena.allocateFrom(ValueLayout.JAVA_FLOAT, data);
            
            int res = (int) CUDA_MEMCPY_HTOD_ASYNC.invoke(handle, host, size, stream.handle());
            if (res != 0) throw new RuntimeException("cuMemcpyHtoDAsync failed: " + res);
        }
    }
    
    public void copyToDeviceAsync(long[] data, CudaStream stream) throws Throwable {
        long size = bytesOf(data);
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment host = arena.allocateFrom(ValueLayout.JAVA_LONG, data);
            
            int res = (int) CUDA_MEMCPY_HTOD_ASYNC.invoke(handle, host, size, stream.handle());
            if (res != 0) throw new RuntimeException("cuMemcpyHtoDAsync failed: " + res);
        }
    }
    
    public void copyToDeviceAsync(int[] data, CudaStream stream) throws Throwable {
        long size = bytesOf(data);
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment host = arena.allocateFrom(ValueLayout.JAVA_INT, data);
            
            int res = (int) CUDA_MEMCPY_HTOD_ASYNC.invoke(handle, host, size, stream.handle());
            if (res != 0) throw new RuntimeException("cuMemcpyHtoDAsync failed: " + res);
        }
    }
    
    public void copyToDeviceAsync(short[] data, CudaStream stream) throws Throwable {
        long size = bytesOf(data);
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment host = arena.allocateFrom(ValueLayout.JAVA_SHORT, data);
            
            int res = (int) CUDA_MEMCPY_HTOD_ASYNC.invoke(handle, host, size, stream.handle());
            if (res != 0) throw new RuntimeException("cuMemcpyHtoDAsync failed: " + res);
        }
    }
    
    public long getNativePointer() throws Throwable {
        return (long) CUDA_BUFFER_PTR.invoke(handle);
    }
}
