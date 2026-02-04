package org.silicon.api;

import org.silicon.api.backend.BackendType;
import org.silicon.api.backend.ComputeBackend;
import org.silicon.api.device.ComputeDevice;

import java.util.ServiceLoader;

/**
 * Entry point for backend selection and device creation.
 * <p>
 * Uses {@link ServiceLoader} to discover available {@link ComputeBackend} implementations.
 * Can explicitly choose a backend or auto-select the best available by priority.
 */
public class Silicon {

    private static volatile ComputeBackend backend;

    /**
     * Explicitly selects a backend by type.
     * @param backendType backend type to use
     * @throws NullPointerException if backendType is null
     * @throws IllegalStateException if the backend is not available
     */
    public static void chooseBackend(BackendType backendType) {
        if (backendType == null) {
            throw new NullPointerException("Chosen backend type cannot be null");
        }
        
        ServiceLoader<ComputeBackend> loader = ServiceLoader.load(ComputeBackend.class);

        for (ComputeBackend backend : loader) {
            if (!backend.type().equals(backendType)) continue;

            Silicon.backend = backend;
            return;
        }
        
        throw new IllegalStateException("No backend with name '" + backendType.formalName() + "' was found on this system");
    }
    
    private static ComputeBackend loadBackend() {
        ServiceLoader<ComputeBackend> loader = ServiceLoader.load(ComputeBackend.class);
        ComputeBackend best = null;
        
        for (ComputeBackend backend : loader) {
            if (!backend.isAvailable()) continue;
            
            if (best == null) {
                best = backend;
                continue;
            }
            
            if (isBetter(backend, best)) {
                best = backend;
            }
        }
        
        if (best == null) {
            throw new IllegalStateException("No compute backend available on this system");
        }
        
        return best;
    }
    
    private static boolean isBetter(ComputeBackend candidate, ComputeBackend current) {
        return candidate.type().priority() < current.type().priority();
    }

    /**
     * Creates the first device exposed by the selected backend.
     * @return compute device
     */
    public static ComputeDevice createDevice() {
        return createDevice(0);
    }

    /**
     * Creates a device by index exposed by the selected backend.
     * @param index device index
     * @return compute device
     */
    public static ComputeDevice createDevice(int index) {
        return backend().createDevice(index);
    }

    /**
     * Returns the active backend, selecting the best available if none was chosen.
     * @return active backend
     */
    public static ComputeBackend backend() {
        if (backend == null) {
            backend = loadBackend();
        }
        return backend;
    }
}
