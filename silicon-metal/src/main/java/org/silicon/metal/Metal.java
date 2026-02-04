package org.silicon.metal;

import org.silicon.api.SiliconException;
import org.silicon.api.backend.BackendType;
import org.silicon.api.backend.ComputeBackend;
import org.silicon.metal.device.MetalDevice;

import java.io.InputStream;
import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class Metal implements ComputeBackend {
    
    public static final Linker LINKER = Linker.nativeLinker();

    public static final SymbolLookup LOOKUP;
    public static final MethodHandle METAL_CREATE_SYSTEM_DEVICE;

    static {
        LOOKUP = loadFromResources("/libmetal4j.dylib");

        if (LOOKUP != null) {
            METAL_CREATE_SYSTEM_DEVICE = MetalObject.find(
                "metal_create_system_device",
                FunctionDescriptor.of(ValueLayout.ADDRESS)
            );
        } else {
            METAL_CREATE_SYSTEM_DEVICE = null;
        }
    }

    @Override
    public int deviceCount() {
        if (METAL_CREATE_SYSTEM_DEVICE == null) return 0;

        return 1;
    }

    @Override
    public boolean isAvailable() {
        return deviceCount() > 0;
    }

    @Override
    public BackendType type() {
        return BackendType.METAL;
    }

    @Override
    public MetalDevice createDevice(int index) {
        try {
            if (index != 0) {
                throw new IllegalArgumentException("Index should be equal to 0 when using Metal");
            }

            MemorySegment ptr = (MemorySegment) METAL_CREATE_SYSTEM_DEVICE.invokeExact();
            
            if (ptr == null || ptr.address() == 0) {
                throw new SiliconException("metalCreateSystemDevice failed");
            }
            
            return new MetalDevice(ptr);
        } catch (Throwable e) {
            throw new SiliconException("createSystemDevice(int) failed", e);
        }
    }

    @Override
    public MetalDevice createDevice() {
        return createDevice(0);
    }

    public static SymbolLookup loadFromResources(String resourceName) {
        try (InputStream in = MetalObject.class.getResourceAsStream(resourceName)) {
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
