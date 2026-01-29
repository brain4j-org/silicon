package org.silicon.cuda.device;

import org.silicon.SiliconException;
import org.silicon.memory.BufferState;
import org.silicon.computing.ComputeQueue;
import org.silicon.cuda.CudaObject;
import org.silicon.cuda.computing.CudaStream;
import org.silicon.device.ComputeBuffer;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;

public class CudaBuffer implements CudaObject, ComputeBuffer {

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
    public static final MethodHandle CUDA_MEMCPY_DTOD_ASYNC = LINKER.downcallHandle(
        LOOKUP.find("cuda_memcpy_dtod_async").orElse(null),
        FunctionDescriptor.of(ValueLayout.JAVA_INT,
            ValueLayout.ADDRESS, // destination pointer
            ValueLayout.ADDRESS, // source pointer
            ValueLayout.JAVA_LONG, // size
            ValueLayout.ADDRESS) // stream
    );

    private final CudaContext context;
    private final MemorySegment handle;
    private final long size;
    private BufferState state;

    public CudaBuffer(CudaContext context, MemorySegment handle, long size) {
        this.context = context;
        this.handle = handle;
        this.size = size;
        this.state = BufferState.ALIVE;
    }

    @Override
    public BufferState getState() {
        return state;
    }

    @Override
    public CudaBuffer copy() {
        CudaBuffer buffer = context.allocateBytes(size);
        return copyInto(buffer);
    }

    @Override
    public CudaBuffer copyInto(ComputeBuffer other) {
        try {
            CudaBuffer buffer = (CudaBuffer) other;

            int res = (int) CUDA_MEMCPY_DTOD.invoke(buffer.handle, handle, size);
            if (res != 0) throw new RuntimeException("cuMemcpyDtoD failed: " + res);

            return buffer;
        } catch (Throwable e) {
            throw new SiliconException("copyInto(ComputeBuffer) failed", e);
        }
    }

    @Override
    public CudaBuffer copyAsync(ComputeQueue queue) {
        CudaBuffer buffer = context.allocateBytes(size);
        return copyIntoAsync(buffer, queue);
    }

    @Override
    public CudaBuffer copyIntoAsync(ComputeBuffer other, ComputeQueue queue) {
        try {
            CudaBuffer buffer = (CudaBuffer) other;
            CudaStream stream = (CudaStream) queue;

            int res = (int) CUDA_MEMCPY_DTOD_ASYNC.invoke(buffer.handle, handle, size, stream.handle());
            if (res != 0) throw new RuntimeException("cuMemcpyDtoD failed: " + res);

            return buffer;
        } catch (Throwable e) {
            throw new SiliconException("copyIntoAsync(ComputeBuffer, ComputeQueue) failed", e);
        }
    }

    @Override
    public void free() {
        try {
            this.state = BufferState.PENDING_FREE;

            try {
                int res = (int) CUDA_MEM_FREE.invoke(handle);
                if (res != 0) throw new RuntimeException("cuMemFree failed: " + res);
            } finally {
                this.state = BufferState.FREE;
            }
        } catch (Throwable e) {
            throw new SiliconException("free() failed", e);
        }
    }

