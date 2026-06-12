package org.silicon.api;

import org.silicon.api.backend.BackendType;
import org.silicon.api.backend.ComputeBackend;
import org.silicon.api.device.ComputeDevice;

import java.util.ArrayList;
import java.util.List;
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
            if (!backend.isAvailable()) {
                throw unavailableBackendException(backend);
            }

            Silicon.backend = backend;
            return;
        }
        
        throw new IllegalStateException("No backend with name '" + backendType.formalName() + "' was found on this system");
    }
    
    private static ComputeBackend loadBackend() {
        ServiceLoader<ComputeBackend> loader = ServiceLoader.load(ComputeBackend.class);
        ComputeBackend best = null;
        List<String> unavailableReasons = new ArrayList<>();
        
        for (ComputeBackend backend : loader) {
            if (!backend.isAvailable()) {
                String reason = backend.unavailableReason();
                if (reason != null && !reason.isBlank()) {
                    unavailableReasons.add(backend.type().formalName() + ": " + reason);
                }
                continue;
            }
            
            if (best == null) {
                best = backend;
                continue;
            }
            
            if (isBetter(backend, best)) {
                best = backend;
            }
        }
        
        if (best == null) {
            String message = "No compute backend available on this system";
            if (!unavailableReasons.isEmpty()) {
                message += ": " + String.join("; ", unavailableReasons);
            }
            throw new IllegalStateException(message);
        }
        
        return best;
    }

    private static IllegalStateException unavailableBackendException(ComputeBackend backend) {
        String message = "Backend '" + backend.type().formalName() + "' is not available on this system";
        String reason = backend.unavailableReason();
        if (reason != null && !reason.isBlank()) {
            message += ": " + reason;
        }

        Throwable cause = backend.unavailableCause();
        if (cause == null) {
            return new IllegalStateException(message);
        }
        return new IllegalStateException(message, cause);
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
