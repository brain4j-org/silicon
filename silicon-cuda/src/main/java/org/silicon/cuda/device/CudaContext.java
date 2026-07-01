package org.silicon.cuda.device;

import org.silicon.api.SiliconException;
import org.silicon.api.backend.BackendType;
import org.silicon.api.device.ComputeArena;
import org.silicon.api.device.ComputeContext;
import org.silicon.cuda.Bindings;
import org.silicon.cuda.CUResult;
import org.silicon.cuda.CudaObject;
import org.silicon.cuda.function.CudaModule;
import org.silicon.cuda.kernel.CudaStream;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.silicon.cuda.Bindings.*;


public record CudaContext(MemorySegment handle, CudaDevice device) implements CudaObject, ComputeContext {


    public CudaContext setCurrent() {
        try {
            int res = (int) CU_CTX_SET_CURRENT.invokeExact(handle);

            if (res != 0) {
                throw new SiliconException("cuCtxSetCurrent failed: " + CUResult.fromCode(res));
            }

            return this;
        } catch (Throwable e) {
            throw new SiliconException("setCurrent() failed", e);
        }
    }

    public CudaContext synchronize() {
        try {
            int res = (int) CU_CTX_SYNCHRONIZE.invokeExact();

            if (res != 0) throw new SiliconException("cuCtxSynchronize failed: " + res);

            return this;
        } catch (Throwable e) {
            throw new SiliconException("synchronize() failed", e);
        }
    }
    
    @Override
    public void syncThread() {
        setCurrent();
    }
    
    @Override
    public BackendType backendType() {
        return BackendType.CUDA;
    }

    @Override
    public CudaStream createQueue() {
        return createQueue(null);
    }

    @Override
    public CudaStream createQueue(ComputeArena arena) {
        try (Arena ffmArena = Arena.ofConfined()) {
            MemorySegment streamPtr = ffmArena.allocate(Bindings.CU_STREAM);
            int res = (int) CU_STREAM_CREATE.invokeExact(streamPtr, 0);

            if (res != 0) {
                throw new SiliconException("cuStreamCreate failed: " + CUResult.fromCode(res));
            }

            MemorySegment stream = streamPtr.get(CU_STREAM, 0);
            return new CudaStream(stream);
        } catch (Throwable e) {
            throw new SiliconException("createQueue() failed", e);
        }
    }

    @Override
    public CudaModule loadModule(Path path) {
        if (!Files.exists(path)) {
            throw new IllegalArgumentException(path + " does not exist");
        }

        try (Arena arena = Arena.ofConfined()) {
            byte[] pathBytes = (path + "\0").getBytes(StandardCharsets.UTF_8);

            MemorySegment cPath = arena.allocate(pathBytes.length);
            cPath.copyFrom(MemorySegment.ofArray(pathBytes));

            MemorySegment moduleHandle = (MemorySegment) CUDA_MODULE_LOAD.invoke(cPath);

            if (moduleHandle == null || moduleHandle.address() == 0) {
                throw new SiliconException("cuModuleLoad failed for: " + path);
            }

            return new CudaModule(moduleHandle, this);
        } catch (Throwable e) {
            throw new SiliconException("loadModule(Path) failed", e);
        }
    }

    @Override
    public CudaModule loadModule(byte[] rawSrc) {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment data = arena.allocate(rawSrc.length);
            data.copyFrom(MemorySegment.ofArray(rawSrc));
            MemorySegment moduleHandle = (MemorySegment) CUDA_MODULE_LOAD_DATA.invoke(data);

            if (moduleHandle == null || moduleHandle.address() == 0) {
                throw new SiliconException("cuModuleLoadData failed");
            }

            return new CudaModule(moduleHandle, this);
        } catch (Throwable e) {
            throw new SiliconException("loadModule(byte[]) failed", e);
        }
    }

    @Override
    public CudaModule loadModule(String source) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public CudaBuffer allocateBytes(long size) {
        try {
            MemorySegment ptr = (MemorySegment) CUDA_MEM_ALLOC.invoke(size);

            if (ptr == null || ptr.address() == 0) {
                throw new SiliconException("cuMemAlloc failed: " + ptr);
            }

            return new CudaBuffer(this, ptr, size);
        } catch (Throwable e) {
            throw new SiliconException("allocateBytes(long) failed", e);
        }
    }

    @Override
    public CudaBuffer allocateArray(byte[] data) {
        CudaBuffer buffer = allocateBytes(data.length);
        buffer.write(data);
        return buffer;
    }

    @Override
    public CudaBuffer allocateArray(double[] data) {
        CudaBuffer buffer = allocateBytes(data.length * ValueLayout.JAVA_DOUBLE.byteSize());
        buffer.write(data);
        return buffer;
    }

    @Override
    public CudaBuffer allocateArray(float[] data) {
        CudaBuffer buffer = allocateBytes(data.length * ValueLayout.JAVA_FLOAT.byteSize());
        buffer.write(data);
        return buffer;
    }

    @Override
    public CudaBuffer allocateArray(long[] data) {
        CudaBuffer buffer = allocateBytes(data.length * ValueLayout.JAVA_LONG.byteSize());
        buffer.write(data);
        return buffer;
    }

    @Override
    public CudaBuffer allocateArray(int[] data) {
        CudaBuffer buffer = allocateBytes(data.length * ValueLayout.JAVA_INT.byteSize());
        buffer.write(data);
        return buffer;
    }

    @Override
    public CudaBuffer allocateArray(short[] data) {
        CudaBuffer buffer = allocateBytes(data.length * ValueLayout.JAVA_SHORT.byteSize());
        buffer.write(data);
        return buffer;
    }
}
