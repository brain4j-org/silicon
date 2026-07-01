package org.silicon.cuda.device;

import org.silicon.api.SiliconException;
import org.silicon.api.device.ComputeBuffer;
import org.silicon.api.memory.Freeable;
import org.silicon.api.memory.MemoryState;
import org.silicon.cuda.CUResult;
import org.silicon.cuda.CudaObject;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

import static org.silicon.cuda.Bindings.*;

public class CudaBuffer implements CudaObject, ComputeBuffer, Freeable {

    private final CudaContext context;
    private final long devicePtr;
    private final long size;
    private MemoryState state;

    public CudaBuffer(CudaContext context, long devicePtr, long size) {
        this.context = context;
        this.devicePtr = devicePtr;
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

        if (!(other instanceof CudaBuffer buffer))
            throw new IllegalArgumentException("Other buffer is not a CUDA buffer");

        try {
            int res = (int) CU_MEMCPY_DTOD.invokeExact(buffer.devicePtr, devicePtr, size);
            if (res != 0) {
                throw new SiliconException("cuMemcpyDtoD failed: " + CUResult.fromCode(res));
            }

            return buffer;
        } catch (Throwable e) {
            throw new SiliconException("copyInto(ComputeBuffer) failed", e);
        }
    }

    @Override
    public long size() {
        return size;
    }

    @Override
    public MemoryState state() {
        return state;
    }

    @Override
    public void free() {
        if (!isAlive()) return;

        try {
            int res = (int) CU_MEM_FREE.invokeExact(devicePtr);
            if (res != 0) {
                throw new SiliconException("cuMemFree failed: " + CUResult.fromCode(res));
            }

            state = MemoryState.FREE;
        } catch (Throwable e) {
            throw new SiliconException("free() failed", e);
        }
    }

