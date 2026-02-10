package org.silicon.api.device;

import org.silicon.api.SiliconException;
import org.silicon.api.backend.BackendType;
import org.silicon.api.cache.MemoryPool;
import org.silicon.api.function.ComputeModule;
import org.silicon.api.kernel.ComputeQueue;

import java.io.InputStream;
import java.nio.file.Path;

/**
 * Execution context for a compute backend.
 * <p>
 * Provides queue creation, buffer allocation, and module loading.
 * Each context is tied to a specific {@link ComputeDevice} and owns the
 * backend-specific resources used for dispatch.
 */
public interface ComputeContext {

    /**
     * Loads a module from a classpath resource.
     * @param resourcePath resource path
     * @return loaded module
     */
    default ComputeModule loadModuleFromResources(String resourcePath) {
        try (InputStream in = ComputeContext.class.getResourceAsStream(resourcePath)) {
            if (in == null) {
                throw new IllegalArgumentException("Resource not found: " + resourcePath);
            }

            byte[] src = in.readAllBytes();
            return loadModule(src);
        } catch (Exception e) {
            throw new SiliconException("loadModuleFromResources(String) failed", e);
        }
    }

    /**
     * Creates a new memory pool.
     * @return the new memory pool
     * @param <K> key to index memory objects
     * @param <V> values contained in the pool
     */
    default <K extends Record, V> MemoryPool<K, V> createPool() {
        return new MemoryPool<>();
    }

    /**
     * @return backend type associated with this context
     */
    BackendType getBackendType();

    /**
     * Creates a new execution queue.
     * @return compute queue
     */
    ComputeQueue createQueue();

    /**
     * Creates a new execution queue and registers it in the arena.
     * @param arena lifetime arena
     * @return compute queue
     */
    ComputeQueue createQueue(ComputeArena arena);

    /**
     * Loads a module from a file.
     * @param path file path
     * @return loaded module
     */
    ComputeModule loadModule(Path path);

    /**
     * Loads a module from raw source (binary or text).
     * @param rawSrc source contents
     * @return loaded module
     */
    ComputeModule loadModule(byte[] rawSrc);

    /**
     * Loads a module from text source.
     * @param source module source
     * @return loaded module
     */
    ComputeModule loadModule(String source);

    /**
     * Allocates an uninitialized byte buffer.
     * @param size size in bytes
     * @return allocated buffer
     */
    ComputeBuffer allocateBytes(long size);

    /**
     * Allocates and initializes a byte buffer.
     * @param data source data
     * @return allocated buffer
     */
    ComputeBuffer allocateArray(byte[] data);
    
    /**
     * Allocates and initializes a double buffer.
     * @param data source data
     * @return allocated buffer
     */
    ComputeBuffer allocateArray(double[] data);

    /**
     * Allocates and initializes a float buffer.
     * @param data source data
     * @return allocated buffer
     */
    ComputeBuffer allocateArray(float[] data);

    /**
     * Allocates and initializes a long buffer.
     * @param data source data
     * @return allocated buffer
     */
    ComputeBuffer allocateArray(long[] data);

    /**
     * Allocates and initializes an int buffer.
     * @param data source data
     * @return allocated buffer
     */
    ComputeBuffer allocateArray(int[] data);

    /**
     * Allocates and initializes a short buffer.
     * @param data source data
     * @return allocated buffer
     */
    ComputeBuffer allocateArray(short[] data);

    /**
     * Creates an arena for automatic resource lifetime management.
     * @return new arena associated with this context
     */
    default ComputeArena createArena() {
        return new ComputeArena(this);
    }
}