    @Override
    public void get(byte[] data) {
        if (state != BufferState.ALIVE) {
            throw new IllegalStateException("Buffer is not ALIVE! Current buffer state: " + state);
        }

        long size = bytesOf(data);
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment host = arena.allocate(ValueLayout.JAVA_BYTE, data.length);

            int res = (int) CUDA_MEMCPY_DTOH.invoke(host, handle, size);
            if (res != 0) throw new RuntimeException("cuMemcpyDtoH failed: " + res);

            MemorySegment.copy(host, 0, MemorySegment.ofArray(data), 0, size);
        } catch (Throwable e) {
            throw new SiliconException("get(byte[]) failed", e);
        }
    }

    @Override
    public void get(double[] data) {
        if (state != BufferState.ALIVE) {
            throw new IllegalStateException("Buffer is not ALIVE! Current buffer state: " + state);
        }

        long size = bytesOf(data);
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment host = arena.allocate(ValueLayout.JAVA_DOUBLE, data.length);

            int res = (int) CUDA_MEMCPY_DTOH.invoke(host, handle, size);
            if (res != 0) throw new RuntimeException("cuMemcpyDtoH failed: " + res);

            MemorySegment.copy(host, 0, MemorySegment.ofArray(data), 0, size);
        } catch (Throwable e) {
            throw new SiliconException("get(double[]) failed", e);
        }
    }

    @Override
    public void get(float[] data) {
        if (state != BufferState.ALIVE) {
            throw new IllegalStateException("Buffer is not ALIVE! Current buffer state: " + state);
        }

        long size = bytesOf(data);
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment host = arena.allocate(ValueLayout.JAVA_FLOAT, data.length);

            int res = (int) CUDA_MEMCPY_DTOH.invoke(host, handle, size);
            if (res != 0) throw new RuntimeException("cuMemcpyDtoH failed: " + res);

            MemorySegment.copy(host, 0, MemorySegment.ofArray(data), 0, size);
        } catch (Throwable e) {
            throw new SiliconException("get(float[]) failed", e);
        }
    }

    @Override
    public void get(long[] data) {
        if (state != BufferState.ALIVE) {
            throw new IllegalStateException("Buffer is not ALIVE! Current buffer state: " + state);
        }

        long size = bytesOf(data);
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment host = arena.allocate(ValueLayout.JAVA_LONG, data.length);

            int res = (int) CUDA_MEMCPY_DTOH.invoke(host, handle, size);
            if (res != 0) throw new RuntimeException("cuMemcpyDtoH failed: " + res);

            MemorySegment.copy(host, 0, MemorySegment.ofArray(data), 0, size);
        } catch (Throwable e) {
            throw new SiliconException("get(long[]) failed", e);
        }
    }

    @Override
    public void get(int[] data) {
        if (state != BufferState.ALIVE) {
            throw new IllegalStateException("Buffer is not ALIVE! Current buffer state: " + state);
        }

        long size = bytesOf(data);
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment host = arena.allocate(ValueLayout.JAVA_INT, data.length);

            int res = (int) CUDA_MEMCPY_DTOH.invoke(host, handle, size);
            if (res != 0) throw new RuntimeException("cuMemcpyDtoH failed: " + res);

            MemorySegment.copy(host, 0, MemorySegment.ofArray(data), 0, size);
        } catch (Throwable e) {
            throw new SiliconException("get(int[]) failed", e);
        }
    }

    @Override
    public void get(short[] data) {
        if (state != BufferState.ALIVE) {
            throw new IllegalStateException("Buffer is not ALIVE! Current buffer state: " + state);
        }

        long size = bytesOf(data);
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment host = arena.allocate(ValueLayout.JAVA_SHORT, data.length);

            int res = (int) CUDA_MEMCPY_DTOH.invoke(host, handle, size);
            if (res != 0) throw new RuntimeException("cuMemcpyDtoH failed: " + res);

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
        if (state != BufferState.ALIVE) {
            throw new IllegalStateException("Buffer is not ALIVE! Current buffer state: " + state);
        }

        long size = bytesOf(data);
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment host = arena.allocateFrom(ValueLayout.JAVA_BYTE, data);

            int res = (int) CUDA_MEMCPY_HTOD.invoke(handle, host, size);
            if (res != 0) throw new RuntimeException("cuMemcpyHtoD failed: " + res);
        } catch (Throwable e) {
            throw new SiliconException("copyToDevice(byte[]) failed", e);
        }
    }

    public void copyToDevice(double[] data) {
        if (state != BufferState.ALIVE) {
            throw new IllegalStateException("Buffer is not ALIVE! Current buffer state: " + state);
        }

        long size = bytesOf(data);
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment host = arena.allocateFrom(ValueLayout.JAVA_DOUBLE, data);

            int res = (int) CUDA_MEMCPY_HTOD.invoke(handle, host, size);
            if (res != 0) throw new RuntimeException("cuMemcpyHtoD failed: " + res);
        } catch (Throwable e) {
            throw new SiliconException("copyToDevice(double[]) failed", e);
        }
    }

    public void copyToDevice(float[] data) {
        if (state != BufferState.ALIVE) {
            throw new IllegalStateException("Buffer is not ALIVE! Current buffer state: " + state);
        }

        long size = bytesOf(data);
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment host = arena.allocateFrom(ValueLayout.JAVA_FLOAT, data);

            int res = (int) CUDA_MEMCPY_HTOD.invoke(handle, host, size);
            if (res != 0) throw new RuntimeException("cuMemcpyHtoD failed: " + res);
        } catch (Throwable e) {
            throw new SiliconException("copyToDevice(float[]) failed", e);
        }
    }

    public void copyToDevice(long[] data) {
        if (state != BufferState.ALIVE) {
            throw new IllegalStateException("Buffer is not ALIVE! Current buffer state: " + state);
        }

        long size = bytesOf(data);
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment host = arena.allocateFrom(ValueLayout.JAVA_LONG, data);

            int res = (int) CUDA_MEMCPY_HTOD.invoke(handle, host, size);
            if (res != 0) throw new RuntimeException("cuMemcpyHtoD failed: " + res);
        } catch (Throwable e) {
            throw new SiliconException("copyToDevice(long[]) failed", e);
        }
    }

    public void copyToDevice(int[] data) {
        if (state != BufferState.ALIVE) {
            throw new IllegalStateException("Buffer is not ALIVE! Current buffer state: " + state);
        }

        long size = bytesOf(data);
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment host = arena.allocateFrom(ValueLayout.JAVA_INT, data);

            int res = (int) CUDA_MEMCPY_HTOD.invoke(handle, host, size);
            if (res != 0) throw new RuntimeException("cuMemcpyHtoD failed: " + res);
        } catch (Throwable e) {
            throw new SiliconException("copyToDevice(int[]) failed", e);
        }
    }

    public void copyToDevice(short[] data) {
        if (state != BufferState.ALIVE) {
            throw new IllegalStateException("Buffer is not ALIVE! Current buffer state: " + state);
        }

        long size = bytesOf(data);
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment host = arena.allocateFrom(ValueLayout.JAVA_SHORT, data);

            int res = (int) CUDA_MEMCPY_HTOD.invoke(handle, host, size);
            if (res != 0) throw new RuntimeException("cuMemcpyHtoD failed: " + res);
        } catch (Throwable e) {
            throw new SiliconException("copyToDevice(short[]) failed", e);
        }
    }

    // ========================= ASYNC COPY TO DEVICE =========================
    public void copyToDeviceAsync(byte[] data, CudaStream stream) {
        if (state != BufferState.ALIVE) {
            throw new IllegalStateException("Buffer is not ALIVE! Current buffer state: " + state);
        }

        long size = bytesOf(data);
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment host = arena.allocateFrom(ValueLayout.JAVA_BYTE, data);

            int res = (int) CUDA_MEMCPY_HTOD_ASYNC.invoke(handle, host, size, stream.handle());
            if (res != 0) throw new RuntimeException("cuMemcpyHtoDAsync failed: " + res);
        } catch (Throwable e) {
            throw new SiliconException("copyToDeviceAsync(byte[], CudaStream) failed", e);
        }
    }

    public void copyToDeviceAsync(double[] data, CudaStream stream) {
        if (state != BufferState.ALIVE) {
            throw new IllegalStateException("Buffer is not ALIVE! Current buffer state: " + state);
        }

        long size = bytesOf(data);
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment host = arena.allocateFrom(ValueLayout.JAVA_DOUBLE, data);

            int res = (int) CUDA_MEMCPY_HTOD_ASYNC.invoke(handle, host, size, stream.handle());
            if (res != 0) throw new RuntimeException("cuMemcpyHtoDAsync failed: " + res);
        } catch (Throwable e) {
            throw new SiliconException("copyToDeviceAsync(double[], CudaStream) failed", e);
        }
    }

    public void copyToDeviceAsync(float[] data, CudaStream stream) {
        if (state != BufferState.ALIVE) {
            throw new IllegalStateException("Buffer is not ALIVE! Current buffer state: " + state);
        }

        long size = bytesOf(data);
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment host = arena.allocateFrom(ValueLayout.JAVA_FLOAT, data);

            int res = (int) CUDA_MEMCPY_HTOD_ASYNC.invoke(handle, host, size, stream.handle());
            if (res != 0) throw new RuntimeException("cuMemcpyHtoDAsync failed: " + res);
        } catch (Throwable e) {
            throw new SiliconException("copyToDeviceAsync(float[], CudaStream) failed", e);
        }
    }

    public void copyToDeviceAsync(long[] data, CudaStream stream) {
        if (state != BufferState.ALIVE) {
            throw new IllegalStateException("Buffer is not ALIVE! Current buffer state: " + state);
        }

        long size = bytesOf(data);
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment host = arena.allocateFrom(ValueLayout.JAVA_LONG, data);

            int res = (int) CUDA_MEMCPY_HTOD_ASYNC.invoke(handle, host, size, stream.handle());
            if (res != 0) throw new RuntimeException("cuMemcpyHtoDAsync failed: " + res);
        } catch (Throwable e) {
            throw new SiliconException("copyToDeviceAsync(long[], CudaStream) failed", e);
        }
    }

    public void copyToDeviceAsync(int[] data, CudaStream stream) {
        if (state != BufferState.ALIVE) {
            throw new IllegalStateException("Buffer is not ALIVE! Current buffer state: " + state);
        }

        long size = bytesOf(data);
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment host = arena.allocateFrom(ValueLayout.JAVA_INT, data);

            int res = (int) CUDA_MEMCPY_HTOD_ASYNC.invoke(handle, host, size, stream.handle());
            if (res != 0) throw new RuntimeException("cuMemcpyHtoDAsync failed: " + res);
        } catch (Throwable e) {
            throw new SiliconException("copyToDeviceAsync(int[], CudaStream) failed", e);
        }
    }

    public void copyToDeviceAsync(short[] data, CudaStream stream) {
        if (state != BufferState.ALIVE) {
            throw new IllegalStateException("Buffer is not ALIVE! Current buffer state: " + state);
        }

        long size = bytesOf(data);
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment host = arena.allocateFrom(ValueLayout.JAVA_SHORT, data);

            int res = (int) CUDA_MEMCPY_HTOD_ASYNC.invoke(handle, host, size, stream.handle());
            if (res != 0) throw new RuntimeException("cuMemcpyHtoDAsync failed: " + res);
        } catch (Throwable e) {
            throw new SiliconException("copyToDeviceAsync(short[], CudaStream) failed", e);
        }
    }

    public long getNativePointer() {
        try {
            return (long) CUDA_BUFFER_PTR.invoke(handle);
        } catch (Throwable e) {
            throw new SiliconException("getNativePointer() failed", e);
        }
    }

    public CudaContext getContext() {
        return context;
    }

    public long getSize() {
        return size;
    }

    @Override
    public String toString() {
        return "CudaBuffer[" +
            "context=" + context + ", " +
            "handle=" + handle + ", " +
            "size=" + size + ']';
    }
}
