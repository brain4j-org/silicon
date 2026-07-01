package org.silicon.cuda.function;

import org.silicon.api.SiliconException;
import org.silicon.api.function.ComputeModule;
import org.silicon.cuda.Bindings;
import org.silicon.cuda.CudaObject;
import org.silicon.cuda.device.CudaContext;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.nio.charset.StandardCharsets;

import static org.silicon.cuda.Bindings.*;

public record CudaModule(MemorySegment handle, CudaContext context) implements CudaObject, ComputeModule {

    @Override
    public CudaFunction getFunction(String name) {
        try (Arena arena = Arena.ofConfined()) {
            byte[] nameBytes = (name + "\0").getBytes(StandardCharsets.UTF_8);
            MemorySegment cName = arena.allocate(nameBytes.length);
            cName.copyFrom(MemorySegment.ofArray(nameBytes));

            MemorySegment funcPtr = arena.allocate(Bindings.CU_FUNCTION);
            int res = (int) CU_MODULE_GET_FUNCTION.invokeExact(funcPtr, handle, cName);

            if (res != 0) {
                throw new SiliconException("cuModuleGetFunction failed: " + res);
            }

            MemorySegment funcHandle = funcPtr.get(Bindings.CU_FUNCTION, 0);
            return new CudaFunction(funcHandle);
        } catch (Throwable e) {
            throw new SiliconException("getFunction(String) failed", e);
        }
    }
}
