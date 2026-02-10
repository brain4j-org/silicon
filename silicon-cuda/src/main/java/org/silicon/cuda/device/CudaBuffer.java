package org.silicon.cuda.device;

import org.silicon.api.SiliconException;
import org.silicon.api.device.ComputeBuffer;
import org.silicon.api.memory.Freeable;
import org.silicon.api.memory.MemoryState;
import org.silicon.cuda.CudaObject;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;

public class CudaBuffer implements CudaObject, ComputeBuffer, Freeable {

    public static final MethodHandle CUDA_BUFFER_PTR = CudaObject.find(
        "cuda_buffer_ptr",
        FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.ADDRESS)
    );
    public static final MethodHandle CUDA_MEM_FREE = CudaObject.find(
        "cuda_mem_free",
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS)
    );
    public static final MethodHandle CUDA_MEMCPY_HTOD = CudaObject.find(
        "cuda_memcpy_htod",
        FunctionDescriptor.of(ValueLayout.JAVA_INT,
            ValueLayout.ADDRESS, // buffer pointer
            ValueLayout.ADDRESS, // host pointer
            ValueLayout.JAVA_LONG) // size
    );
    public static final MethodHandle CUDA_MEMCPY_DTOH = CudaObject.find(
        "cuda_memcpy_dtoh",
        FunctionDescriptor.of(ValueLayout.JAVA_INT,
            ValueLayout.ADDRESS, // host pointer
            ValueLayout.ADDRESS, // buffer pointer
            ValueLayout.JAVA_LONG) // size
    );
    public static final MethodHandle CUDA_MEMCPY_DTOD = CudaObject.find(
        "cuda_memcpy_dtod",
        FunctionDescriptor.of(ValueLayout.JAVA_INT,
            ValueLayout.ADDRESS, // destination pointer
            ValueLayout.ADDRESS, // source pointer
            ValueLayout.JAVA_LONG) // size
    );
    public static final MethodHandle CUDA_MEMCPY_HTOD_ASYNC = CudaObject.find(
        "cuda_memcpy_htod_async",
        FunctionDescriptor.of(ValueLayout.JAVA_INT,
            ValueLayout.ADDRESS, // buffer pointer
            ValueLayout.ADDRESS, // host pointer
            ValueLayout.JAVA_LONG, // size
            ValueLayout.ADDRESS) // stream
    );
    public static final MethodHandle CUDA_MEMCPY_DTOH_ASYNC = CudaObject.find(
        "cuda_memcpy_dtoh_async",
        FunctionDescriptor.of(ValueLayout.JAVA_INT,
            ValueLayout.ADDRESS, // host pointer
            ValueLayout.ADDRESS, // buffer pointer
            ValueLayout.JAVA_LONG, // size
            ValueLayout.ADDRESS) // stream
    );
    public static final MethodHandle CUDA_MEMCPY_DTOD_ASYNC = CudaObject.find(
        "cuda_memcpy_dtod_async",
        FunctionDescriptor.of(ValueLayout.JAVA_INT,
            ValueLayout.ADDRESS, // destination pointer
            ValueLayout.ADDRESS, // source pointer
            ValueLayout.JAVA_LONG, // size
            ValueLayout.ADDRESS) // stream
    );

    private final CudaContext context;
    private final MemorySegment handle;
    private final long size;
    private MemoryState state;

    public CudaBuffer(CudaContext context, MemorySegment handle, long size) {
        this.context = context;
        this.handle = handle;
        this.size = size;
        this.state = MemoryState.ALIVE;
    }

    @Override
    public CudaBuffer copy() {
        CudaBuffer buffer = context.allocateBytes(size);
        return copyInto(buffer);
    }

    @Override
    public CudaBuffer copyInto(ComputeBuffer other) {
        ensureAlive();
        ensureOther(other);

        if (!(other instanceof CudaBuffer buffer)) throw new IllegalArgumentException("Other buffer is not a CUDA buffer");

        try {
            int res = (int) CUDA_MEMCPY_DTOD.invoke(buffer.handle, handle, size);
            if (res != 0) throw new SiliconException("cuMemcpyDtoD failed: " + res);

            return buffer;
        } catch (Throwable e) {
            throw new SiliconException("copyInto(ComputeBuffer) failed", e);
        }
    }

    @Override
    public MemoryState state() {
        return state;
    }

    @Override
    public void free() {
        if (!isAlive()) return;
        
        try {
            int res = (int) CUDA_MEM_FREE.invoke(handle);
            if (res != 0) throw new SiliconException("cuMemFree failed: " + res);

            state = MemoryState.FREE;
        } catch (Throwable e) {
            throw new SiliconException("free() failed", e);
        }
    }

    @Override
    public void get(byte[] data) {
        ensureAlive();
        
        long size = bytesOf(data);
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment host = arena.allocate(ValueLayout.JAVA_BYTE, data.length);

            int res = (int) CUDA_MEMCPY_DTOH.invoke(host, handle, size);
            if (res != 0) throw new SiliconException("cuMemcpyDtoH failed: " + res);

            MemorySegment.copy(host, 0, MemorySegment.ofArray(data), 0, size);
        } catch (Throwable e) {
            throw new SiliconException("get(byte[]) failed", e);
        }
    }

    @Override
    public void get(double[] data) {
        ensureAlive();

        long size = bytesOf(data);
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment host = arena.allocate(ValueLayout.JAVA_DOUBLE, data.length);

            int res = (int) CUDA_MEMCPY_DTOH.invoke(host, handle, size);
            if (res != 0) throw new SiliconException("cuMemcpyDtoH failed: " + res);

            MemorySegment.copy(host, 0, MemorySegment.ofArray(data), 0, size);
        } catch (Throwable e) {
            throw new SiliconException("get(double[]) failed", e);
        }
    }

    @Override
    public void get(float[] data) {
        ensureAlive();

        long size = bytesOf(data);
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment host = arena.allocate(ValueLayout.JAVA_FLOAT, data.length);

            int res = (int) CUDA_MEMCPY_DTOH.invoke(host, handle, size);
            if (res != 0) throw new SiliconException("cuMemcpyDtoH failed: " + res);

            MemorySegment.copy(host, 0, MemorySegment.ofArray(data), 0, size);
        } catch (Throwable e) {
            throw new SiliconException("get(float[]) failed", e);
        }
    }

    @Override
    public void get(long[] data) {
        ensureAlive();

        long size = bytesOf(data);
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment host = arena.allocate(ValueLayout.JAVA_LONG, data.length);

            int res = (int) CUDA_MEMCPY_DTOH.invoke(host, handle, size);
            if (res != 0) throw new SiliconException("cuMemcpyDtoH failed: " + res);

            MemorySegment.copy(host, 0, MemorySegment.ofArray(data), 0, size);
        } catch (Throwable e) {
            throw new SiliconException("get(long[]) failed", e);
        }
    }

    @Override
    public void get(int[] data) {
        ensureAlive();

        long size = bytesOf(data);
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment host = arena.allocate(ValueLayout.JAVA_INT, data.length);

            int res = (int) CUDA_MEMCPY_DTOH.invoke(host, handle, size);
            if (res != 0) throw new SiliconException("cuMemcpyDtoH failed: " + res);

            MemorySegment.copy(host, 0, MemorySegment.ofArray(data), 0, size);
        } catch (Throwable e) {
            throw new SiliconException("get(int[]) failed", e);
        }
    }

    @Override
    public void get(short[] data) {
        ensureAlive();

        long size = bytesOf(data);
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment host = arena.allocate(ValueLayout.JAVA_SHORT, data.length);

            int res = (int) CUDA_MEMCPY_DTOH.invoke(host, handle, size);
            if (res != 0) throw new SiliconException("cuMemcpyDtoH failed: " + res);

            MemorySegment.copy(host, 0, MemorySegment.ofArray(data), 0, size);
        } catch (Throwable e) {
            throw new SiliconException("get(short[]) failed", e);
        }
    }

    @Override
    public MemorySegment handle() {
        return handle;
    }

    // ========================= COPY TO DEVICE =========================
    public void copyToDevice(byte[] data) {
        ensureAlive();

        long size = bytesOf(data);
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment host = arena.allocateFrom(ValueLayout.JAVA_BYTE, data);

            int res = (int) CUDA_MEMCPY_HTOD.invoke(handle, host, size);
            if (res != 0) throw new SiliconException("cuMemcpyHtoD failed: " + res);
        } catch (Throwable e) {
            throw new SiliconException("copyToDevice(byte[]) failed", e);
        }
    }

    public void copyToDevice(double[] data) {
        ensureAlive();

        long size = bytesOf(data);
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment host = arena.allocateFrom(ValueLayout.JAVA_DOUBLE, data);

            int res = (int) CUDA_MEMCPY_HTOD.invoke(handle, host, size);
            if (res != 0) throw new SiliconException("cuMemcpyHtoD failed: " + res);
        } catch (Throwable e) {
            throw new SiliconException("copyToDevice(double[]) failed", e);
        }
    }

    public void copyToDevice(float[] data) {
        ensureAlive();

        long size = bytesOf(data);
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment host = arena.allocateFrom(ValueLayout.JAVA_FLOAT, data);

            int res = (int) CUDA_MEMCPY_HTOD.invoke(handle, host, size);
            if (res != 0) throw new SiliconException("cuMemcpyHtoD failed: " + res);
        } catch (Throwable e) {
            throw new SiliconException("copyToDevice(float[]) failed", e);
        }
    }

    public void copyToDevice(long[] data) {
        ensureAlive();

        long size = bytesOf(data);
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment host = arena.allocateFrom(ValueLayout.JAVA_LONG, data);

            int res = (int) CUDA_MEMCPY_HTOD.invoke(handle, host, size);
            if (res != 0) throw new SiliconException("cuMemcpyHtoD failed: " + res);
        } catch (Throwable e) {
            throw new SiliconException("copyToDevice(long[]) failed", e);
        }
    }

    public void copyToDevice(int[] data) {
        ensureAlive();

        long size = bytesOf(data);
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment host = arena.allocateFrom(ValueLayout.JAVA_INT, data);

            int res = (int) CUDA_MEMCPY_HTOD.invoke(handle, host, size);
            if (res != 0) throw new SiliconException("cuMemcpyHtoD failed: " + res);
        } catch (Throwable e) {
            throw new SiliconException("copyToDevice(int[]) failed", e);
        }
    }

    public void copyToDevice(short[] data) {
        ensureAlive();

        long size = bytesOf(data);
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment host = arena.allocateFrom(ValueLayout.JAVA_SHORT, data);

            int res = (int) CUDA_MEMCPY_HTOD.invoke(handle, host, size);
            if (res != 0) throw new SiliconException("cuMemcpyHtoD failed: " + res);
        } catch (Throwable e) {
            throw new SiliconException("copyToDevice(short[]) failed", e);
        }
    }

    public long nativePointer() {
        try {
            return (long) CUDA_BUFFER_PTR.invoke(handle);
        } catch (Throwable e) {
            throw new SiliconException("getNativePointer() failed", e);
        }
    }

    public CudaContext context() {
        return context;
    }

    public long size() {
        return size;
    }

    @Override
    public String toString() {
        return "CudaBuffer{" +
            "context=" + context +
            ", handle=" + handle +
            ", size=" + size +
            ", state=" + state +
            '}';
    }
}
