package org.silicon;

import org.silicon.api.Silicon;
import org.silicon.api.device.*;
import org.silicon.api.function.ComputeFunction;
import org.silicon.api.function.ComputeModule;
import org.silicon.api.kernel.ComputeArgs;
import org.silicon.api.kernel.ComputeEvent;
import org.silicon.api.kernel.ComputeQueue;
import org.silicon.api.kernel.ComputeSize;
import org.silicon.api.slang.SlangCompiler;

import java.util.Arrays;

public class VectorAdd {
    
    private static final int N = 536870912;
    private static final float[] A_HOST = generateData();
    private static final float[] B_HOST = generateData();
    
    public static void main(String[] args) {
        System.err.println("Warning: This test needs a big amount of RAM!");
        System.err.println("If the JVM crashes make sure to change -Xmx JVM argument");

        System.out.println("Chosen backend: " + Silicon.backend().name());
        
        ComputeDevice device = Silicon.createDevice();
        ComputeContext context = device.createContext();
        
        printDeviceInfo(device);
        
        SlangCompiler compiler = new SlangCompiler(context);
        
        if (device.supports(DeviceFeature.FP16)) {
            runFp16VectorAdd(context, compiler);
        } else {
            System.out.println("FP16 not supported on this device");
        }
        
        runFp32VectorAdd(context, compiler);
    }
    
    private static void runFp16VectorAdd(ComputeContext context, SlangCompiler compiler) {
        System.out.println("\n=== FP16 vector add ===");
        
        ComputeModule module = compiler.compileFromResource("fp16/vector_add_fp16.slang");
        ComputeFunction function = module.getFunction("add");
        
        try (ComputeArena arena = context.createArena()) {
            ComputeBuffer a = arena.allocateHalf(A_HOST);
            ComputeBuffer b = arena.allocateHalf(B_HOST);
            ComputeBuffer c = arena.allocateBytes((long) N * 2); // half = 2 byte
            
            dispatch(function, arena, a, b, c);
            
            float[] preview = new float[16];
            c.getHalf(preview);
            System.out.println("FP16 result preview: " + Arrays.toString(preview));
        }
    }
    
    private static void runFp32VectorAdd(ComputeContext context, SlangCompiler compiler) {
        System.out.println("\n=== FP32 vector add ===");
        
        ComputeModule module = compiler.compileFromResource("fp32/vector_add_fp32.slang");
        ComputeFunction function = module.getFunction("add");
        
        try (ComputeArena arena = context.createArena()) {
            ComputeBuffer a = arena.allocateArray(A_HOST);
            ComputeBuffer b = arena.allocateArray(B_HOST);
            ComputeBuffer c = arena.allocateBytes((long) N * 4);

            dispatch(function, arena, a, b, c);
            
            float[] preview = new float[16];
            c.get(preview);
            System.out.println("FP32 result preview: " + Arrays.toString(preview));
        }
    }
    
    private static void dispatch(
        ComputeFunction function,
        ComputeArena arena,
        ComputeBuffer a,
        ComputeBuffer b,
        ComputeBuffer c
    ) {
        ComputeArgs args = ComputeArgs.of(a, b, c);
        
        ComputeSize globalSize = new ComputeSize(N, 1, 1);
        ComputeSize groupSize  = new ComputeSize(function.maxWorkGroupSize(), 1, 1);
        
        ComputeQueue queue = arena.createQueue();
        
        long start = System.nanoTime();
        
        ComputeEvent event = queue.dispatchAsync(function, globalSize, groupSize, args);
        event.future().thenRun(() -> System.out.println("Complted kernel!"));
        event.await();
        
        long end = System.nanoTime();
        
        System.out.printf("Execution time: %.2f ms%n", (end - start) / 1e6);
    }
    
    private static void printDeviceInfo(ComputeDevice device) {
        System.out.println("========== Device ==========");
        System.out.println("Name:     " + device.name());
        System.out.println("Vendor:   " + device.vendor());
        System.out.println("Memory:   " + device.memorySize());
        System.out.println("FP16:     " + device.supports(DeviceFeature.FP16));
        System.out.println("FP64:     " + device.supports(DeviceFeature.FP64));
    }
    
    private static float[] generateData() {
        float[] data = new float[VectorAdd.N];
        for (int i = 0; i < VectorAdd.N; i++) {
            data[i] = i;
        }
        return data;
    }
}
