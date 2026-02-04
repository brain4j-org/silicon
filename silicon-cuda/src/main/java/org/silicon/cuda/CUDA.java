package org.silicon.cuda;

import org.silicon.api.SiliconException;
import org.silicon.api.backend.BackendType;
import org.silicon.api.backend.ComputeBackend;
import org.silicon.cuda.device.CudaDevice;

import java.io.InputStream;
import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class CUDA implements ComputeBackend {
    
    public static final Linker LINKER = Linker.nativeLinker();
    public static final SymbolLookup LOOKUP;
    public static final MethodHandle CUDA_INIT;
    public static final MethodHandle CUDA_DEVICE_COUNT;
    public static final MethodHandle CUDA_CREATE_SYSTEM_DEVICE;
    
    static {
        LOOKUP = loadFromResources("/libcuda4j.dll");

        if (LOOKUP != null) {
            CUDA_INIT = CudaObject.find(
                "cuda_init",
                FunctionDescriptor.ofVoid()
            );
            CUDA_DEVICE_COUNT = CudaObject.find(
                "cuda_device_count",
                FunctionDescriptor.of(ValueLayout.JAVA_INT)
            );
            CUDA_CREATE_SYSTEM_DEVICE = CudaObject.find(
                "cuda_create_system_device",
                FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.JAVA_INT)
            );

            try {
                init();
            } catch (Throwable _) {
                // ignore
            }
        } else {
            CUDA_INIT = null;
            CUDA_DEVICE_COUNT = null;
            CUDA_CREATE_SYSTEM_DEVICE = null;
        }
    }
    
    @Override
    public int deviceCount() {
        if (CUDA_DEVICE_COUNT == null) return 0;

        try {
            return (int) CUDA_DEVICE_COUNT.invokeExact();
        } catch (Throwable e) {
            throw new SiliconException("getDeviceCount() failed", e);
        }
    }
    
    @Override
    public boolean isAvailable() {
        try {
            return deviceCount() > 0;
        } catch (Throwable _) {
            return false;
        }
    }
    
    @Override
    public BackendType type() {
        return BackendType.CUDA;
    }
    
    @Override
    public CudaDevice createDevice(int index) {
        if (CUDA_CREATE_SYSTEM_DEVICE == null || !isAvailable()) {
            throw new IllegalStateException("This backend is not available on this platform");
        }
        
        int count = deviceCount();
        
        if (index < 0 || index >= count) {
            throw new IllegalArgumentException("Index " + index + " out of range! Device count: " + count);
        }

        try {
            MemorySegment ptr = (MemorySegment) CUDA_CREATE_SYSTEM_DEVICE.invokeExact(index);
            
            if (ptr == null || ptr.address() == 0) {
                throw new SiliconException("cuDeviceGet failed");
            }
            
            return new CudaDevice(ptr, index);
        } catch (Throwable e) {
            throw new SiliconException("createSystemDevice(int) failed", e);
        }
    }
    
    public static void init() {
        try {
            CUDA_INIT.invokeExact();
        } catch (Throwable e) {
            throw new SiliconException("init() failed", e);
        }
    }
    
    public static SymbolLookup loadFromResources(String resourceName) {
        try (InputStream in = CUDA.class.getResourceAsStream(resourceName)) {
            if (in == null) {
                throw new IllegalArgumentException("Resource not found: " + resourceName);
            }
            
            String suffix = resourceName.substring(resourceName.lastIndexOf('.'));
            Path tempFile = Files.createTempFile("nativeLib", suffix);
            
            Files.copy(in, tempFile, StandardCopyOption.REPLACE_EXISTING);
            tempFile.toFile().deleteOnExit();
            
            return SymbolLookup.libraryLookup(tempFile.toString(), Arena.global());
        } catch (Exception _) {
            return null;
        }
    }
}
