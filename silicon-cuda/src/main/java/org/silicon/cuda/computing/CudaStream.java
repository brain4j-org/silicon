package org.silicon.cuda.computing;

import org.silicon.SiliconException;
import org.silicon.computing.ComputeArgs;
import org.silicon.computing.ComputeQueue;
import org.silicon.computing.ComputeSize;
import org.silicon.cuda.kernel.CudaFunction;
import org.silicon.device.ComputeBuffer;
import org.silicon.kernel.ComputeFunction;
import org.silicon.cuda.CudaObject;
import org.silicon.cuda.device.CudaBuffer;
import org.silicon.cuda.device.CudaPointer;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.util.List;

public record CudaStream(MemorySegment handle) implements CudaObject, ComputeQueue {

    public static final MethodHandle CUDA_STREAM_DESTROY = LINKER.downcallHandle(
        LOOKUP.find("cuda_stream_destroy").orElse(null),
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS)
    );
    public static final MethodHandle CUDA_STREAM_SYNC = LINKER.downcallHandle(
        LOOKUP.find("cuda_stream_sync").orElse(null),
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS)
    );
    private static final MethodHandle CUDA_LAUNCH_KERNEL = LINKER.downcallHandle(
        LOOKUP.find("cuda_launch_kernel").orElse(null),
        FunctionDescriptor.of(
            ValueLayout.JAVA_INT, // return
            ValueLayout.ADDRESS, // function
            ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, // grid
            ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, // block
            ValueLayout.JAVA_INT, // shared mem
            ValueLayout.ADDRESS, // stream
            ValueLayout.ADDRESS // kernel params
        )
    );

    @Override
    public void dispatch(ComputeFunction function, ComputeSize globalSize, ComputeSize groupSize, ComputeArgs args) {
        if (!(function instanceof CudaFunction(MemorySegment funcHandle))) {
            throw new IllegalArgumentException("Compute function is not an CUDA stream!");
        }

        if (groupSize == null) groupSize = new ComputeSize(128, 1, 1); // TODO: better handling

        int gridX = globalSize.x() / groupSize.x();
        int gridY = globalSize.y() / groupSize.y();
        int gridZ = globalSize.z() / groupSize.z();

        List<Object> computeArgs = args.getArgs();
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

        CudaPointer parameters = CudaPointer.from(pointers);

        try {
            int result = (int) CUDA_LAUNCH_KERNEL.invoke(
                funcHandle,
                gridX, gridY, gridZ,
                groupSize.x(), groupSize.y(), groupSize.z(),
                0, handle(),
                args.size() == 0 ? MemorySegment.NULL : parameters.segment()
            );

            if (result != 0) {
                throw new RuntimeException("cuLaunchKernel failed: " + result);
            }
        } catch (Throwable e) {
            throw new SiliconException("dispatch(ComputeFunction, ComputeSize, ComputeSize, ComputeArgs) failed", e);
        }
    }

    @Override
    public void awaitCompletion() {
        try {
            int res = (int) CUDA_STREAM_SYNC.invoke(handle);
            if (res != 0) throw new RuntimeException("cuStreamSynchronized failed: " + res);
        } catch (Throwable e) {
            throw new SiliconException("awaitCompletion() failed", e);
        }
    }

    @Override
    public void release() {
        try {
            int res = (int) CUDA_STREAM_DESTROY.invoke(handle);
            if (res != 0) throw new RuntimeException("cuStreamDestroy failed: " + res);
            CudaObject.super.release();
        } catch (Throwable e) {
            throw new SiliconException("release() failed", e);
        }
    }
}
