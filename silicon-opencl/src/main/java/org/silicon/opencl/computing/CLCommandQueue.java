package org.silicon.opencl.computing;

import org.lwjgl.PointerBuffer;
import org.lwjgl.opencl.CL10;
import org.lwjgl.system.MemoryStack;
import org.silicon.computing.ComputeArgs;
import org.silicon.computing.ComputeQueue;
import org.silicon.computing.ComputeSize;
import org.silicon.kernel.ComputeFunction;
import org.silicon.opencl.device.CLBuffer;
import org.silicon.opencl.kernel.CLKernel;

import java.util.List;

public record CLCommandQueue(long handle) implements ComputeQueue {

    private PointerBuffer toBuffer(ComputeSize size, MemoryStack stack) {
        int dim = size.workDim();
        PointerBuffer buf = stack.mallocPointer(dim);
        buf.put(0, size.x());
        if (dim > 1) buf.put(1, size.y());
        if (dim > 2) buf.put(2, size.z());
        return buf;
    }

    @Override
    public void dispatch(ComputeFunction function, ComputeSize globalSize, ComputeSize groupSize, ComputeArgs args) {
        if (!(function instanceof CLKernel(long kernelHandle))) {
            throw new IllegalArgumentException("Compute function is not an OpenCL kernel!");
        }

        try (MemoryStack stack = MemoryStack.stackPush()) {
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

            ComputeSize fixedLocal = fixLocalSize(globalSize, groupSize);
            ComputeSize fixedGlobal = fixGlobalSize(globalSize, fixedLocal);

            PointerBuffer globalBuf = toBuffer(fixedGlobal, stack);
            PointerBuffer localBuf = fixedLocal != null ? toBuffer(fixedLocal, stack) : null;

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
    public void awaitCompletion() {
        CL10.clFinish(handle);
    }

    @Override
    public void release() {
        CL10.clReleaseCommandQueue(handle);
    }

    private ComputeSize fixGlobalSize(ComputeSize global, ComputeSize local) {
        if (local == null) return global;

        int x = global.x();
        int lx = local.x();

        if (lx > x) lx = x;

        int fixedX = ((x + lx - 1) / lx) * lx;

        return new ComputeSize(fixedX, 1, 1);
    }

    private ComputeSize fixLocalSize(ComputeSize global, ComputeSize local) {
        if (local == null) return null;

        int lx = local.x();
        if (lx > global.x()) lx = global.x();

        return new ComputeSize(lx, 1, 1);
    }
}
