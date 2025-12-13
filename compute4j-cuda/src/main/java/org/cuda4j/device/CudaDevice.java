package org.cuda4j.device;

import org.compute4j.computing.ComputeQueue;
import org.compute4j.device.ComputeBuffer;
import org.compute4j.device.ComputeDevice;
import org.cuda4j.CudaObject;
import org.cuda4j.buffer.CudaBuffer;
import org.cuda4j.context.CudaContext;
import org.cuda4j.context.CudaStream;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;

public record CudaDevice(MemorySegment handle, int index) implements CudaObject, ComputeDevice {
    
    public static final MethodHandle CUDA_DEVICE_NAME = LINKER.downcallHandle(
        LOOKUP.find("cuda_device_name").orElse(null),
        FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS)
    );
    public static final MethodHandle CUDA_CREATE_CONTEXT = LINKER.downcallHandle(
        LOOKUP.find("cuda_create_context").orElse(null),
        FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS)
    );
    public static final MethodHandle CUDA_MEM_ALLOC = LINKER.downcallHandle(
        LOOKUP.find("cuda_mem_alloc").orElse(null),
        FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.JAVA_LONG)
    );
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
    
    @Override
    public CudaContext createContext() throws Throwable {
        MemorySegment ctx = (MemorySegment) CUDA_CREATE_CONTEXT.invoke(handle());
        
        if (ctx == null || ctx.address() == 0) {
            throw new RuntimeException("Failed to create CUDA context");
        }
        
        return new CudaContext(ctx).setCurrent();
    }
    
    @Override
    public String getName() throws Throwable {
        MemorySegment nameHandle = (MemorySegment) CUDA_DEVICE_NAME.invokeExact(handle);
        return nameHandle.reinterpret(Long.MAX_VALUE).getString(0);
    }
    
    @Override
    public CudaBuffer allocateBytes(long size) throws Throwable {
        MemorySegment ptr = (MemorySegment) CUDA_MEM_ALLOC.invoke(size);
        
        if (ptr == null || ptr.address() == 0) {
            throw new OutOfMemoryError("cuMemAlloc failed: " + ptr);
        }
        
        return new CudaBuffer(ptr, size);
    }
    
    @Override
    public CudaBuffer allocateBytes(long size, ComputeQueue queue) {
        return null; // TODO cuMallocAsync
    }
    
    @Override
    public ComputeBuffer allocateArray(byte[] data, long size) throws Throwable {
        CudaBuffer buffer = allocateBytes(size);
        buffer.copyToDevice(data);
        return buffer;
    }
    
    @Override
    public ComputeBuffer allocateArray(byte[] data, long size, ComputeQueue queue) throws Throwable {
        CudaBuffer buffer = allocateBytes(size);
        buffer.copyToDeviceAsync(data, (CudaStream) queue);
        return buffer;
    }
    
    @Override
    public CudaBuffer allocateArray(double[] data, long size) throws Throwable {
        CudaBuffer buffer = allocateBytes(size);
        buffer.copyToDevice(data);
        return buffer;
    }
    
    @Override
    public CudaBuffer allocateArray(double[] data, long size, ComputeQueue queue) throws Throwable {
        CudaBuffer buffer = allocateBytes(size);
        buffer.copyToDeviceAsync(data, (CudaStream) queue);
        return buffer;
    }
    
    @Override
    public ComputeBuffer allocateArray(float[] data, long size) throws Throwable {
        CudaBuffer buffer = allocateBytes(size);
        buffer.copyToDevice(data);
        return buffer;
    }
    
    @Override
    public ComputeBuffer allocateArray(float[] data, long size, ComputeQueue queue) throws Throwable {
        CudaBuffer buffer = allocateBytes(size);
        buffer.copyToDeviceAsync(data, (CudaStream) queue);
        return buffer;
    }
    
    @Override
    public ComputeBuffer allocateArray(long[] data, long size) throws Throwable {
        CudaBuffer buffer = allocateBytes(size);
        buffer.copyToDevice(data);
        return buffer;
    }
    
    @Override
    public ComputeBuffer allocateArray(long[] data, long size, ComputeQueue queue) throws Throwable {
        CudaBuffer buffer = allocateBytes(size);
        buffer.copyToDeviceAsync(data, (CudaStream) queue);
        return buffer;
    }
    
    @Override
    public ComputeBuffer allocateArray(int[] data, long size) throws Throwable {
        CudaBuffer buffer = allocateBytes(size);
        buffer.copyToDevice(data);
        return buffer;
    }
    
    @Override
    public ComputeBuffer allocateArray(int[] data, long size, ComputeQueue queue) throws Throwable {
        CudaBuffer buffer = allocateBytes(size);
        buffer.copyToDeviceAsync(data, (CudaStream) queue);
        return buffer;
    }
    
    @Override
    public ComputeBuffer allocateArray(short[] data, long size) throws Throwable {
        CudaBuffer buffer = allocateBytes(size);
        buffer.copyToDevice(data);
        return buffer;
    }
    
    @Override
    public ComputeBuffer allocateArray(short[] data, long size, ComputeQueue queue) throws Throwable {
        CudaBuffer buffer = allocateBytes(size);
        buffer.copyToDeviceAsync(data, (CudaStream) queue);
        return buffer;
    }
}
