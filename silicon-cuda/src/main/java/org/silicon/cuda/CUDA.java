package org.silicon.cuda;

import org.silicon.api.SiliconException;
import org.silicon.api.NativeLibraryLoader;
import org.silicon.api.backend.BackendType;
import org.silicon.api.backend.ComputeBackend;
import org.silicon.cuda.device.CudaDevice;

import java.io.InputStream;
import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class CUDA implements ComputeBackend {

    private static final String RUNTIME_MANIFEST = "cuda4j-runtime-libraries.txt";
    
    public static final Linker LINKER = Linker.nativeLinker();
    public static final SymbolLookup LOOKUP;
    private static final List<String> LOAD_FAILURES = new ArrayList<>();
    private static final List<SymbolLookup> RUNTIME_LOOKUPS = new ArrayList<>();
    private static Throwable LOAD_FAILURE;
    public static final MethodHandle CUDA_INIT;
    public static final MethodHandle CUDA_DEVICE_COUNT;
    public static final MethodHandle CUDA_CREATE_SYSTEM_DEVICE;
    
    static {
        LOOKUP = loadFromResources("cuda4j");

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
            } catch (Throwable e) {
                recordLoadFailure("CUDA native library loaded, but cuda_init failed", e);
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
            throw unavailableException();
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

    public static SymbolLookup loadFromResources(String baseName) {
        List<String> nativeNames = NativeLibraryLoader.nativeLibraryNames(baseName);
        if (nativeNames.isEmpty()) {
            recordLoadFailure("Unsupported platform: " + NativeLibraryLoader.platformDescription(), null);
            return null;
        }

        List<String> resources = new ArrayList<>();
        for (String nativeName : nativeNames) {
            NativeLibraryLoader.platformClassifier()
                .ifPresent(platform -> resources.add("/natives/" + platform + "/" + nativeName));
            resources.add("/" + nativeName);
        }

        for (String resource : resources) {
            SymbolLookup lookup = loadResource(resource);
            if (lookup != null) {
                LOAD_FAILURES.clear();
                LOAD_FAILURE = null;
                return lookup;
            }
        }

        return null;
    }

    private static SymbolLookup loadResource(String resourceName) {
        if (CUDA.class.getResource(resourceName) == null) {
            recordLoadFailure("Resource not found: " + resourceName, null);
            return null;
        }

        try {
            Path tempDirectory = Files.createTempDirectory("silicon-cuda-");
            tempDirectory.toFile().deleteOnExit();

            for (Path runtimeLibrary : extractRuntimeLibraries(resourceName, tempDirectory)) {
                RUNTIME_LOOKUPS.add(SymbolLookup.libraryLookup(runtimeLibrary.toString(), Arena.global()));
            }

            Path nativeLibrary = extractResource(resourceName, tempDirectory);
            return SymbolLookup.libraryLookup(nativeLibrary.toString(), Arena.global());
        } catch (Exception e) {
            recordLoadFailure("Failed to load " + resourceName, e);
            return null;
        }
    }

    private static List<Path> extractRuntimeLibraries(String nativeResourceName, Path targetDirectory) throws Exception {
        if (!NativeLibraryLoader.isWindows()) {
            return List.of();
        }

        String resourceDirectory = resourceDirectory(nativeResourceName);
        String manifestResourceName = resourceDirectory + RUNTIME_MANIFEST;

        try (InputStream in = CUDA.class.getResourceAsStream(manifestResourceName)) {
            if (in == null) {
                recordLoadFailure("Swift runtime manifest not found: " + manifestResourceName, null);
                return List.of();
            }

            String manifest = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            List<String> libraryNames = manifest.lines()
                .map(String::trim)
                .filter(line -> !line.isEmpty())
                .filter(line -> !line.startsWith("#"))
                .distinct()
                .sorted(Comparator.comparingInt(CUDA::windowsRuntimeLoadPriority).thenComparing(String::compareToIgnoreCase))
                .toList();

            List<Path> runtimeLibraries = new ArrayList<>();
            for (String libraryName : libraryNames) {
                runtimeLibraries.add(extractResource(resourceDirectory + libraryName, targetDirectory));
            }
            return runtimeLibraries;
        }
    }

    private static Path extractResource(String resourceName, Path targetDirectory) throws Exception {
        String fileName = resourceName.substring(resourceName.lastIndexOf('/') + 1);
        Path target = targetDirectory.resolve(fileName);

        try (InputStream in = CUDA.class.getResourceAsStream(resourceName)) {
            if (in == null) {
                throw new IllegalStateException("Resource not found: " + resourceName);
            }

            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            target.toFile().deleteOnExit();
            return target;
        }
    }

    private static String resourceDirectory(String resourceName) {
        int lastSlash = resourceName.lastIndexOf('/');
        if (lastSlash < 0) {
            return "/";
        }
        return resourceName.substring(0, lastSlash + 1);
    }

    private static int windowsRuntimeLoadPriority(String libraryName) {
        return switch (libraryName.toLowerCase()) {
            case "swiftcrt.dll" -> 0;
            case "swiftwinsdk.dll" -> 1;
            case "swiftcore.dll" -> 2;
            default -> 3;
        };
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
