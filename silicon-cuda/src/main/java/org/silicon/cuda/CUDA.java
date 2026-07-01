package org.silicon.cuda;

import org.silicon.api.SiliconException;
import org.silicon.api.Platform;
import org.silicon.api.backend.BackendType;
import org.silicon.api.backend.ComputeBackend;
import org.silicon.cuda.device.CudaDevice;

import java.io.InputStream;
import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import static org.silicon.cuda.Bindings.*;

public class CUDA implements ComputeBackend {

    public static final Linker LINKER = Linker.nativeLinker();
    public static final SymbolLookup LOOKUP;

    private static final List<String> LOAD_FAILURES = new ArrayList<>();
    private static Throwable LOAD_FAILURE;


    static {
        LOOKUP = loadFromSystem();

        if (LOOKUP != null) {
            try {
                init();
            } catch (Throwable e) {
                recordLoadFailure("CUDA native library loaded, but cuda_init failed", e);
            }
        }
    }

    @Override
    public int deviceCount() {
        if (CU_DEVICE_GET_COUNT == null) return 0;

        try (Arena arena = Arena.ofConfined()) {
            MemorySegment count = arena.allocate(ValueLayout.JAVA_INT);

            int result = (int) CU_DEVICE_GET_COUNT.invokeExact(count);

            if (result != 0) {
                throw new SiliconException("cuDeviceCount() failed: " + CUResult.fromCode(result));
            }

            return count.get(ValueLayout.JAVA_INT, 0);
        } catch (Throwable e) {
            throw new SiliconException("getDeviceCount() failed", e);
        }
    }
    
    @Override
    public boolean isAvailable() {
        if (Platform.isMacOS()) return false;

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
        if (!isAvailable()) {
            throw unavailableException();
        }
        
        int count = deviceCount();
        
        if (index < 0 || index >= count) {
            throw new IllegalArgumentException("Index " + index + " out of range! Device count: " + count);
        }

        try (Arena arena = Arena.ofConfined()) {
            MemorySegment device = arena.allocate(CU_DEVICE);
            int result = (int) CU_DEVICE_GET.invokeExact(device, index);

            if (result != 0) {
                throw new SiliconException("cuDeviceGet failed: " + CUResult.fromCode(result));
            }

            int devicePtr = device.get(ValueLayout.JAVA_INT, 0);
            return new CudaDevice(devicePtr, index);
        } catch (Throwable e) {
            throw new SiliconException("createSystemDevice(int) failed", e);
        }
    }
    
    public static void init() {
        try {
            CU_INIT.invoke(0);
        } catch (Throwable e) {
            throw new SiliconException("init() failed", e);
        }
    }

    public static SymbolLookup loadFromSystem() {
        String library = switch (Platform.current()) {
            case WINDOWS -> "nvcuda.dll";
            case LINUX -> "libcuda.so.1";
            default -> throw new UnsupportedOperationException();
        };

        try{
            return SymbolLookup.libraryLookup(library, Arena.global());
        } catch (Exception e) {
            LOAD_FAILURE = e;
            return null;
        }
    }

    public static SymbolLookup loadFromResources(String baseName) {
        var classifier = Platform.platformClassifier();

        if (classifier.isEmpty()) {
            LOAD_FAILURES.add("Unsupported platform: " + Platform.platformDescription());
            return null;
        }

        String resource = "/natives/" + classifier.get() + "/" + Platform.nativeLibraryName(baseName).get();

        return loadResource(resource);
    }

    private static SymbolLookup loadResource(String resourceName) {
        try (InputStream in = CUDA.class.getResourceAsStream(resourceName)) {
            if (in == null) {
                LOAD_FAILURES.add("Resource not found: " + resourceName);
                return null;
            }

            String suffix = resourceName.substring(resourceName.lastIndexOf('.'));
            Path tempFile = Files.createTempFile("silicon-cuda-", suffix);

            Files.copy(in, tempFile, StandardCopyOption.REPLACE_EXISTING);
            tempFile.toFile().deleteOnExit();

            return SymbolLookup.libraryLookup(tempFile.toString(), Arena.global());
        } catch (Exception e) {
            LOAD_FAILURES.add("Failed to load " + resourceName + ": " + e.getMessage());
            return null;
        }
    }

    public static List<String> loadFailures() {
        return List.copyOf(LOAD_FAILURES);
    }

    public static Throwable loadFailure() {
        return LOAD_FAILURE;
    }

    @Override
    public String unavailableReason() {
        return unavailableReasonMessage();
    }

    @Override
    public Throwable unavailableCause() {
        return LOAD_FAILURE;
    }

    private static IllegalStateException unavailableException() {
        String reason = unavailableReasonMessage();

        if (reason == null || reason.isBlank()) {
            reason = "no CUDA device is available";
        }

        return new IllegalStateException("This backend is not available on this platform: " + reason, LOAD_FAILURE);
    }

    private static String unavailableReasonMessage() {
        if (LOAD_FAILURES.isEmpty()) {
            return null;
        }
        return String.join("; ", LOAD_FAILURES);
    }

    private static void recordLoadFailure(String message, Throwable cause) {
        if (cause == null) {
            LOAD_FAILURES.add(message);
        } else {
            LOAD_FAILURES.add(message + ": " + cause.getMessage());
            if (LOAD_FAILURE == null) {
                LOAD_FAILURE = cause;
            }
        }
    }
}
