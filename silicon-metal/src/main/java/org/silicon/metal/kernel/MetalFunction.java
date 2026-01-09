package org.silicon.metal.kernel;

import org.silicon.SiliconException;
import org.silicon.kernel.ComputeFunction;
import org.silicon.metal.MetalObject;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.util.Objects;

public final class MetalFunction implements MetalObject, ComputeFunction {

    public static final MethodHandle METAL_CREATE_FUNCTION = LINKER.downcallHandle(
        LOOKUP.find("metal_create_function").orElse(null),
        FunctionDescriptor.of(ValueLayout.ADDRESS, // return MTLFunction*
            ValueLayout.ADDRESS,                  // library (MTLLibrary*)
            ValueLayout.ADDRESS)                  // function name (char*)
    );
    private final MemorySegment handle;
    private final MetalPipeline pipeline;

    public MetalFunction(MemorySegment handle) {
        this.handle = handle;
        this.pipeline = makePipeline();
    }

    public static MetalFunction create(MetalLibrary library, String name) {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment fnName = arena.allocateFrom(name);
            MemorySegment fnPtr = (MemorySegment) METAL_CREATE_FUNCTION.invokeExact(library.handle(), fnName);

            if (fnPtr == null) {
                throw new RuntimeException("Function '" + name + "' not found in library");
            }

            return new MetalFunction(fnPtr);
        } catch (Throwable e) {
            throw new SiliconException("create(MetalLibrary, String) failed", e);
        }
    }

    public MetalPipeline makePipeline() {
        return MetalPipeline.makePipeline(this);
    }

    @Override
    public MemorySegment handle() {
        return handle;
    }

    public MetalPipeline getPipeline() {
        return pipeline;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (MetalFunction) obj;
        return Objects.equals(this.handle, that.handle);
    }

    @Override
    public int hashCode() {
        return Objects.hash(handle);
    }

    @Override
    public String toString() {
        return "MetalFunction[" +
            "handle=" + handle + ']';
    }

}
