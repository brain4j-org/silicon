package org.silicon.metal;

import org.silicon.Silicon;
import org.silicon.computing.ComputeArgs;
import org.silicon.computing.ComputeQueue;
import org.silicon.computing.ComputeSize;
import org.silicon.device.ComputeBuffer;
import org.silicon.device.ComputeContext;
import org.silicon.device.ComputeDevice;
import org.silicon.kernel.ComputeFunction;
import org.silicon.kernel.ComputeModule;
import org.silicon.metal.device.MetalBuffer;
import org.silicon.metal.buffer.MetalCommandBuffer;
import org.silicon.metal.device.MetalContext;
import org.silicon.metal.kernel.MetalFunction;
import org.silicon.metal.kernel.MetalLibrary;
import org.silicon.metal.kernel.MetalPipeline;
import org.silicon.metal.computing.MetalCommandQueue;
import org.silicon.metal.device.MetalDevice;
import org.silicon.metal.computing.MetalEncoder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public class MetalTest {

    public static final int N = 64;
    public static final int E = 16;

    public static void main(String[] args) {
       benchmarkMetal();
    }

    private static void benchmarkMetal() {
        System.out.println("========= Benchmark for: Metal =========");

        byte[] raw = Files.readAllBytes(Path.of("resources/vector_add.metal"));
        String src = new String(raw);

        System.out.println("Using backned " + Silicon.getBackend());

        ComputeDevice device = Silicon.createSystemDevice();
        ComputeContext context = device.createContext();
        ComputeModule module = context.loadModule(src);
        ComputeFunction function = module.getFunction("add");

        float[] a = new float[N];
        float[] b = new float[N];
        System.out.println("created buffers");

        for (int i = 0; i < N; i++) {
            a[i] = i;
            b[i] = i + 1;
        }

        ComputeBuffer bufA = context.allocateArray(a);
        ComputeBuffer bufB = context.allocateArray(b);
        ComputeBuffer bufC = context.allocateBytes(N * Float.BYTES);

        ComputeQueue queue = context.createQueue();
        ComputeArgs args = ComputeArgs.of(bufA, bufB, bufC);

        ComputeSize globalSize = new ComputeSize(N, 1, 1);
        ComputeSize groupSize = new ComputeSize(256, 1, 1);

        queue.dispatch(function, globalSize, groupSize, args);
        queue.awaitCompletion();

        float[] c = new float[N];
        bufC.get(c);

        System.out.println(Arrays.toString(c));

        // MetalCommandBuffer commandBuffer = queue.makeCommandBuffer();
//        MetalPipeline pipeline = function.makePipeline();
//
//        try (MetalEncoder encoder = commandBuffer.makeEncoder(pipeline)) {
//            encoder.setBuffer(bufA, 0);
//            encoder.setBuffer(bufB, 1);
//            encoder.setBuffer(bufC, 2);
//
//            encoder.dispatchThreads(N, 1, 1, 512, 1, 1);
//        }
//
//        long start = System.nanoTime();
//        commandBuffer.commit();
//        commandBuffer.waitUntilCompleted();
//        long end = System.nanoTime();
//
//        double took = (end - start) / 1e6;
//        System.out.println("Took " + took + " millis");
//
//        float[] C = new float[E];
//        bufC.get(C);
//
//        System.out.println("Device: " + device.getName());
//        System.out.println("Computed on GPU: " + Arrays.toString(C));
    }
}
