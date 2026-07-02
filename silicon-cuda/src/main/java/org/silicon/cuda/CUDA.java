package org.silicon.cuda;

import org.silicon.api.SiliconException;
import org.silicon.api.Platform;
import org.silicon.api.backend.BackendType;
import org.silicon.api.backend.ComputeBackend;
import org.silicon.cuda.device.CudaDevice;

import java.lang.foreign.*;

import static org.silicon.cuda.Bindings.*;

public class CUDA implements ComputeBackend {

    public static SymbolLookup LOOKUP;

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
            init();
            return deviceCount() > 0;
        } catch (Throwable e) {
            return false;
        }
    }
    
    @Override
    public BackendType type() {
        return BackendType.CUDA;
    }

    @Override
    public void init() {
        if (LOOKUP != null) return;

        LOOKUP = loadFromSystem();

        try {
            CU_INIT.invoke(0);
        } catch (Throwable e) {
            throw new SiliconException("init() failed", e);
        }
    }

    @Override
    public CudaDevice createDevice(int index) {
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

    public static SymbolLookup loadFromSystem() {
        String library = switch (Platform.current()) {
            case WINDOWS -> "nvcuda.dll";
            case LINUX -> "libcuda.so.1";
            default -> throw new UnsupportedOperationException();
        };

        return SymbolLookup.libraryLookup(library, Arena.global());
    }
}
