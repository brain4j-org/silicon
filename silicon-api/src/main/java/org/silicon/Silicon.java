package org.silicon;

import org.silicon.backend.BackendType;
import org.silicon.backend.ComputeBackend;
import org.silicon.device.ComputeDevice;

import java.util.ServiceLoader;

public class Silicon {

    private static ComputeBackend backend;
    
    public static void chooseBackend(BackendType backendType) {
        if (backendType == null) {
            throw new NullPointerException("Chosen backend type cannot be null!");
        }
        
        ServiceLoader<ComputeBackend> loader = ServiceLoader.load(ComputeBackend.class);

        for (ComputeBackend backend : loader) {
            if (!backend.type().equals(backendType)) continue;

            Silicon.backend = backend;
            return;
        }
        
        throw new IllegalStateException("No backend with name '" + backendType.formalName() + "' was found on this system!");
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
            throw new IllegalStateException("No compute backend available on this system!");
        }
        
        return best;
    }
    
    private static boolean isBetter(ComputeBackend candidate, ComputeBackend current) {
        return candidate.type().priority() < current.type().priority();
    }

    public static ComputeDevice createDevice() {
        return createDevice(0);
    }

    public static ComputeDevice createDevice(int index) {
        return backend().createDevice(index);
    }

    public static ComputeBackend backend() {
        if (backend == null) {
            backend = loadBackend();
        }
        return backend;
    }
}
