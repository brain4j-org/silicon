package org.compute4j;

import org.compute4j.computing.ComputeQueue;
import org.compute4j.computing.ComputeSize;
import org.compute4j.device.ComputeBuffer;
import org.compute4j.device.ComputeContext;
import org.compute4j.device.ComputeDevice;
import org.compute4j.computing.ComputeArgs;
import org.compute4j.kernel.ComputeFunction;
import org.compute4j.kernel.ComputeModule;

import java.nio.file.Path;
import java.util.Arrays;

public class ComputeTest {

    public static void main(String[] _args) throws Throwable {
        int N = 512 * 512 * 512;
        
        System.out.println("Chosen backend " + Compute4J.getBackend().getName());
        
        ComputeDevice device = Compute4J.createSystemDevice();
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
        
        ComputeQueue queue = context.createQueue();
        ComputeArgs args = ComputeArgs.of(a, b, c, N);

        ComputeSize globalSize = new ComputeSize(N, 1, 1);
        ComputeSize groupSize = new ComputeSize(256, 1, 1);
        
        queue.dispatch(function, globalSize, groupSize, args);
        queue.awaitCompletion();
        
        float[] result = new float[10];
        c.get(result);
        
        System.out.println(Arrays.toString(result));
    }
}
