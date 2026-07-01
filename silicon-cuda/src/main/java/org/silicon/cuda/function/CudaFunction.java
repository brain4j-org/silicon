package org.silicon.cuda.function;

import org.silicon.api.SiliconException;
import org.silicon.api.function.ComputeFunction;
import org.silicon.cuda.CudaObject;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

import static org.silicon.cuda.Bindings.*;

public record CudaFunction(MemorySegment handle) implements CudaObject, ComputeFunction {

    private static final int CU_FUNC_ATTRIBUTE_MAX_THREADS_PER_BLOCK = 0;

    @Override
    public int maxWorkGroupSize() {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment pi = arena.allocate(ValueLayout.JAVA_INT);
            int res = (int) CU_FUNC_GET_ATTRIBUTE.invokeExact(pi, CU_FUNC_ATTRIBUTE_MAX_THREADS_PER_BLOCK, handle);

            if (res != 0) {
                throw new SiliconException("cuFuncGetAttribute failed: " + res);
            }

            return pi.get(ValueLayout.JAVA_INT, 0);
        } catch (Throwable e) {
            throw new SiliconException("maxWorkGroupSize() failed", e);
        }
    }
}
