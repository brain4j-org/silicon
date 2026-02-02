package org.silicon.opencl.computing;

import org.lwjgl.PointerBuffer;
import org.lwjgl.opencl.CL10;
import org.lwjgl.system.MemoryStack;
import org.silicon.api.SiliconException;
import org.silicon.api.kernel.ComputeArgs;
import org.silicon.api.kernel.ComputeEvent;
import org.silicon.api.kernel.ComputeQueue;
import org.silicon.api.kernel.ComputeSize;
import org.silicon.api.function.ComputeFunction;
import org.silicon.api.memory.MemoryState;
import org.silicon.opencl.device.CLBuffer;
import org.silicon.opencl.kernel.CLKernel;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public final class CLCommandQueue implements ComputeQueue {

    private final long handle;
    private MemoryState state;

    public CLCommandQueue(long handle) {
        this.handle = handle;
        this.state = MemoryState.ALIVE;
    }

    private PointerBuffer toBuffer(ComputeSize size, MemoryStack stack) {
        int dim = size.workDim();
        
        PointerBuffer buf = stack.mallocPointer(dim);
        buf.put(0, size.x());
        
        if (dim > 1) buf.put(1, size.y());
        if (dim > 2) buf.put(2, size.z());
        
        return buf;
    }

    @Override
    public void dispatch(
        ComputeFunction function,
        ComputeSize globalSize,
        ComputeSize groupSize,
        ComputeArgs args
    ) {
        if (!(function instanceof CLKernel(long kernelHandle, long device))) {
            throw new IllegalArgumentException("Compute function is not an OpenCL kernel");
        }
        
        Objects.requireNonNull(globalSize, "Global size must not be null");
        Objects.requireNonNull(groupSize, "Group size must not be null");
        
        if (groupSize.total() <= 0) {
            throw new IllegalArgumentException("Invalid group size");
        }

        try (MemoryStack stack = MemoryStack.stackPush()) {
            setArgs(args, kernelHandle, stack);
            
            ComputeSize fixedGlobal = fixGlobalSize(globalSize, groupSize);

            PointerBuffer globalBuf = toBuffer(fixedGlobal, stack);
            PointerBuffer localBuf = groupSize != null ? toBuffer(groupSize, stack) : null;

            int err = CL10.clEnqueueNDRangeKernel(
                handle,
                kernelHandle, // cl_kernel
                globalSize.workDim(), // work_dim
                null, // global offset
                globalBuf, // global size
                localBuf,
                null, // wait list (in-order queue)
                null // event out
            );
            if (err != CL10.CL_SUCCESS) throw new IllegalStateException("clEnqueueNDRangeKernel failed: " + err);
        }
    }

    @Override
    public ComputeEvent dispatchAsync(
        ComputeFunction function,
        ComputeSize globalSize,
        ComputeSize groupSize,
        ComputeArgs args
    ) {
        if (!(function instanceof CLKernel(long kernelHandle, long device))) {
            throw new IllegalArgumentException("Compute function is not an OpenCL kernel");
        }
        
        Objects.requireNonNull(globalSize, "Global size must not be null");
        Objects.requireNonNull(groupSize, "Group size must not be null");
        
        if (groupSize.total() <= 0) {
            throw new IllegalArgumentException("Invalid group size");
        }
        
        try (MemoryStack stack = MemoryStack.stackPush()) {
            setArgs(args, kernelHandle, stack);
            
            ComputeSize fixedGlobal = fixGlobalSize(globalSize, groupSize);

            PointerBuffer globalBuf = toBuffer(fixedGlobal, stack);
            PointerBuffer localBuf = groupSize != null ? toBuffer(groupSize, stack) : null;

            PointerBuffer eventPtr = stack.mallocPointer(1);
            int err = CL10.clEnqueueNDRangeKernel(
                handle,
                kernelHandle, // cl_kernel
                globalSize.workDim(), // work_dim
                null, // global offset
                globalBuf, // global size
                localBuf,
                null, // wait list (in-order queue)
                eventPtr // event out
            );
            if (err != CL10.CL_SUCCESS) throw new IllegalStateException("clEnqueueNDRangeKernel failed: " + err);

            return new CLEvent(eventPtr.get(0));
        }
    }

    @Override
    public void await() {
        ensureAlive();
        CL10.clFinish(handle);
    }

    @Override
    public MemoryState state() {
        return state;
    }

    @Override
    public void free() {
        if (state != MemoryState.ALIVE) return;

        int err = CL10.clReleaseCommandQueue(handle);

        if (err != 0) throw new SiliconException("clReleaseCommandQueue failed: " + err);

        state = MemoryState.FREE;
    }

    private static void setArgs(ComputeArgs args, long kernelHandle, MemoryStack stack) {
        List<Object> computeArgs = args.getArgs();
        for (int i = 0; i < args.size(); i++) {
            Object arg = computeArgs.get(i);
            switch (arg) {
                case Byte val -> CL10.clSetKernelArg(kernelHandle, i, stack.bytes(val));
                case Double val -> CL10.clSetKernelArg(kernelHandle, i, stack.doubles(val));
                case Float val -> CL10.clSetKernelArg(kernelHandle, i, stack.floats(val));
                case Integer val -> CL10.clSetKernelArg(kernelHandle, i, stack.ints(val));
                case Long val -> CL10.clSetKernelArg(kernelHandle, i, stack.longs(val));
                case Short val -> CL10.clSetKernelArg(kernelHandle, i, stack.shorts(val));
                case String val -> CL10.clSetKernelArg(kernelHandle, i, stack.ASCII(val));
                case CLBuffer val -> CL10.clSetKernelArg(kernelHandle, i, stack.pointers(val.getHandle()));
                default -> throw new IllegalStateException("Unexpected value: " + arg);
            }
        }
    }
    
    private ComputeSize fixGlobalSize(ComputeSize global, ComputeSize local) {
        if (local == null) return global;
        
        int dim = global.workDim();
        
        int gx = roundUp(global.x(), local.x());
        int gy = dim > 1 ? roundUp(global.y(), local.y()) : 1;
        int gz = dim > 2 ? roundUp(global.z(), local.z()) : 1;
        
        return new ComputeSize(gx, gy, gz);
    }
    
    private static int roundUp(int global, int local) {
        return ((global + local - 1) / local) * local;
    }

    public long handle() {
        return handle;
    }

    @Override
    public String toString() {
        return "CLCommandQueue{" +
            "handle=" + handle +
            ", state=" + state +
            '}';
    }
}
