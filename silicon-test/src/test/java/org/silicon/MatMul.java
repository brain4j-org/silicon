package org.silicon;

import org.silicon.api.Silicon;
import org.silicon.api.backend.BackendType;
import org.silicon.api.device.*;
import org.silicon.api.function.ComputeFunction;
import org.silicon.api.function.ComputeModule;
import org.silicon.api.kernel.ComputeArgs;
import org.silicon.api.kernel.ComputeQueue;
import org.silicon.api.kernel.ComputeSize;
import org.silicon.api.slang.SlangCompiler;

import java.util.Arrays;

public class MatMul {

    private static final int CONST = 4096;
    private static final int M = CONST;
    private static final int N = CONST;
    private static final int K = CONST;

    private static final float[] A_HOST = generateA();
    private static final float[] B_HOST = generateB();

    public static void main(String[] args) {
        System.err.println("Warning: This test allocates large GPU buffers");
        System.out.println("Chosen backend: " + Silicon.backend().name());

        Silicon.chooseBackend(BackendType.CUDA);

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
    
    // Do not use: the kernel is currently broken.
    private static void runTensorFp16MatMul(ComputeContext context, SlangCompiler compiler) {
        System.out.println("\n=== FP16 Tensor MatMul ===");
        
        ComputeModule module = compiler.compileFromResource("fp16/tensor_matmul_fp16.slang");
        ComputeFunction function = module.getFunction("matmul");
        
        try (ComputeArena arena = context.createArena()) {
            ComputeBuffer A = arena.allocateArray(A_HOST);
            ComputeBuffer B = arena.allocateArray(B_HOST);
            ComputeBuffer C = arena.allocateBytes((long) M * N * 4);
            
            ComputeSize globalSize = new ComputeSize(N, M, 1);
            ComputeSize groupSize = new ComputeSize(16, 16, 1);
            ComputeArgs args = ComputeArgs.of(A, B, C);
            
            dispatchMatMul(function, arena, args, globalSize, groupSize);
            
            float[] preview = new float[16];
            C.get(preview);
            System.out.println("FP32 result preview: " + Arrays.toString(preview));
        }
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
            
            ComputeSize globalSize = new ComputeSize(N, M, 1);
            ComputeSize groupSize = new ComputeSize(16, 16, 1);
            ComputeArgs args = ComputeArgs.of(A, B, C, sizes);

            dispatchMatMul(function, arena, args, globalSize, groupSize);

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
            
            int TILE = 16;
            int gx = ((N + TILE - 1) / TILE) * TILE;
            int gy = ((M + TILE - 1) / TILE) * TILE;
            
            ComputeSize globalSize = new ComputeSize(gx, gy, 1);
            ComputeSize groupSize  = new ComputeSize(TILE, TILE, 1);
            ComputeArgs args = ComputeArgs.of(A, B, C, sizes);
            
            dispatchMatMul(function, arena, args, globalSize, groupSize);

            float[] preview = new float[16];
            C.getHalf(preview);
            System.out.println("FP16 result preview: " + Arrays.toString(preview));
        }
    }

    private static void dispatchMatMul(
        ComputeFunction function,
        ComputeArena arena,
        ComputeArgs args,
        ComputeSize globalSize,
        ComputeSize groupSize
    ) {
        ComputeQueue queue = arena.createQueue();

        long start = System.nanoTime();
        queue.dispatch(function, globalSize, groupSize, args);
        queue.await();
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
