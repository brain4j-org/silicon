package org.silicon;

import org.silicon.computing.*;
import org.silicon.device.*;
import org.silicon.kernel.ComputeFunction;
import org.silicon.kernel.ComputeModule;
import org.silicon.slang.SlangCompiler;

import java.util.Arrays;

public class MatMul {

    private static final int M = 2048;
    private static final int N = 2048;
    private static final int K = 2048;

    private static final float[] A_HOST = generateA();
    private static final float[] B_HOST = generateB();

    public static void main(String[] args) {
        System.err.println("Warning: This test allocates large GPU buffers");
        System.out.println("Chosen backend: " + Silicon.backend().name());

        ComputeDevice device = Silicon.createDevice();
        ComputeContext context = device.createContext();

        printDeviceInfo(device);

        SlangCompiler compiler = new SlangCompiler(context);

        if (device.supports(DeviceFeature.FP16)) {
            runFp16MatMul(context, compiler);
        } else {
            System.out.println("FP16 not supported on this device");
        }

        runFp32MatMul(context, compiler);
    }

    private static void runFp32MatMul(ComputeContext context, SlangCompiler compiler) {
        System.out.println("\n=== FP32 MatMul ===");

        ComputeModule module = compiler.compileFromResource("fp32/matmul_fp32.slang");
        ComputeFunction function = module.getFunction("matmul");

        try (ComputeArena arena = context.createArena()) {
            ComputeBuffer A = arena.allocateArray(A_HOST);
            ComputeBuffer B = arena.allocateArray(B_HOST);
            ComputeBuffer C = arena.allocateBytes((long) M * N * 4);

            ComputeBuffer sizes = arena.allocateArray(new int[] { M, N, K });

            dispatchMatMul(function, arena, A, B, C, sizes);

            float[] preview = new float[16];
            C.get(preview);
            System.out.println("FP32 result preview: " + Arrays.toString(preview));
        }
    }

    private static void runFp16MatMul(ComputeContext context, SlangCompiler compiler) {
        System.out.println("\n=== FP16 MatMul ===");

        ComputeModule module = compiler.compileFromResource("fp16/matmul_fp16.slang");
        ComputeFunction function = module.getFunction("matmul");

        try (ComputeArena arena = context.createArena()) {
            ComputeBuffer A = arena.allocateHalf(A_HOST);
            ComputeBuffer B = arena.allocateHalf(B_HOST);
            ComputeBuffer C = arena.allocateBytes((long) M * N * 2);

            ComputeBuffer sizes = arena.allocateArray(new int[] { M, N, K });

            dispatchMatMul(function, arena, A, B, C, sizes);

            float[] preview = new float[16];
            C.getHalf(preview);
            System.out.println("FP16 result preview: " + Arrays.toString(preview));
        }
    }

    private static void dispatchMatMul(
        ComputeFunction function,
        ComputeArena arena,
        ComputeBuffer A,
        ComputeBuffer B,
        ComputeBuffer C,
        ComputeBuffer sizes
    ) {
        ComputeArgs args = ComputeArgs.of(A, B, C, sizes);

        ComputeSize globalSize = new ComputeSize(N, M, 1);
        ComputeSize groupSize = new ComputeSize(32, 32, 1);

        ComputeQueue queue = arena.createQueue();

        long start = System.nanoTime();
        queue.dispatch(function, globalSize, groupSize, args);
        queue.awaitCompletion();
        long end = System.nanoTime();

        System.out.printf(
            "Execution time: %.2f ms%n",
            (end - start) / 1e6
        );
    }

    private static void printDeviceInfo(ComputeDevice device) {
        System.out.println("========== Device ==========");
        System.out.println("Name:     " + device.name());
        System.out.println("Vendor:   " + device.vendor());
        System.out.println("Memory:   " + device.memorySize());
        System.out.println("FP16:     " + device.supports(DeviceFeature.FP16));
        System.out.println("FP64:     " + device.supports(DeviceFeature.FP64));
    }

    private static float[] generateA() {
        float[] a = new float[M * K];
        for (int i = 0; i < a.length; i++) {
            a[i] = (i % 13) * 0.1f;
        }
        return a;
    }

    private static float[] generateB() {
        float[] b = new float[K * N];
        for (int i = 0; i < b.length; i++) {
            b[i] = (i % 7) * 0.2f;
        }
        return b;
    }
}
