package org.silicon.cuda;

import org.silicon.BackendType;
import org.silicon.ComputeBackend;
import org.silicon.cuda.device.CudaDevice;

import java.io.IOException;
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
            CUDA_INIT = LINKER.downcallHandle(
                LOOKUP.find("cuda_init").orElse(null),
                FunctionDescriptor.ofVoid()
            );
            CUDA_DEVICE_COUNT = LINKER.downcallHandle(
                LOOKUP.find("cuda_device_count").orElse(null),
                FunctionDescriptor.of(ValueLayout.JAVA_INT)
            );
            CUDA_CREATE_SYSTEM_DEVICE = LINKER.downcallHandle(
                LOOKUP.find("cuda_create_system_device").orElse(null),
                FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.JAVA_INT)
            );
        } else {
            CUDA_INIT = null;
            CUDA_DEVICE_COUNT = null;
            CUDA_CREATE_SYSTEM_DEVICE = null;
        }

        try {
            init();
        } catch (Throwable _) {
            // ignore
        }
    }
    
    @Override
    public int getDeviceCount() throws Throwable {
        return (int) CUDA_DEVICE_COUNT.invokeExact();
    }
    
    @Override
    public boolean isAvailable() {
        try {
            return getDeviceCount() > 0;
        } catch (Throwable _) {
            return false;
        }
    }
    
    @Override
    public BackendType getType() {
        return BackendType.CUDA;
    }
    
    @Override
    public CudaDevice createSystemDevice(int index) throws Throwable {
        MemorySegment ptr = (MemorySegment) CUDA_CREATE_SYSTEM_DEVICE.invokeExact(index);
        return new CudaDevice(ptr, index);
    }
    
    public static void init() throws Throwable {
        CUDA_INIT.invokeExact();
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
