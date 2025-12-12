package org.compute4j;

import java.nio.file.Path;

public class ComputeTest {

    public static void main(String[] _args) {
        ComputeDevice device = Compute4J.createSystemDevice();
        ComputeContext context = device.createContext();

        ComputeModule moduleFromPath = context.loadModule(Path.of("vector_add.metal"));
        ComputeFunction function = moduleFromPath.getFunction("add");

        float[] dataA = new float[1024];
        float[] dataB = new float[1024];

        ComputeBuffer a = device.allocateArray(dataA, dataA.length * 4L);
        ComputeBuffer b = device.allocateArray(dataB, dataB.length * 4L);
        ComputeBuffer c = device.allocateBytes(dataA.length * 4L);

        ComputeQueue queue = context.createQueue();
        ComputeArgs args = ComputeArgs.of(a, b, c);

        ComputeSize grid = new ComputeSize(1024, 1, 1);
        ComputeSize block = new ComputeSize(128, 1, 1);

        queue.dispatch(function, grid, block, args);
        queue.awaitCompletion();
    }
}
