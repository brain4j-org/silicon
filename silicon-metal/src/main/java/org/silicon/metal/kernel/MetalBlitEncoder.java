package org.silicon.metal.kernel;

import org.silicon.api.SiliconException;
import org.silicon.api.memory.MemoryState;
import org.silicon.metal.MetalObject;
import org.silicon.metal.device.MetalBuffer;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;

public final class MetalBlitEncoder implements MetalObject, AutoCloseable {

    public static final MethodHandle METAL_BLIT_COPY = MetalObject.find(
        "metal_blit_copy",
        FunctionDescriptor.ofVoid(
            ValueLayout.ADDRESS, // encoder
            ValueLayout.ADDRESS, // src buffer
            ValueLayout.ADDRESS, // dst buffer
            ValueLayout.JAVA_LONG // size in bytes
        )
    );

    private final MemorySegment handle;
    private MemoryState state;

    public MetalBlitEncoder(MemorySegment handle) {
        this.handle = handle;
        this.state = MemoryState.ALIVE;
    }

    public void copy(MetalBuffer src, MetalBuffer dst, long size) {
        try {
            METAL_BLIT_COPY.invokeExact(handle, src.handle(), dst.handle(), size);
        } catch (Throwable e) {
            throw new SiliconException("blit copy failed", e);
        }
    }

    @Override
    public void close() {
        try {
            MetalEncoder.METAL_END_ENCODING.invokeExact(handle);
        } catch (Throwable e) {
            throw new SiliconException("endEncoding() failed", e);
        } finally {
            state = MemoryState.FREE;
        }
    }

    @Override
    public MemorySegment handle() {
        return handle;
    }

    public MemoryState state() {
        return state;
    }
}