    @Override
    public byte[] get(byte[] data) {
        ensureAlive();

        long transferSize = bytesOf(data);
        ensureCapacity(transferSize);
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment host = arena.allocate(transferSize);

            int res = (int) CU_MEMCPY_DTOH.invokeExact(host, devicePtr, transferSize);
            if (res != 0) {
                throw new SiliconException("cuMemcpyDtoH failed: " + CUResult.fromCode(res));
            }

            MemorySegment.copy(host, 0, MemorySegment.ofArray(data), 0, transferSize);
        } catch (Throwable e) {
            throw new SiliconException("get(byte[]) failed", e);
        }
        return data;
    }

    @Override
    public double[] get(double[] data) {
        ensureAlive();

        long transferSize = bytesOf(data);
        ensureCapacity(transferSize);
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment host = arena.allocate(transferSize);

            int res = (int) CU_MEMCPY_DTOH.invokeExact(host, devicePtr, transferSize);
            if (res != 0) {
                throw new SiliconException("cuMemcpyDtoH failed: " + CUResult.fromCode(res));
            }

            MemorySegment.copy(host, 0, MemorySegment.ofArray(data), 0, transferSize);
        } catch (Throwable e) {
            throw new SiliconException("get(double[]) failed", e);
        }
        return data;
    }

    @Override
    public float[] get(float[] data) {
        ensureAlive();

        long transferSize = bytesOf(data);
        ensureCapacity(transferSize);
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment host = arena.allocate(transferSize);

            int res = (int) CU_MEMCPY_DTOH.invokeExact(host, devicePtr, transferSize);
            if (res != 0) {
                throw new SiliconException("cuMemcpyDtoH failed: " + CUResult.fromCode(res));
            }

            MemorySegment.copy(host, 0, MemorySegment.ofArray(data), 0, transferSize);
        } catch (Throwable e) {
            throw new SiliconException("get(float[]) failed", e);
        }
        return data;
    }

    @Override
    public long[] get(long[] data) {
        ensureAlive();

        long transferSize = bytesOf(data);
        ensureCapacity(transferSize);
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment host = arena.allocate(transferSize);

            int res = (int) CU_MEMCPY_DTOH.invokeExact(host, devicePtr, transferSize);
            if (res != 0) {
                throw new SiliconException("cuMemcpyDtoH failed: " + CUResult.fromCode(res));
            }

            MemorySegment.copy(host, 0, MemorySegment.ofArray(data), 0, transferSize);
        } catch (Throwable e) {
            throw new SiliconException("get(long[]) failed", e);
        }
        return data;
    }

    @Override
    public int[] get(int[] data) {
        ensureAlive();

        long transferSize = bytesOf(data);
        ensureCapacity(transferSize);
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment host = arena.allocate(transferSize);

            int res = (int) CU_MEMCPY_DTOH.invokeExact(host, devicePtr, transferSize);
            if (res != 0) {
                throw new SiliconException("cuMemcpyDtoH failed: " + CUResult.fromCode(res));
            }

            MemorySegment.copy(host, 0, MemorySegment.ofArray(data), 0, transferSize);
        } catch (Throwable e) {
            throw new SiliconException("get(int[]) failed", e);
        }
        return data;
    }

    @Override
    public short[] get(short[] data) {
        ensureAlive();

        long transferSize = bytesOf(data);
        ensureCapacity(transferSize);
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment host = arena.allocate(transferSize);

            int res = (int) CU_MEMCPY_DTOH.invokeExact(host, devicePtr, transferSize);
            if (res != 0) {
                throw new SiliconException("cuMemcpyDtoH failed: " + CUResult.fromCode(res));
            }

            MemorySegment.copy(host, 0, MemorySegment.ofArray(data), 0, transferSize);
        } catch (Throwable e) {
            throw new SiliconException("get(short[]) failed", e);
        }
        return data;
    }

    @Override
    public void write(byte[] data) {
        ensureAlive();

        long transferSize = bytesOf(data);
        ensureCapacity(transferSize);
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment host = arena.allocate(transferSize);
            host.copyFrom(MemorySegment.ofArray(data));

            int res = (int) CU_MEMCPY_HTOD.invokeExact(devicePtr, host, transferSize);
            if (res != 0) {
                throw new SiliconException("cuMemcpyHtoD failed: " + CUResult.fromCode(res));
            }
        } catch (Throwable e) {
            throw new SiliconException("write(byte[]) failed", e);
        }
    }

    @Override
    public void write(double[] data) {
        ensureAlive();

        long transferSize = bytesOf(data);
        ensureCapacity(transferSize);
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment host = arena.allocate(transferSize);
            host.copyFrom(MemorySegment.ofArray(data));

            int res = (int) CU_MEMCPY_HTOD.invokeExact(devicePtr, host, transferSize);
            if (res != 0) {
                throw new SiliconException("cuMemcpyHtoD failed: " + CUResult.fromCode(res));
            }
        } catch (Throwable e) {
            throw new SiliconException("write(double[]) failed", e);
        }
    }

    @Override
    public void write(float[] data) {
        ensureAlive();

        long transferSize = bytesOf(data);
        ensureCapacity(transferSize);
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment host = arena.allocate(transferSize);
            host.copyFrom(MemorySegment.ofArray(data));

            int res = (int) CU_MEMCPY_HTOD.invokeExact(devicePtr, host, transferSize);
            if (res != 0) {
                throw new SiliconException("cuMemcpyHtoD failed: " + CUResult.fromCode(res));
            }
        } catch (Throwable e) {
            throw new SiliconException("write(float[]) failed", e);
        }
    }

    @Override
    public void write(long[] data) {
        ensureAlive();

        long transferSize = bytesOf(data);
        ensureCapacity(transferSize);
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment host = arena.allocate(transferSize);
            host.copyFrom(MemorySegment.ofArray(data));

            int res = (int) CU_MEMCPY_HTOD.invokeExact(devicePtr, host, transferSize);
            if (res != 0) {
                throw new SiliconException("cuMemcpyHtoD failed: " + CUResult.fromCode(res));
            }
        } catch (Throwable e) {
            throw new SiliconException("write(long[]) failed", e);
        }
    }

    @Override
    public void write(int[] data) {
        ensureAlive();

        long transferSize = bytesOf(data);
        ensureCapacity(transferSize);
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment host = arena.allocate(transferSize);
            host.copyFrom(MemorySegment.ofArray(data));

            int res = (int) CU_MEMCPY_HTOD.invokeExact(devicePtr, host, transferSize);
            if (res != 0) {
                throw new SiliconException("cuMemcpyHtoD failed: " + CUResult.fromCode(res));
            }
        } catch (Throwable e) {
            throw new SiliconException("write(int[]) failed", e);
        }
    }

    @Override
    public void write(short[] data) {
        ensureAlive();

        long transferSize = bytesOf(data);
        ensureCapacity(transferSize);
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment host = arena.allocate(transferSize);
            host.copyFrom(MemorySegment.ofArray(data));

            int res = (int) CU_MEMCPY_HTOD.invokeExact(devicePtr, host, transferSize);
            if (res != 0) {
                throw new SiliconException("cuMemcpyHtoD failed: " + CUResult.fromCode(res));
            }
        } catch (Throwable e) {
            throw new SiliconException("write(short[]) failed", e);
        }
    }

    public long nativePointer() {
        return devicePtr;
    }

    public CudaContext context() {
        return context;
    }

    private void ensureCapacity(long transferSize) {
        if (transferSize > size) {
            throw new IllegalArgumentException("Requested transfer of " + transferSize + " bytes, but buffer size is " + size);
        }
    }

    @Override
    public String toString() {
        return "CudaBuffer{" +
            "context=" + context +
            ", devicePtr=0x" + Long.toHexString(devicePtr) +
            ", size=" + size +
            ", state=" + state +
            '}';
    }
}
