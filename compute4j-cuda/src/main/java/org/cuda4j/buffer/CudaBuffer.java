package org.cuda4j.buffer;

import org.cuda4j.CudaObject;
import org.cuda4j.context.CudaStream;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;

public record CudaBuffer(MemorySegment handle, long length) implements CudaObject {
    
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
    
    public void transferTo(CudaBuffer destination, long size) throws Throwable {
        int res = (int) CUDA_MEMCPY_DTOH.invoke(destination.handle, handle, size);
        
        if (res != 0) {
            throw new RuntimeException("cuMemcpyDtoD failed: " + res);
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
    
    public void copyToDevice(int[] data) throws Throwable {
        long size = bytesOf(data);
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment host = arena.allocateFrom(ValueLayout.JAVA_INT, data);
            
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
    
    // ========================= COPY TO HOST =========================
    public void copyToHost(byte[] data) throws Throwable {
        long size = bytesOf(data);
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment host = arena.allocate(ValueLayout.JAVA_BYTE, data.length);
            
            int res = (int) CUDA_MEMCPY_DTOH.invoke(host, handle, size);
            if (res != 0) throw new RuntimeException("cuMemcpyDtoH failed: " + res);
            
            MemorySegment.copy(host, 0, MemorySegment.ofArray(data), 0, size);
        }
    }
    
    public void copyToHost(int[] data) throws Throwable {
        long size = bytesOf(data);
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment host = arena.allocate(ValueLayout.JAVA_INT, data.length);
            
            int res = (int) CUDA_MEMCPY_DTOH.invoke(host, handle, size);
            if (res != 0) throw new RuntimeException("cuMemcpyDtoH failed: " + res);
            
            MemorySegment.copy(host, 0, MemorySegment.ofArray(data), 0, size);
        }
    }
    
    public void copyToHost(float[] data) throws Throwable {
        long size = bytesOf(data);
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment host = arena.allocate(ValueLayout.JAVA_FLOAT, data.length);
            
            int res = (int) CUDA_MEMCPY_DTOH.invoke(host, handle, size);
            if (res != 0) throw new RuntimeException("cuMemcpyDtoH failed: " + res);
            
            MemorySegment.copy(host, 0, MemorySegment.ofArray(data), 0, size);
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
    
    public void copyToDeviceAsync(int[] data, CudaStream stream) throws Throwable {
        long size = bytesOf(data);
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment host = arena.allocateFrom(ValueLayout.JAVA_INT, data);
            
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

    // ========================= ASYNC COPY TO HOST =========================
    public void copyToHostAsync(byte[] data, CudaStream stream) throws Throwable {
        long size = bytesOf(data);
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment host = arena.allocate(ValueLayout.JAVA_BYTE, data.length);
            
            int res = (int) CUDA_MEMCPY_DTOH_ASYNC.invoke(host, handle, size, stream.handle());
            if (res != 0) throw new RuntimeException("cuMemcpyDtoHAsync failed: " + res);
            
            MemorySegment.copy(host, 0, MemorySegment.ofArray(data), 0, size);
        }
    }
    
    public void copyToHostAsync(int[] data, CudaStream stream) throws Throwable {
        long size = bytesOf(data);
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment host = arena.allocate(ValueLayout.JAVA_INT, data.length);
            
            int res = (int) CUDA_MEMCPY_DTOH_ASYNC.invoke(host, handle, size, stream.handle());
            if (res != 0) throw new RuntimeException("cuMemcpyDtoHAsync failed: " + res);
            
            MemorySegment.copy(host, 0, MemorySegment.ofArray(data), 0, size);
        }
    }
    
    public void copyToHostAsync(float[] data, CudaStream stream) throws Throwable {
        long size = bytesOf(data);
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment host = arena.allocate(ValueLayout.JAVA_FLOAT, data.length);
            
            int res = (int) CUDA_MEMCPY_DTOH_ASYNC.invoke(host, handle, size, stream.handle());
            if (res != 0) throw new RuntimeException("cuMemcpyDtoHAsync failed: " + res);
            
            MemorySegment.copy(host, 0, MemorySegment.ofArray(data), 0, size);
        }
    }
    
    public long devicePointer() throws Throwable {
        return (long) CUDA_BUFFER_PTR.invoke(handle);
    }
}
