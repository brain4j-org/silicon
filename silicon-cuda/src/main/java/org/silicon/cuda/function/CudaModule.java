package org.silicon.cuda.function;

import org.silicon.api.SiliconException;
import org.silicon.cuda.CudaObject;
import org.silicon.cuda.device.CudaContext;
import org.silicon.api.function.ComputeModule;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;

public record CudaModule(MemorySegment handle, CudaContext context) implements CudaObject, ComputeModule {

    private static final MethodHandle CUDA_MODULE_GET_FUNCTION = CudaObject.find(
        "cuda_module_get_function",
        FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS)
    );

    @Override
    public CudaFunction getFunction(String name) {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment cName = arena.allocateFrom(name);
            MemorySegment funcHandle = (MemorySegment) CUDA_MODULE_GET_FUNCTION.invoke(handle, cName);

            if (funcHandle == null || funcHandle.address() == 0) {
                throw new SiliconException("Failed to get function: " + name);
            }

            return new CudaFunction(funcHandle);
        } catch (Throwable e) {
            throw new SiliconException("getFunction(String) failed", e);
        }
    }
}
