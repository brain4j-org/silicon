package org.silicon;

import org.silicon.computing.ComputeArgs;
import org.silicon.computing.ComputeQueue;
import org.silicon.computing.ComputeSize;
import org.silicon.device.ComputeBuffer;
import org.silicon.device.ComputeContext;
import org.silicon.device.ComputeDevice;
import org.silicon.kernel.ComputeFunction;
import org.silicon.kernel.ComputeModule;

import java.nio.file.Path;
import java.util.Arrays;

public class ComputeTest {

    public static void main(String[] _args) throws Throwable {
        int N = 512 * 512 * 512;
        
        System.out.println("Chosen backend " + Silicon.getBackend().getName());
        
        ComputeDevice device = Silicon.createSystemDevice();
        ComputeContext context = device.createContext();

        ComputeModule moduleFromPath = context.loadModule(Path.of("resources/vector_add.ptx"));
        ComputeFunction function = moduleFromPath.getFunction("vecAdd");

        float[] dataA = new float[N];
        float[] dataB = new float[N];
        
        for (int i = 0; i < N; i++) {
            dataA[i] = i;
            dataB[i] = i + 1;
        }

        ComputeBuffer a = device.allocateArray(dataA);
        ComputeBuffer b = device.allocateArray(dataB);
        ComputeBuffer c = device.allocateBytes(N * 4L);
        ComputeBuffer d = c.copy(); // creates a copy of this buffer on the GPU
        
        ComputeQueue queue = context.createQueue();
        ComputeArgs args = ComputeArgs.of(a, b, c, N);

        ComputeSize globalSize = new ComputeSize(N, 1, 1);
        ComputeSize groupSize = new ComputeSize(256, 1, 1);
        
        queue.dispatch(function, globalSize, groupSize, args);
        queue.awaitCompletion();
        
        float[] result = new float[10];
        float[] dataD = new float[10];
        
        c.get(result);
        d.get(dataD);
        
        System.out.println(Arrays.toString(result));
        System.out.println(Arrays.toString(dataD));
    }
}
