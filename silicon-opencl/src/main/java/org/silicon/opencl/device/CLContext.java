package org.silicon.opencl.device;

import org.lwjgl.opencl.CL10;
import org.lwjgl.system.MemoryStack;
import org.silicon.SiliconException;
import org.silicon.computing.ComputeQueue;
import org.silicon.device.ComputeContext;
import org.silicon.kernel.ComputeModule;
import org.silicon.opencl.computing.CLCommandQueue;
import org.silicon.opencl.kernel.CLProgram;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Path;

public record CLContext(long handle, long device) implements ComputeContext {
    
    private void writeBuffer(CLBuffer buffer, ByteBuffer data, ComputeQueue queue) {
        boolean blocking = queue == null;
        if (queue == null) queue = createQueue();
        
        CLCommandQueue clQueue = (CLCommandQueue) queue;
        int err = CL10.clEnqueueWriteBuffer(
            clQueue.handle(), buffer.getHandle(), blocking, 0L,
            data, null, null
        );
        
        if (err != CL10.CL_SUCCESS) throw new IllegalStateException("clEnqueueWriteBuffer failed: " + err);
        if (blocking) {
            queue.awaitCompletion();
            queue.release();
        }
    }
    
    @Override
    public CLCommandQueue createQueue() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer result = stack.mallocInt(1);
            
            long queue = CL10.clCreateCommandQueue(handle, device, 0, result);
            if (result.get(0) != CL10.CL_SUCCESS) throw new RuntimeException("clCreateCommandQueue failed: " + result.get(0));
            
            return new CLCommandQueue(queue);
        }
    }
    
    @Override
    public ComputeModule loadModule(Path path) {
        try {
            return loadModule(Files.readAllBytes(path));
        } catch (Throwable e) {
            throw new SiliconException("loadModule(Path) failed", e);
        }
    }
    
    @Override
    public ComputeModule loadModule(byte[] rawSrc) {
        return loadModule(new String(rawSrc));
    }
    
    @Override
    public ComputeModule loadModule(String source) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer result = stack.mallocInt(1);
            long program = CL10.clCreateProgramWithSource(handle, source, result);
            
            if (program == 0L) throw new RuntimeException("clCreateProgramWithSource returned null!");
            if (result.get(0) != CL10.CL_SUCCESS) {
                throw new RuntimeException("clCreateProgramWithSource failed: " + result.get(0));
            }
            
            int buildErr = CL10.clBuildProgram(program, device, "", null, 0);
            if (buildErr != CL10.CL_SUCCESS) throw new RuntimeException("clBuildProgram failed: " + buildErr);
            
            return new CLProgram(program);
        }
    }
    
    @Override
    public CLBuffer allocateBytes(long size) {
        if (size < 0) throw new IllegalArgumentException("Allocation size must be positive!");
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer err = stack.mallocInt(1);
            
            long mem = CL10.clCreateBuffer(handle, CL10.CL_MEM_READ_WRITE, size, err);
            if (err.get(0) != CL10.CL_SUCCESS) throw new IllegalStateException("clCreateBuffer failed: " + err.get(0));
            
            return new CLBuffer(mem, this, size);
        }
    }
    
    @Override
    public CLBuffer allocateArray(byte[] data, long size) {
        return allocateArray(data, size, null);
    }
    
    @Override
    public CLBuffer allocateArray(byte[] data, long size, ComputeQueue queue) {
        if (data.length > size) {
            throw new IllegalArgumentException(
                "byte[] requires " + data.length + " bytes, but buffer size is " + size
            );
        }
        
        CLBuffer buffer = allocateBytes(size);
        ByteBuffer buf = ByteBuffer
            .allocateDirect(data.length)
            .order(ByteOrder.nativeOrder());
        
        buf.put(data).flip();
        
        writeBuffer(buffer, buf, queue);
        return buffer;
    }
    
    @Override
    public CLBuffer allocateArray(double[] data, long size) {
        return allocateArray(data, size, null);
    }
    
    @Override
    public CLBuffer allocateArray(double[] data, long size, ComputeQueue queue) {
        long required = (long) data.length * Double.BYTES;
        if (required > size) {
            throw new IllegalArgumentException(
                "double[] requires " + required + " bytes, but buffer size is " + size
            );
        }
        
        CLBuffer buffer = allocateBytes(size);
        ByteBuffer buf = ByteBuffer
            .allocateDirect(data.length * Double.BYTES)
            .order(ByteOrder.nativeOrder());
        
        buf.asDoubleBuffer().put(data).flip();
        
        writeBuffer(buffer, buf, queue);
        return buffer;
    }
    
    @Override
    public CLBuffer allocateArray(float[] data, long size) {
        return allocateArray(data, size, null);
    }
    
    @Override
    public CLBuffer allocateArray(float[] data, long size, ComputeQueue queue) {
        long required = (long) data.length * Float.BYTES;
        if (required > size) {
            throw new IllegalArgumentException(
                "float[] requires " + required + " bytes, but buffer size is " + size
            );
        }
        
        CLBuffer buffer = allocateBytes(size);
        ByteBuffer buf = ByteBuffer
            .allocateDirect(data.length * Float.BYTES)
            .order(ByteOrder.nativeOrder());
        
        buf.asFloatBuffer().put(data);
        
        writeBuffer(buffer, buf, queue);
        return buffer;
    }
    
    @Override
    public CLBuffer allocateArray(long[] data, long size) {
        return allocateArray(data, size, null);
    }
    
    @Override
    public CLBuffer allocateArray(long[] data, long size, ComputeQueue queue) {
        long required = (long) data.length * Long.BYTES;
        if (required > size) {
            throw new IllegalArgumentException(
                "long[] requires " + required + " bytes, but buffer size is " + size
            );
        }
        
        CLBuffer buffer = allocateBytes(size);
        ByteBuffer buf = ByteBuffer
            .allocateDirect((int) required)
            .order(ByteOrder.nativeOrder());
        
        buf.asLongBuffer().put(data).flip();
        
        writeBuffer(buffer, buf, queue);
        return buffer;
    }
    
    @Override
    public CLBuffer allocateArray(int[] data, long size) {
        return allocateArray(data, size, null);
    }
    
    @Override
    public CLBuffer allocateArray(int[] data, long size, ComputeQueue queue) {
        long required = (long) data.length * Integer.BYTES;
        if (required > size) {
            throw new IllegalArgumentException(
                "int[] requires " + required + " bytes, but buffer size is " + size
            );
        }
        
        CLBuffer buffer = allocateBytes(size);
        ByteBuffer buf = ByteBuffer
            .allocateDirect((int) required)
            .order(ByteOrder.nativeOrder());
        
        buf.asIntBuffer().put(data).flip();
        
        writeBuffer(buffer, buf, queue);
        return buffer;
    }
    
    @Override
    public CLBuffer allocateArray(short[] data, long size) {
        return allocateArray(data, size, null);
    }
    
    @Override
    public CLBuffer allocateArray(short[] data, long size, ComputeQueue queue) {
        long required = (long) data.length * Short.BYTES;
        if (required > size) {
            throw new IllegalArgumentException(
                "short[] requires " + required + " bytes, but buffer size is " + size
            );
        }
        
        CLBuffer buffer = allocateBytes(size);
        ByteBuffer buf = ByteBuffer
            .allocateDirect((int) required)
            .order(ByteOrder.nativeOrder());
        
        buf.asShortBuffer().put(data).flip();
        
        writeBuffer(buffer, buf, queue);
        return buffer;
    }
}
