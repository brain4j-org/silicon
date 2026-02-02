package org.silicon.device;

import org.silicon.backend.BackendType;
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

    BackendType getBackendType();

    ComputeQueue createQueue();

    ComputeQueue createQueue(ComputeArena arena);

    ComputeModule loadModule(Path path);

    ComputeModule loadModule(byte[] rawSrc);

    ComputeModule loadModule(String source);

    ComputeBuffer allocateBytes(long size);

    ComputeBuffer allocateArray(byte[] data);
    
    ComputeBuffer allocateArray(double[] data);

    ComputeBuffer allocateArray(float[] data);

    ComputeBuffer allocateArray(long[] data);

    ComputeBuffer allocateArray(int[] data);

    ComputeBuffer allocateArray(short[] data);

    default ComputeArena createArena() {
        return new ComputeArena(this);
    }
}