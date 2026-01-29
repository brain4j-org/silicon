package org.silicon;

import org.silicon.computing.ComputeArgs;
import org.silicon.computing.ComputeQueue;
import org.silicon.computing.ComputeSize;
import org.silicon.device.*;
import org.silicon.kernel.ComputeFunction;
import org.silicon.kernel.ComputeModule;
import org.silicon.slang.SlangCompiler;

import java.util.Arrays;

public class ComputeTest {
    
    public static void main(String[] args) {
        int N = 2048 * 2048;

        System.out.println("Chosen backend " + Silicon.backend().name());
        
        ComputeDevice device = Silicon.createDevice();
        ComputeContext context = device.createContext();

        System.out.println("Device name: " + device.name());
        System.out.println("Vendor: " + device.vendor());
        System.out.println("Device memory: " + device.memorySize());
        System.out.println("Supports FP16? " + device.supports(DeviceFeature.FP16));
        System.out.println("Supports FP64? " + device.supports(DeviceFeature.FP64));

        SlangCompiler compiler = new SlangCompiler(context);

        ComputeModule module = compiler.compileFromResource("vector_add.slang");
        ComputeFunction function = module.getFunction("add");

        float[] dataA = generateData(N);
        float[] dataB = generateData(N);

        try (ComputeArena arena = context.createArena()) {
            ComputeBuffer a = arena.allocateArray(dataA);
            ComputeBuffer b = arena.allocateArray(dataB);
            ComputeBuffer c = arena.allocateBytes(N * 4L);

            ComputeArgs kernelArgs = ComputeArgs.of(a, b, c, N);

            ComputeSize globalSize = new ComputeSize(N, 1, 1);
            ComputeSize groupSize = new ComputeSize(16, 1, 1);

            ComputeQueue queue = arena.createQueue();

            queue.dispatch(function, globalSize, groupSize, kernelArgs);
            queue.awaitCompletion();

            float[] result = new float[64];
            c.get(result);
            System.out.println(Arrays.toString(result));
        }
    }
    
    private static float[] generateData(int length) {
        float[] data = new float[length];
        for (int i = 0; i < length; i++) data[i] = i;
        return data;
    }
}
