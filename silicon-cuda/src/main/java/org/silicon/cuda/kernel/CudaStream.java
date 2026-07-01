package org.silicon.cuda.kernel;

import org.silicon.api.SiliconException;
import org.silicon.api.device.ComputeBuffer;
import org.silicon.api.function.ComputeFunction;
import org.silicon.api.kernel.ComputeArgs;
import org.silicon.api.kernel.ComputeEvent;
import org.silicon.api.kernel.ComputeQueue;
import org.silicon.api.kernel.ComputeSize;
import org.silicon.api.memory.Freeable;
import org.silicon.api.memory.MemoryState;
import org.silicon.cuda.CudaObject;
import org.silicon.cuda.device.CudaBuffer;
import org.silicon.cuda.device.CudaPointer;
import org.silicon.cuda.function.CudaFunction;

import java.lang.foreign.MemorySegment;
import java.util.List;
import java.util.Objects;

import static org.silicon.cuda.Bindings.*;

public final class CudaStream implements CudaObject, ComputeQueue, Freeable {

    private final MemorySegment handle;
    private MemoryState state;

    public CudaStream(MemorySegment handle) {
        this.handle = handle;
        this.state = MemoryState.ALIVE;
    }

    @Override
    public void dispatch(ComputeFunction function, ComputeSize globalSize, ComputeSize groupSize, ComputeArgs args) {
        ensureAlive();

        if (!(function instanceof CudaFunction(MemorySegment funcHandle))) {
            throw new IllegalArgumentException("Compute function is not an CUDA kernel");
        }

        Objects.requireNonNull(globalSize, "Global size must not be null");
        Objects.requireNonNull(groupSize, "Group size must not be null");

        if (groupSize.total() <= 0) {
            throw new IllegalArgumentException("Invalid group size");
        }

        int localX = Math.min(globalSize.x(), groupSize.x());
        int localY = Math.min(globalSize.y(), groupSize.y());
        int localZ = Math.min(globalSize.z(), groupSize.z());

        int gridX = globalSize.x() / localX;
        int gridY = globalSize.y() / localY;
        int gridZ = globalSize.z() / localZ;

        CudaPointer parameters = getParameters(args);

        try {
            int result = (int) CU_LAUNCH_KERNEL.invokeExact(
                funcHandle,
                gridX, gridY, gridZ,
                groupSize.x(), groupSize.y(), groupSize.z(),
                0, handle,
                args.size() == 0 ? MemorySegment.NULL : parameters.segment(),
                MemorySegment.NULL
            );

            if (result != 0) {
                throw new SiliconException("cuLaunchKernel failed: " + result);
            }
        } catch (Throwable e) {
            throw new SiliconException("dispatch(ComputeFunction, ComputeSize, ComputeSize, ComputeArgs) failed", e);
        }
    }

    @Override
    public ComputeEvent dispatchAsync(
        ComputeFunction function,
        ComputeSize globalSize,
        ComputeSize groupSize,
        ComputeArgs args
    ) {
        ensureAlive();

        if (!(function instanceof CudaFunction(MemorySegment funcHandle))) {
            throw new IllegalArgumentException("Compute function is not an CUDA kernel");
        }

        Objects.requireNonNull(globalSize, "Global size must not be null");
        Objects.requireNonNull(groupSize, "Group size must not be null");

        if (groupSize.total() <= 0) {
            throw new IllegalArgumentException("Invalid group size");
        }

        int localX = Math.min(globalSize.x(), groupSize.x());
        int localY = Math.min(globalSize.y(), groupSize.y());
        int localZ = Math.min(globalSize.z(), groupSize.z());

        int gridX = globalSize.x() / localX;
        int gridY = globalSize.y() / localY;
        int gridZ = globalSize.z() / localZ;

        CudaPointer parameters = getParameters(args);

        try {
            int result = (int) CU_LAUNCH_KERNEL.invokeExact(
                funcHandle,
                gridX, gridY, gridZ,
                groupSize.x(), groupSize.y(), groupSize.z(),
                0, handle,
                args.size() == 0 ? MemorySegment.NULL : parameters.segment(),
                MemorySegment.NULL
            );

            if (result != 0) {
                throw new SiliconException("cuLaunchKernel failed: " + result);
            }

            return new CudaEvent(this);
        } catch (Throwable e) {
            throw new SiliconException("dispatch(ComputeFunction, ComputeSize, ComputeSize, ComputeArgs) failed", e);
        }
    }

    private CudaPointer getParameters(ComputeArgs args) {
        List<Object> computeArgs = args.args();
        CudaPointer[] pointers = new CudaPointer[computeArgs.size()];

        for (int i = 0; i < args.size(); i++) {
            Object arg = computeArgs.get(i);
            switch (arg) {
                case Byte val -> pointers[i] = CudaPointer.fromByte(val);
                case Double val -> pointers[i] = CudaPointer.fromDouble(val);
                case Float val -> pointers[i] = CudaPointer.fromFloat(val);
                case Integer val -> pointers[i] = CudaPointer.fromInt(val);
                case Long val -> pointers[i] = CudaPointer.fromLong(val);
                case Short val -> pointers[i] = CudaPointer.fromShort(val);
                case String val -> pointers[i] = CudaPointer.fromString(val);
                case ComputeBuffer val -> pointers[i] = CudaPointer.fromBuffer((CudaBuffer) val);
                default -> throw new IllegalStateException("Unexpected value: " + arg);
            }
        }

        return CudaPointer.from(pointers);
    }

    @Override
    public void await() {
        ensureAlive();

        try {
            int res = (int) CU_STREAM_SYNCHRONIZE.invokeExact(handle);
            if (res != 0) throw new SiliconException("cuStreamSynchronize failed: " + res);
        } catch (Throwable e) {
            throw new SiliconException("awaitCompletion() failed", e);
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
            int res = (int) CU_STREAM_DESTROY.invokeExact(handle);
            if (res != 0) throw new SiliconException("cuStreamDestroy_v2 failed: " + res);

            state = MemoryState.FREE;
        } catch (Throwable e) {
            throw new SiliconException("free() failed", e);
        }
    }

    public MemorySegment handle() {
        return handle;
    }

    @Override
    public String toString() {
        return "CudaStream{" +
            "handle=" + handle +
            ", state=" + state +
            '}';
    }
}
