package org.silicon.metal.kernel;

import org.silicon.api.SiliconException;
import org.silicon.api.memory.Freeable;
import org.silicon.api.memory.MemoryState;
import org.silicon.metal.MetalObject;
import org.silicon.metal.function.MetalPipeline;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;

public final class MetalCommandBuffer implements MetalObject, Freeable {

    public static final MethodHandle METAL_MAKE_ENCODER = MetalObject.find(
        "metal_make_encoder",
        FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS)
    );
    public static final MethodHandle METAL_COMMIT = MetalObject.find(
        "metal_commit",
        FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
    );
    public static final MethodHandle METAL_WAIT_UNTIL_COMPLETED = MetalObject.find(
        "metal_wait_until_completed",
        FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
    );
    private final MemorySegment handle;
    private MemoryState state;

    public MetalCommandBuffer(MemorySegment handle) {
        this.handle = handle;
        this.state = MemoryState.ALIVE;
    }

    public MetalEncoder makeEncoder(MetalPipeline pipeline) {
        try {
            MemorySegment ptr = (MemorySegment) METAL_MAKE_ENCODER.invokeExact(handle, pipeline.handle());

            if (ptr == null || ptr.address() == 0) {
                throw new SiliconException("makeComputeCommandEncoder failed");
            }

            return new MetalEncoder(ptr);
        } catch (Throwable e) {
            throw new SiliconException("makeEncoder(MetalPipeline) failed", e);
        }
    }

    public void commit() {
        try {
            METAL_COMMIT.invokeExact(handle);
        } catch (Throwable e) {
            throw new SiliconException("commit() failed", e);
        }
    }

    public void waitUntilCompleted() {
        try {
            METAL_WAIT_UNTIL_COMPLETED.invokeExact(handle);
        } catch (Throwable e) {
            throw new SiliconException("waitUntilCompleted() failed", e);
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
            METAL_RELEASE_OBJECT.invokeExact(handle);
            state = MemoryState.FREE;
        } catch (Throwable t) {
            throw new SiliconException("free() failed", t);
        }
    }

    @Override
    public MemorySegment handle() {
        return handle;
    }

    @Override
    public String toString() {
        return "MetalCommandBuffer{" +
            "handle=" + handle +
            ", state=" + state +
            '}';
    }
}
