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

public class ComputeTest {

    public static void main(String[] _args) throws Throwable {
        Compute4J.chooseBackend(BackendType.OPENCL); // force the backend
        
        ComputeDevice device = Compute4J.createSystemDevice();
        ComputeContext context = device.createContext();

        ComputeModule moduleFromPath = context.loadModule(Path.of("vector_add.metal"));
        ComputeFunction function = moduleFromPath.getFunction("add");

        float[] dataA = new float[1024];
        float[] dataB = new float[1024];

        ComputeBuffer a = device.allocateArray(dataA);
        ComputeBuffer b = device.allocateArray(dataB);
        ComputeBuffer c = device.allocateBytes(dataA.length * 4L);

        ComputeQueue queue = context.createQueue();
        ComputeArgs args = ComputeArgs.of(a, b, c);

        ComputeSize globalSize = new ComputeSize(1024, 1, 1);
        ComputeSize groupSize = new ComputeSize(128, 1, 1);

        queue.dispatch(function, globalSize, groupSize, args);
        queue.awaitCompletion();
        
        float[] result = new float[1024];
        c.get(result);
        
        System.out.println(result);
    }
}
