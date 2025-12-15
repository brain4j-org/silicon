package org.silicon.metal.computing;

import org.silicon.computing.ComputeArgs;
import org.silicon.computing.ComputeQueue;
import org.silicon.computing.ComputeSize;
import org.silicon.kernel.ComputeFunction;
import org.silicon.metal.MetalObject;
import org.silicon.metal.buffer.MetalCommandBuffer;
import org.silicon.metal.device.MetalDevice;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;

public record MetalCommandQueue(MemorySegment handle) implements MetalObject, ComputeQueue {

    public MetalCommandBuffer makeCommandBuffer() throws Throwable {
        return MetalCommandBuffer.make(this);
    }

    @Override
    public void dispatch(ComputeFunction function, ComputeSize globalSize, ComputeSize groupSize, ComputeArgs args) throws Throwable {

    }

    @Override
    public void awaitCompletion() throws Throwable {

    }

    @Override
    public void release() throws Throwable {
        MetalObject.super.release();
    }

    @Override
    public boolean isCompleted() throws Throwable {
        return false;
    }
}

