package org.silicon;

import org.silicon.computing.ComputeArgs;
import org.silicon.computing.ComputeQueue;
import org.silicon.computing.ComputeSize;
import org.silicon.device.ComputeBuffer;
import org.silicon.device.ComputeContext;
import org.silicon.device.ComputeDevice;
import org.silicon.kernel.ComputeFunction;
import org.silicon.kernel.ComputeModule;
import org.silicon.slang.SlangCompiler;

import java.nio.file.Path;
import java.util.Arrays;

public class ComputeTest {
    
    public static void main(String[] _args) {
        int N = 2048 * 2048;
        
        System.out.println("Chosen backend " + Silicon.getBackend().getName());
        
        ComputeDevice device = Silicon.createSystemDevice();
        ComputeContext context = device.createContext();
        
        SlangCompiler compiler = new SlangCompiler(context, Silicon.getBackend().getType());

        ComputeModule module = compiler.compile(Path.of("resources/vector_add.slang"));
        ComputeFunction function = module.getFunction("add");
        
        float[] dataA = new float[N];
        float[] dataB = new float[N];
        
        for (int i = 0; i < N; i++) {
            dataA[i] = i;
            dataB[i] = i + 1;
        }
        
        ComputeBuffer a = context.allocateArray(dataA);
        ComputeBuffer b = context.allocateArray(dataB);
        ComputeBuffer c = context.allocateBytes(N * 4L);
        
        ComputeArgs args = ComputeArgs.of(a, b, c, N);
        
        ComputeSize globalSize = new ComputeSize(N, 1, 1);
        ComputeSize groupSize = new ComputeSize(16, 1, 1);
        
        ComputeQueue queue = context.createQueue();
        
        queue.dispatch(function, globalSize, groupSize, args);
        queue.awaitCompletion();
        
        float[] result = new float[64];
        c.get(result);
        System.out.println(Arrays.toString(result));
    }
}
