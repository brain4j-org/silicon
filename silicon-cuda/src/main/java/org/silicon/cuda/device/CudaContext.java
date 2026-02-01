package org.silicon.cuda.device;

import org.silicon.SiliconException;
import org.silicon.backend.BackendType;
import org.silicon.computing.ComputeQueue;
import org.silicon.cuda.CudaObject;
import org.silicon.cuda.computing.CudaStream;
import org.silicon.cuda.kernel.CudaModule;
import org.silicon.device.ComputeArena;
import org.silicon.device.ComputeContext;

import java.io.File;
import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.nio.file.Path;


public record CudaContext(MemorySegment handle, CudaDevice device) implements CudaObject, ComputeContext {

    public static final MethodHandle CUDA_SYNC_CONTEXT = CudaObject.find(
        "cuda_sync_context",
        FunctionDescriptor.of(ValueLayout.JAVA_INT)
    );
    public static final MethodHandle CUDA_CONTEXT_SET_CURRENT = CudaObject.find(
        "cuda_context_set_current",
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS)
    );
    public static final MethodHandle CUDA_STREAM_CREATE = CudaObject.find(
        "cuda_stream_create",
        FunctionDescriptor.of(ValueLayout.ADDRESS)
    );
    public static final MethodHandle CUDA_MODULE_LOAD = CudaObject.find(
        "cuda_module_load",
        FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS)
    );
    public static final MethodHandle CUDA_MODULE_LOAD_DATA = CudaObject.find(
        "cuda_module_load_data",
        FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS)
    );
    public static final MethodHandle CUDA_MEM_ALLOC = CudaObject.find(
        "cuda_mem_alloc",
        FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.JAVA_LONG)
    );

    public CudaContext setCurrent() {
        try {
            int res = (int) CUDA_CONTEXT_SET_CURRENT.invoke(handle);
            if (res != 0) throw new RuntimeException("cuCtxSetCurrent failed: " + res);

            return this;
        } catch (Throwable e) {
            throw new SiliconException("setCurrent() failed", e);
        }
    }

    public CudaContext synchronize() {
        try {
            int res = (int) CUDA_SYNC_CONTEXT.invoke();

            if (res != 0) throw new RuntimeException("cuCtxSynchronize failed: " + res);

            return this;
        } catch (Throwable e) {
            throw new SiliconException("synchronize() failed", e);
        }
    }

    @Override
    public BackendType getBackendType() {
        return BackendType.CUDA;
    }

    @Override
    public CudaStream createQueue() {
        return createQueue(null);
    }

    @Override
    public CudaStream createQueue(ComputeArena arena) {
        try {
            MemorySegment ptr = (MemorySegment) CUDA_STREAM_CREATE.invoke();

            if (ptr == null || ptr.address() == 0) {
                throw new RuntimeException("cuStreamCreate failed");
            }

            return new CudaStream(ptr);
        } catch (Throwable e) {
            throw new SiliconException("createQueue() failed", e);
        }
    }

    @Override
    public CudaModule loadModule(Path path) {
        if (!new File(path.toString()).exists()) {
            throw new IllegalArgumentException(path + " does not exist");
        }

        try (Arena arena = Arena.ofConfined()) {
            MemorySegment cPath = arena.allocateFrom(path.toString());
            MemorySegment moduleHandle = (MemorySegment) CUDA_MODULE_LOAD.invoke(cPath);

            if (moduleHandle == null || moduleHandle.address() == 0) {
                throw new RuntimeException("cuModuleLoad failed for: " + path);
            }

            return new CudaModule(moduleHandle, this);
        } catch (Throwable e) {
            throw new SiliconException("loadModule(Path) failed", e);
        }
    }

    @Override
    public CudaModule loadModule(byte[] rawSrc) {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment data = arena.allocateFrom(ValueLayout.JAVA_BYTE, rawSrc);
            MemorySegment moduleHandle = (MemorySegment) CUDA_MODULE_LOAD_DATA.invoke(data);

            if (moduleHandle == null || moduleHandle.address() == 0) {
                throw new RuntimeException("cuModuleLoadData failed");
            }

            return new CudaModule(moduleHandle, this);
        } catch (Throwable e) {
            throw new SiliconException("loadModule(byte[]) failed", e);
        }
    }

    @Override
    public CudaModule loadModule(String source) {
        throw new UnsupportedOperationException("JIT kernel compilation is not supported yet");
    }

    @Override
    public CudaBuffer allocateBytes(long size) {
        try {
            MemorySegment ptr = (MemorySegment) CUDA_MEM_ALLOC.invoke(size);

            if (ptr == null || ptr.address() == 0) {
                throw new RuntimeException("cuMemAlloc failed: " + ptr);
            }

            return new CudaBuffer(this, ptr, size);
        } catch (Throwable e) {
            throw new SiliconException("allocateBytes(long) failed", e);
        }
    }

    @Override
    public CudaBuffer allocateArray(byte[] data, long size) {
        CudaBuffer buffer = allocateBytes(size);
        buffer.copyToDevice(data);
        return buffer;
    }

    @Override
    public CudaBuffer allocateArray(double[] data, long size) {
        CudaBuffer buffer = allocateBytes(size);
        buffer.copyToDevice(data);
        return buffer;
    }

    @Override
    public CudaBuffer allocateArray(float[] data, long size) {
        CudaBuffer buffer = allocateBytes(size);
        buffer.copyToDevice(data);
        return buffer;
    }
    
    @Override
    public CudaBuffer allocateArray(long[] data, long size) {
        CudaBuffer buffer = allocateBytes(size);
        buffer.copyToDevice(data);
        return buffer;
    }
    
    @Override
    public CudaBuffer allocateArray(int[] data, long size) {
        CudaBuffer buffer = allocateBytes(size);
        buffer.copyToDevice(data);
        return buffer;
    }

    @Override
    public CudaBuffer allocateArray(short[] data, long size) {
        CudaBuffer buffer = allocateBytes(size);
        buffer.copyToDevice(data);
        return buffer;
    }
}
