package org.silicon.metal.computing;

import org.silicon.computing.ComputeArgs;
import org.silicon.computing.ComputeQueue;
import org.silicon.computing.ComputeSize;
import org.silicon.kernel.ComputeFunction;
import org.silicon.metal.MetalObject;
import org.silicon.metal.buffer.MetalCommandBuffer;
import org.silicon.metal.device.MetalDevice;
import org.silicon.metal.kernel.MetalFunction;
import org.silicon.metal.kernel.MetalPipeline;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.util.List;
import java.util.Objects;

public final class MetalCommandQueue implements MetalObject, ComputeQueue {

    public static final MethodHandle METAL_CREATE_COMMAND_BUFFER = LINKER.downcallHandle(
        LOOKUP.find("metal_create_command_buffer").orElse(null),
        FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS)
    );

    private final MemorySegment handle;
    private final MetalCommandBuffer commandBuffer;

    public MetalCommandQueue(MemorySegment handle) throws Throwable {
        this.handle = handle;
        this.commandBuffer = makeCommandBuffer();
    }

    @Override
    public void dispatch(ComputeFunction function, ComputeSize globalSize, ComputeSize groupSize, ComputeArgs args) throws Throwable {
        MetalFunction metalFunction = (MetalFunction) function;
        MetalPipeline pipeline = metalFunction.makePipeline();

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
                    default -> throw new IllegalStateException("Unexpected value: " + argList.get(i));
                }
            }

            encoder.dispatchThreads(globalSize.x(), globalSize.y(), globalSize.z(), groupSize.x(), groupSize.y(), groupSize.z());
        }

        commandBuffer.commit();
    }

    @Override
    public void awaitCompletion() throws Throwable {
        commandBuffer.waitUntilCompleted();
    }

    @Override
    public void release() throws Throwable {
        MetalObject.super.release();
    }

    public MetalCommandBuffer makeCommandBuffer() throws Throwable {
        MemorySegment ptr = (MemorySegment) METAL_CREATE_COMMAND_BUFFER.invokeExact(handle);
        return new MetalCommandBuffer(ptr);
    }

    @Override
    public MemorySegment handle() {
        return handle;
    }
}

