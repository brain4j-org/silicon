package org.silicon.metal.computing;

import org.silicon.SiliconException;
import org.silicon.computing.ComputeArgs;
import org.silicon.computing.ComputeQueue;
import org.silicon.computing.ComputeSize;
import org.silicon.kernel.ComputeFunction;
import org.silicon.metal.MetalObject;
import org.silicon.metal.buffer.MetalCommandBuffer;
import org.silicon.metal.device.MetalBuffer;
import org.silicon.metal.kernel.MetalFunction;
import org.silicon.metal.kernel.MetalPipeline;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.util.List;

public final class MetalCommandQueue implements MetalObject, ComputeQueue {

    public static final MethodHandle METAL_CREATE_COMMAND_BUFFER = MetalObject.find(
        "metal_create_command_buffer",
        FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS)
    );

    private final MemorySegment handle;
    private MetalCommandBuffer lastCommandBuf;

    public MetalCommandQueue(MemorySegment handle) {
        this.handle = handle;
        this.lastCommandBuf = makeCommandBuffer();
    }

    @Override
    public void dispatch(ComputeFunction function, ComputeSize globalSize, ComputeSize groupSize, ComputeArgs args) {
        MetalFunction metalFunction = (MetalFunction) function;
        MetalPipeline pipeline = metalFunction.pipeline();

        MetalCommandBuffer commandBuffer = makeCommandBuffer();

        if (globalSize == null) throw new IllegalArgumentException("Global size cannot be null!");
        if (groupSize == null) throw new IllegalArgumentException("Group size cannot be null!");

        try (MetalEncoder encoder = commandBuffer.makeEncoder(pipeline)) {
            List<Object> argList = args.getArgs();

            for (int i = 0; i < args.size(); i++) {
                switch (argList.get(i)) {
                    case Double x -> encoder.setDouble(x, i);
                    case Float x -> encoder.setFloat(x, i);
                    case Long x -> encoder.setLong(x, i);
                    case Integer x -> encoder.setInt(x, i);
                    case Short x -> encoder.setShort(x, i);
                    case MetalBuffer x -> encoder.setBuffer(x, i);
                    default -> throw new IllegalStateException("Unexpected value: " + argList.get(i));
                }
            }

            encoder.dispatchThreads(globalSize.x(), globalSize.y(), globalSize.z(), groupSize.x(), groupSize.y(), groupSize.z());
        }

        commandBuffer.commit();
        lastCommandBuf = commandBuffer;
    }

    @Override
    public void awaitCompletion() {
        if (lastCommandBuf == null) return;

        lastCommandBuf.waitUntilCompleted();
        lastCommandBuf = null; // recreate it later
    }

    public MetalCommandBuffer makeCommandBuffer() {
        try {
            MemorySegment ptr = (MemorySegment) METAL_CREATE_COMMAND_BUFFER.invokeExact(handle);
            
            if (ptr == null || ptr.address() == 0) {
                throw new RuntimeException("metalMakeCommandBuffer failed!");
            }
            
            return new MetalCommandBuffer(ptr);
        } catch (Throwable e) {
            throw new SiliconException("makeCommandBuffer() failed", e);
        }
    }

    @Override
    public MemorySegment handle() {
        return handle;
    }
}

