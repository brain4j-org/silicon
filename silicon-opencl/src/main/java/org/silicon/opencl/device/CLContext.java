package org.silicon.opencl.device;

import org.lwjgl.opencl.CL10;
import org.lwjgl.system.MemoryStack;
import org.silicon.api.SiliconException;
import org.silicon.api.backend.BackendType;
import org.silicon.api.device.ComputeArena;
import org.silicon.api.device.ComputeContext;
import org.silicon.api.function.ComputeModule;
import org.silicon.opencl.computing.CLCommandQueue;
import org.silicon.opencl.kernel.CLProgram;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Path;

public record CLContext(long handle, long device) implements ComputeContext {
    
    private void writeBuffer(CLBuffer buffer, ByteBuffer data) {
        CLCommandQueue clQueue = createQueue();
        
        int err = CL10.clEnqueueWriteBuffer(
            clQueue.handle(), buffer.getHandle(), true, 0L,
            data, null, null
        );
        
        if (err != CL10.CL_SUCCESS) throw new IllegalStateException("clEnqueueWriteBuffer failed: " + err);
        
        clQueue.await();
        clQueue.free();
    }
    
    @Override
    public BackendType backendType() {
        return BackendType.OPENCL;
    }
    
    @Override
    public CLCommandQueue createQueue() {
        return createQueue(null);
    }

    @Override
    public CLCommandQueue createQueue(ComputeArena arena) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer result = stack.mallocInt(1);

            long queue = CL10.clCreateCommandQueue(handle, device, 0, result);
            if (result.get(0) != CL10.CL_SUCCESS) throw new SiliconException("clCreateCommandQueue failed: " + result.get(0));

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
            
            if (program == 0L) throw new SiliconException("clCreateProgramWithSource returned null");
            if (result.get(0) != CL10.CL_SUCCESS) {
                throw new SiliconException("clCreateProgramWithSource failed: " + result.get(0));
            }
            
            int buildErr = CL10.clBuildProgram(program, device, "", null, 0);
            if (buildErr != CL10.CL_SUCCESS) throw new SiliconException("clBuildProgram failed: " + buildErr);
            
            return new CLProgram(program, device);
        }
    }
    
    @Override
    public CLBuffer allocateBytes(long size) {
        if (size < 0) throw new IllegalArgumentException("Allocation size must be positive");
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer err = stack.mallocInt(1);
            
            long mem = CL10.clCreateBuffer(handle, CL10.CL_MEM_READ_WRITE, size, err);
            if (err.get(0) != CL10.CL_SUCCESS) throw new IllegalStateException("clCreateBuffer failed: " + err.get(0));
            
            return new CLBuffer(mem, this, size);
        }
    }
    
    @Override
    public CLBuffer allocateArray(byte[] data) {
        CLBuffer buffer = allocateBytes(data.length);
        ByteBuffer buf = ByteBuffer
            .allocateDirect(data.length)
            .order(ByteOrder.nativeOrder());
        
        buf.put(data).flip();
        
        writeBuffer(buffer, buf);
        return buffer;
    }
    
    @Override
    public CLBuffer allocateArray(double[] data) {
        long required = (long) data.length * Double.BYTES;
        
        CLBuffer buffer = allocateBytes(required);
        ByteBuffer buf = ByteBuffer
            .allocateDirect(data.length * Double.BYTES)
            .order(ByteOrder.nativeOrder());
        
        buf.asDoubleBuffer().put(data).flip();
        
        writeBuffer(buffer, buf);
        return buffer;
    }
    
    @Override
    public CLBuffer allocateArray(float[] data) {
        long required = (long) data.length * Float.BYTES;

        CLBuffer buffer = allocateBytes(required);
        ByteBuffer buf = ByteBuffer
            .allocateDirect(data.length * Float.BYTES)
            .order(ByteOrder.nativeOrder());
        
        buf.asFloatBuffer().put(data);
        
        writeBuffer(buffer, buf);
        return buffer;
    }
    
    @Override
    public CLBuffer allocateArray(long[] data) {
        long required = (long) data.length * Long.BYTES;
        
        CLBuffer buffer = allocateBytes(required);
        ByteBuffer buf = ByteBuffer
            .allocateDirect((int) required)
            .order(ByteOrder.nativeOrder());
        
        buf.asLongBuffer().put(data).flip();
        
        writeBuffer(buffer, buf);
        return buffer;
    }
    
    @Override
    public CLBuffer allocateArray(int[] data) {
        long required = (long) data.length * Integer.BYTES;

        CLBuffer buffer = allocateBytes(required);
        ByteBuffer buf = ByteBuffer
            .allocateDirect((int) required)
            .order(ByteOrder.nativeOrder());
        
        buf.asIntBuffer().put(data).flip();
        
        writeBuffer(buffer, buf);
        return buffer;
    }
    
    @Override
    public CLBuffer allocateArray(short[] data) {
        long required = (long) data.length * Short.BYTES;

        CLBuffer buffer = allocateBytes(required);
        ByteBuffer buf = ByteBuffer
            .allocateDirect((int) required)
            .order(ByteOrder.nativeOrder());
        
        buf.asShortBuffer().put(data).flip();
        
        writeBuffer(buffer, buf);
        return buffer;
    }
}
