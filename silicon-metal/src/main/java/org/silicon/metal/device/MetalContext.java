package org.silicon.metal.device;

import org.silicon.api.SiliconException;
import org.silicon.api.backend.BackendType;
import org.silicon.api.device.ComputeArena;
import org.silicon.api.device.ComputeBuffer;
import org.silicon.api.device.ComputeContext;
import org.silicon.metal.MetalObject;
import org.silicon.metal.kernel.MetalCommandQueue;
import org.silicon.metal.function.MetalLibrary;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.nio.file.Files;
import java.nio.file.Path;

public record MetalContext(MetalDevice device) implements MetalObject, ComputeContext {

    public static final MethodHandle METAL_NEW_BUFFER = MetalObject.find(
        "metal_new_buffer",
        FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG)
    );
    public static final MethodHandle METAL_CREATE_COMMAND_QUEUE = MetalObject.find(
        "metal_create_command_queue",
        FunctionDescriptor.of(
            ValueLayout.ADDRESS, // MTLCommandQueue*
            ValueLayout.ADDRESS  // device (MTLDevice*)
        )
    );
    public static final MethodHandle METAL_CREATE_LIBRARY = MetalObject.find(
        "metal_create_library",
        FunctionDescriptor.of(ValueLayout.ADDRESS,  // return MTLLibrary*
            ValueLayout.ADDRESS,                    // device (MTLDevice*)
            ValueLayout.ADDRESS)                    // kernel source (char*)
    );
    
    @Override
    public BackendType getBackendType() {
        return BackendType.METAL;
    }
    
    @Override
    public MetalCommandQueue createQueue() {
        return createQueue(null);
    }

    @Override
    public MetalCommandQueue createQueue(ComputeArena arena) {
        try {
            MemorySegment queuePtr = (MemorySegment) METAL_CREATE_COMMAND_QUEUE.invokeExact(device.handle());

            if (queuePtr == null) {
                throw new SiliconException("Failed to create Metal command queue");
            }

            return new MetalCommandQueue(queuePtr, arena);
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
                throw new SiliconException("Failed to compile Metal library");
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
                throw new SiliconException("metalMakeBuffer failed");
            }
            
            return new MetalBuffer(ptr, this, size);
        } catch (Throwable e) {
            throw new SiliconException("allocateBytes(long) failed", e);
        }
    }

    @Override
    public ComputeBuffer allocateArray(byte[] data) {
        long size = data.length;
        MemorySegment srcSeg = MemorySegment.ofArray(data);
        return allocateFromArray(srcSeg, size);
    }

    @Override
    public ComputeBuffer allocateArray(double[] data) {
        long size = data.length * ValueLayout.JAVA_DOUBLE.byteSize();
        MemorySegment srcSeg = MemorySegment.ofArray(data);
        return allocateFromArray(srcSeg, size);
    }

    @Override
    public ComputeBuffer allocateArray(float[] data) {
        long size = data.length * ValueLayout.JAVA_FLOAT.byteSize();
        MemorySegment srcSeg = MemorySegment.ofArray(data);
        return allocateFromArray(srcSeg, size);
    }

    @Override
    public ComputeBuffer allocateArray(long[] data) {
        long size = data.length * ValueLayout.JAVA_LONG.byteSize();
        MemorySegment srcSeg = MemorySegment.ofArray(data);
        return allocateFromArray(srcSeg, size);
    }

    @Override
    public ComputeBuffer allocateArray(int[] data) {
        long size = data.length * ValueLayout.JAVA_INT.byteSize();
        MemorySegment srcSeg = MemorySegment.ofArray(data);
        return allocateFromArray(srcSeg, size);
    }

    @Override
    public ComputeBuffer allocateArray(short[] data) {
        long size = data.length * ValueLayout.JAVA_SHORT.byteSize();
        MemorySegment srcSeg = MemorySegment.ofArray(data);
        return allocateFromArray(srcSeg, size);
    }

    @Override
    public MemorySegment handle() {
        return device.handle();
    }

    private MetalBuffer allocateFromArray(MemorySegment src, long size) {
        MetalBuffer buffer = allocateBytes(size);

        MemorySegment dstSeg = buffer.getContents().reinterpret(size);
        MemorySegment.copy(src, 0, dstSeg, 0, size);

        return buffer;
    }
}
