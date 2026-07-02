package org.silicon.metal;

import org.silicon.api.SiliconException;
import org.silicon.api.Platform;
import org.silicon.api.backend.BackendType;
import org.silicon.api.backend.ComputeBackend;
import org.silicon.metal.device.MetalDevice;

import java.io.InputStream;
import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Metal implements ComputeBackend {

    private static final List<String> LOAD_FAILURES = new ArrayList<>();

    public static SymbolLookup LOOKUP;
    public static MethodHandle METAL_CREATE_SYSTEM_DEVICE;

    @Override
    public int deviceCount() {
        // TODO: maybe have a proper implementation for this?
        // Though I have never seen a Mac with more than 1 GPU
        return 1;
    }

    @Override
    public boolean isAvailable() {
        try {
            init();
            return Platform.isMacOS() && deviceCount() > 0;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public BackendType type() {
        return BackendType.METAL;
    }

    @Override
    public void init() {
        if (LOOKUP != null) return;

        LOOKUP = loadFromResources();

        if (LOOKUP == null) {
            throw new SiliconException("Failed to load library!");
        }

        METAL_CREATE_SYSTEM_DEVICE = MetalObject.find(
            "metal_create_system_device",
            FunctionDescriptor.of(ValueLayout.ADDRESS)
        );
    }

    @Override
    public MetalDevice createDevice(int index) {
        try {
            if (METAL_CREATE_SYSTEM_DEVICE == null || !isAvailable()) {
                throw new IllegalStateException("This backend is not available on this platform: " + LOAD_FAILURES);
            }

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

    public static SymbolLookup loadFromResources() {
        Optional<String> classifier = Platform.platformClassifier();

        if (classifier.isEmpty()) {
            throw new SiliconException("Unsupported platform: " + Platform.platformDescription());
        }

        Optional<String> lib = Platform.nativeLibraryName("metal");

        if (lib.isEmpty()) {
            throw new SiliconException("No library found for metal. Do natives exist?");
        }

        String resource = "/natives/" + classifier.get() + "/" + lib.get();
        return loadResource(resource);
    }

    private static SymbolLookup loadResource(String resourceName) {
        try (InputStream in = Metal.class.getResourceAsStream(resourceName)) {
            if (in == null) {
                LOAD_FAILURES.add("Resource not found: " + resourceName);
                return null;
            }

            String suffix = resourceName.substring(resourceName.lastIndexOf('.'));
            Path tempFile = Files.createTempFile("silicon-metal-", suffix);

            Files.copy(in, tempFile, StandardCopyOption.REPLACE_EXISTING);
            tempFile.toFile().deleteOnExit();

            return SymbolLookup.libraryLookup(tempFile.toString(), Arena.global());
        } catch (Exception e) {
            e.printStackTrace(System.err);
            throw new SiliconException("Failed to load " + resourceName + ": " + e.getMessage());
        }
    }

    private static String unavailableReasonMessage() {
        if (LOAD_FAILURES.isEmpty()) {
            return null;
        }
        return String.join("\n * ", LOAD_FAILURES);
    }
}
