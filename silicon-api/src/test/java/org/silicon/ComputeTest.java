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
         Silicon.chooseBackend(BackendType.METAL);
        
        System.out.println("Chosen backend " + Silicon.getBackend().getName());
        
        ComputeDevice device = Silicon.createSystemDevice();
        ComputeContext context = device.createContext();

        // ComputeModule moduleFromPath = context.loadModule(Path.of("resources/vector_add.ptx"));
        // ComputeModule moduleFromPath = context.loadModule(Path.of("resources/vector_add.cl"));
        ComputeModule moduleFromPath = context.loadModule(Path.of("resources/vector_add.metal"));
        ComputeFunction function = moduleFromPath.getFunction("add");

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
        ComputeSize groupSize = new ComputeSize(256, 1, 1);

        double sum = 0;

        for (int i = 0; i < 100; i++) {
            ComputeQueue queue = context.createQueue();

            long start = System.nanoTime();

            queue.dispatch(function, globalSize, groupSize, args);
            queue.awaitCompletion();

            long end = System.nanoTime();
            double took = (end - start) / 1e6;
            if (i > 5) sum += took;
        }

        double average = sum / 95;
        System.out.printf("Average ms: %.2f ms%n", average);

        float[] result = new float[10];
        c.get(result);
        System.out.println(Arrays.toString(result));
    }
}
