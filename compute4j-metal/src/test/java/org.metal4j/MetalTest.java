package org.metal4j;

import org.metal4j.buffer.MetalBuffer;
import org.metal4j.buffer.MetalCommandBuffer;
import org.metal4j.kernel.MetalFunction;
import org.metal4j.kernel.MetalLibrary;
import org.metal4j.kernel.MetalPipeline;
import org.metal4j.state.MetalCommandQueue;
import org.metal4j.state.MetalDevice;
import org.metal4j.state.MetalEncoder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public class MetalTest {

    public static final int N = 512 * 512 * 512;
    public static final int E = 16;

    public static void main(String[] args) throws Throwable {
       benchmarkMetal();
    }

    private static void benchmarkMetal() throws Throwable {
        System.out.println("========= Benchmark for: Metal =========");

        byte[] raw = Files.readAllBytes(Path.of("resources/vector_add.metal"));
        String src = new String(raw);

        MetalDevice device = Metal.createSystemDevice();

        MetalLibrary lib = device.makeLibrary(src);
        MetalFunction function = lib.makeFunction("add");
        MetalPipeline pipeline = function.makePipeline();

        MetalBuffer bufA = device.makeBuffer(N * Float.BYTES);
        MetalBuffer bufB = device.makeBuffer(N * Float.BYTES);
        MetalBuffer bufC = device.makeBuffer(N * Float.BYTES);

        System.out.println("created buffers");
        float[] a = new float[N];
        float[] b = new float[N];

        for (int i = 0; i < N; i++) {
            a[i] = i;
            b[i] = i + 1;
        }

        bufA.put(a);
        bufB.put(b);

        MetalCommandQueue queue = device.makeCommandQueue();
        MetalCommandBuffer commandBuffer = queue.makeCommandBuffer();

        try (MetalEncoder encoder = commandBuffer.makeEncoder(pipeline)) {
            encoder.setBuffer(bufA, 0);
            encoder.setBuffer(bufB, 1);
            encoder.setBuffer(bufC, 2);

            encoder.dispatchThreads(N, 1, 1, 512, 1, 1);
        }

        long start = System.nanoTime();
        commandBuffer.commit();
        commandBuffer.waitUntilCompleted();
        long end = System.nanoTime();

        double took = (end - start) / 1e6;
        System.out.println("Took " + took + " millis");

        float[] C = new float[E];
        bufC.get(C);

        System.out.println("Device: " + device.getName());
        System.out.println("Computed on GPU: " + Arrays.toString(C));
    }
}
