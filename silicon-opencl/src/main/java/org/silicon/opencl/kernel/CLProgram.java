package org.silicon.opencl.kernel;

import org.lwjgl.opencl.CL10;
import org.lwjgl.system.MemoryStack;
import org.silicon.api.SiliconException;
import org.silicon.api.function.ComputeFunction;
import org.silicon.api.function.ComputeModule;

import java.nio.IntBuffer;

public record CLProgram(long handle, long device) implements ComputeModule {
    
    @Override
    public ComputeFunction getFunction(String name) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer result = stack.mallocInt(1);
            
            long kernel = CL10.clCreateKernel(handle, name, result);
            if (result.get(0) != CL10.CL_SUCCESS) throw new SiliconException("clCreateKernel failed: " + result.get(0));
            
            return new CLKernel(kernel, device);
        }
    }
}
