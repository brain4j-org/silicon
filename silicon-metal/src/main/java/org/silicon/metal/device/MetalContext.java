package org.silicon.metal.device;

import org.silicon.SiliconException;
import org.silicon.computing.ComputeQueue;
import org.silicon.device.ComputeBuffer;
import org.silicon.device.ComputeContext;
import org.silicon.metal.MetalObject;
import org.silicon.metal.computing.MetalCommandQueue;
import org.silicon.metal.kernel.MetalLibrary;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.nio.file.Files;
import java.nio.file.Path;

public record MetalContext(MetalDevice device) implements MetalObject, ComputeContext {

    public static final MethodHandle METAL_NEW_BUFFER = LINKER.downcallHandle(
        LOOKUP.find("metal_new_buffer").orElse(null),
        FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG)
    );
    public static final MethodHandle METAL_CREATE_COMMAND_QUEUE = LINKER.downcallHandle(
        LOOKUP.find("metal_create_command_queue").orElse(null),
        FunctionDescriptor.of(
            ValueLayout.ADDRESS, // MTLCommandQueue*
            ValueLayout.ADDRESS  // device (MTLDevice*)
        )
    );
    public static final MethodHandle METAL_CREATE_LIBRARY = LINKER.downcallHandle(
        LOOKUP.find("metal_create_library").orElse(null),
        FunctionDescriptor.of(ValueLayout.ADDRESS,  // return MTLLibrary*
            ValueLayout.ADDRESS,                    // device (MTLDevice*)
            ValueLayout.ADDRESS)                    // kernel source (char*)
    );

    @Override
    public MetalCommandQueue createQueue() {
        try {
            MemorySegment queuePtr = (MemorySegment) METAL_CREATE_COMMAND_QUEUE.invokeExact(device.handle());

            if (queuePtr == null) {
                throw new RuntimeException("Failed to create Metal command queue");
            }

            return new MetalCommandQueue(queuePtr);
        } catch (Throwable e) {
            throw new SiliconException("createQueue() failed", e);
        }
    }

    @Override
    public MetalLibrary loadModule(Path path) {
        try {
            return loadModule(Files.readAllBytes(path));
        } catch (Throwable e) {
            throw new SiliconException("loadModule(Path) failed", e);
        }
    }

    @Override
    public MetalLibrary loadModule(byte[] rawSrc) {
        return loadModule(new String(rawSrc));
    }

    @Override
    public MetalLibrary loadModule(String source) {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment src = arena.allocateFrom(source);
            MemorySegment libPtr = (MemorySegment) METAL_CREATE_LIBRARY.invokeExact(device.handle(), src);

            if (libPtr == null) {
                throw new RuntimeException("Failed to compile Metal library");
            }

            return new MetalLibrary(libPtr);
        } catch (Throwable e) {
            throw new SiliconException("loadModule(String) failed", e);
        }
    }

    @Override
    public MetalBuffer allocateBytes(long size) {
        try {
            MemorySegment ptr = (MemorySegment) METAL_NEW_BUFFER.invokeExact(device.handle(), size);
            
            if (ptr == null || ptr.address() == 0) {
                throw new RuntimeException("metalMakeBuffer failed!");
            }
            
            return new MetalBuffer(ptr, this, size);
        } catch (Throwable e) {
            throw new SiliconException("allocateBytes(long) failed", e);
        }
    }

    @Override
    public ComputeBuffer allocateArray(byte[] data, long size) {
        MetalBuffer buffer = allocateBytes(size);
        buffer.asByteBuffer().put(data);
        return buffer;
    }

    @Override
    public ComputeBuffer allocateArray(byte[] data, long size, ComputeQueue queue) {
        return allocateArray(data, size);
    }

    @Override
    public ComputeBuffer allocateArray(double[] data, long size) {
        MetalBuffer buffer = allocateBytes(size);
        buffer.asByteBuffer().asDoubleBuffer().put(data);
        return buffer;
    }

    @Override
    public ComputeBuffer allocateArray(double[] data, long size, ComputeQueue queue) {
        return allocateArray(data, size);
    }

    @Override
    public ComputeBuffer allocateArray(float[] data, long size) {
        MetalBuffer buffer = allocateBytes(size);
        buffer.asByteBuffer().asFloatBuffer().put(data);
        return buffer;
    }

    @Override
    public ComputeBuffer allocateArray(float[] data, long size, ComputeQueue queue) {
        return allocateArray(data, size);
    }

    @Override
    public ComputeBuffer allocateArray(long[] data, long size) {
        MetalBuffer buffer = allocateBytes(size);
        buffer.asByteBuffer().asLongBuffer().put(data);
        return buffer;
    }

    @Override
    public ComputeBuffer allocateArray(long[] data, long size, ComputeQueue queue) {
        return allocateArray(data, size);
    }

    @Override
    public ComputeBuffer allocateArray(int[] data, long size) {
        MetalBuffer buffer = allocateBytes(size);
        buffer.asByteBuffer().asIntBuffer().put(data);
        return buffer;
    }

    @Override
    public ComputeBuffer allocateArray(int[] data, long size, ComputeQueue queue) {
        return allocateArray(data, size);
    }

    @Override
    public ComputeBuffer allocateArray(short[] data, long size) {
        MetalBuffer buffer = allocateBytes(size);
        buffer.asByteBuffer().asShortBuffer().put(data);
        return buffer;
    }

    @Override
    public ComputeBuffer allocateArray(short[] data, long size, ComputeQueue queue) {
        return allocateArray(data, size);
    }

    @Override
    public MemorySegment handle() {
        return device.handle();
    }
}
