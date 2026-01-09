package org.silicon.device;

import org.silicon.computing.ComputeQueue;
import org.silicon.kernel.ComputeModule;

import java.io.InputStream;
import java.nio.file.Path;

public interface ComputeContext {
    
    default ComputeModule loadModuleFromResources(String resourcePath) {
        try (InputStream in = ComputeContext.class.getResourceAsStream(resourcePath)) {
            if (in == null) {
                throw new IllegalArgumentException("Resource not found: " + resourcePath);
            }
            
            byte[] src = in.readAllBytes();
            return loadModule(src);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    ComputeQueue createQueue();
    ComputeModule loadModule(Path path);
    ComputeModule loadModule(byte[] rawSrc);
    ComputeModule loadModule(String source);
    
    ComputeBuffer allocateBytes(long size);
    ComputeBuffer allocateArray(byte[] data, long size);
    ComputeBuffer allocateArray(byte[] data, long size, ComputeQueue queue);
    ComputeBuffer allocateArray(double[] data, long size);
    ComputeBuffer allocateArray(double[] data, long size, ComputeQueue queue);
    ComputeBuffer allocateArray(float[] data, long size);
    ComputeBuffer allocateArray(float[] data, long size, ComputeQueue queue);
    ComputeBuffer allocateArray(long[] data, long size);
    ComputeBuffer allocateArray(long[] data, long size, ComputeQueue queue);
    ComputeBuffer allocateArray(int[] data, long size);
    ComputeBuffer allocateArray(int[] data, long size, ComputeQueue queue);
    ComputeBuffer allocateArray(short[] data, long size);
    ComputeBuffer allocateArray(short[] data, long size, ComputeQueue queue);
    
    default ComputeBuffer allocateArray(byte[] data) {
        return allocateArray(data, data.length);
    }
    
    default ComputeBuffer allocateArray(byte[] data, ComputeQueue queue) {
        return allocateArray(data, data.length, queue);
    }
    
    default ComputeBuffer allocateArray(double[] data) {
        return allocateArray(data, data.length * 8L);
    }
    
    default ComputeBuffer allocateArray(double[] data, ComputeQueue queue) {
        return allocateArray(data, data.length * 8L, queue);
    }
    
    default ComputeBuffer allocateArray(float[] data) {
        return allocateArray(data, data.length * 4L);
    }
    
    default ComputeBuffer allocateArray(float[] data, ComputeQueue queue) {
        return allocateArray(data, data.length * 4L, queue);
    }
    
    default ComputeBuffer allocateArray(long[] data) {
        return allocateArray(data, data.length * 8L);
    }
    
    default ComputeBuffer allocateArray(long[] data, ComputeQueue queue) {
        return allocateArray(data, data.length * 8L, queue);
    }
    
    default ComputeBuffer allocateArray(int[] data) {
        return allocateArray(data, data.length * 4L);
    }
    
    default ComputeBuffer allocateArray(int[] data, ComputeQueue queue) {
        return allocateArray(data, data.length * 4L, queue);
    }
    
    default ComputeBuffer allocateArray(short[] data) {
        return allocateArray(data, data.length * 2L);
    }
    
    default ComputeBuffer allocateArray(short[] data, ComputeQueue queue) {
        return allocateArray(data, data.length * 2L, queue);
    }

    default ComputeArena createArena() {
        return new ComputeArena(this);
    }
}
