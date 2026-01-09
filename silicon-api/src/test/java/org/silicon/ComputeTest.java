package org.silicon;

import org.silicon.backend.BackendType;
import org.silicon.computing.ComputeArgs;
import org.silicon.computing.ComputeQueue;
import org.silicon.computing.ComputeSize;
import org.silicon.device.ComputeArena;
import org.silicon.device.ComputeBuffer;
import org.silicon.device.ComputeContext;
import org.silicon.device.ComputeDevice;
import org.silicon.kernel.ComputeFunction;
import org.silicon.kernel.ComputeModule;

import java.nio.file.Path;
import java.util.Arrays;

public class ComputeTest {

    public static void main(String[] _args) {
        int N = 512 * 512 * 512;
        Silicon.chooseBackend(BackendType.CUDA);

        System.out.println("Running backend " + Silicon.getBackend().getName());

        ComputeDevice device = Silicon.createSystemDevice();
        ComputeContext context = device.createContext();

        ComputeModule moduleFromPath = context.loadModule(Path.of("resources/vector_add" + kernelSuffix()));
        ComputeFunction function = moduleFromPath.getFunction("add");

        float[] dataA = new float[N];
        float[] dataB = new float[N];

        for (int i = 0; i < N; i++) {
            dataA[i] = i;
            dataB[i] = i + 1;
        }

        try (ComputeArena arena = context.createArena()) {
            ComputeBuffer a = arena.allocateArray(dataA);
            ComputeBuffer b = arena.allocateArray(dataB);
            ComputeBuffer c = arena.allocateBytes(N * 4L);

            ComputeArgs args = ComputeArgs.of(a, b, c, N);

            ComputeSize globalSize = new ComputeSize(N, 1, 1);
            ComputeSize groupSize = new ComputeSize(256, 1, 1);

            int tests = 100;

            long start = System.nanoTime();
            ComputeQueue queue = context.createQueue();

            for (int i = 0; i < tests; i++) {
                queue.dispatch(function, globalSize, groupSize, args);
            }

            queue.awaitCompletion();

            long end = System.nanoTime();
            double took = (end - start) / 1e6;
            System.out.printf("Took %.2f ms %n", took);

            float[] result = new float[10];
            c.get(result);
            System.out.println(Arrays.toString(result));
        }
    }

    private static String kernelSuffix() {
        return switch (Silicon.getBackend().getType()) {
            case CUDA -> ".ptx";
            case OPENCL -> ".cl";
            case METAL -> ".metal";
        };
    }
}
