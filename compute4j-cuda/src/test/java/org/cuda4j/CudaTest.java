package org.cuda4j;

import org.cuda4j.buffer.CudaBuffer;
import org.cuda4j.buffer.CudaPointer;
import org.cuda4j.context.CudaContext;
import org.cuda4j.context.CudaFunction;
import org.cuda4j.context.CudaStream;
import org.cuda4j.device.CudaDevice;
import org.cuda4j.device.CudaModule;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public class CudaTest {
    
    public static final int N = 512 * 512 * 512;
    public static final int E = 16;
    
    public static void main(String[] args) throws Throwable {
        benchmarkCuda();
    }
    
    private static void benchmarkCuda() throws Throwable {
        CUDA cuda = new CUDA();
        CudaDevice device = cuda.createSystemDevice(0);
        
        System.out.println("CUDA Device name: " + device.getName());
        System.out.println("CUDA Device count: " + cuda.getDeviceCount());
        
        CudaContext context = device.createContext().setCurrent();

        CudaModule module = context.loadModule("resources/vector_add.ptx");
        CudaStream stream = context.createQueue();
        
        CudaFunction function = module.getFunction("vecAdd");
        
        float[] a = new float[N];
        float[] b = new float[N];
        
        for (int i = 0; i < N; i++) {
            a[i] = i;
            b[i] = i + 1;
        }
        
        CudaBuffer bufA = CUDA.allocateFor(a, N * Float.BYTES);
        CudaBuffer bufB = CUDA.allocateFor(b, N * Float.BYTES);
        CudaBuffer bufC = CUDA.allocateBytes(N * Float.BYTES);
        
        int blockSize = 256;
        int gridSize = (N + blockSize - 1) / blockSize;
        
        CudaPointer kernelArgs = CudaPointer.from(
            CudaPointer.fromBuffer(bufA),
            CudaPointer.fromBuffer(bufB),
            CudaPointer.fromBuffer(bufC),
            CudaPointer.fromInt(N)
        );
        
        long start = System.nanoTime();
        function.launch(gridSize, 1, 1, blockSize, 1, 1, 0, stream, kernelArgs);
        stream.awaitCompletion();
        long end = System.nanoTime();
        double took = (end - start) / 1e6;
        
        System.out.println("Took " + took + " millis");
        float[] C = new float[E];
        bufC.copyToHost(C);
        
        System.out.println("Device: " + device.getName());
        System.out.println("Computed on GPU: " + Arrays.toString(C));
    }
}
