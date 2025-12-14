package org.silicon.cuda.device;

import org.silicon.computing.ComputeQueue;
import org.silicon.device.ComputeBuffer;
import org.silicon.device.ComputeDevice;
import org.silicon.cuda.CudaObject;
import org.silicon.cuda.buffer.CudaBuffer;
import org.silicon.cuda.context.CudaContext;
import org.silicon.cuda.context.CudaStream;

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
        
        return new CudaBuffer(this, ptr, size);
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
