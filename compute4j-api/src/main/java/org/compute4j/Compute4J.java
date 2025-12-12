package org.compute4j;

import org.compute4j.device.ComputeDevice;

import java.util.ServiceLoader;

public class Compute4J {

    private static ComputeBackend backend;
    
    static {
        Compute4J.backend = loadBackend();
    }
    
    public static void chooseBackend(String backendName) {
        ServiceLoader<ComputeBackend> loader = ServiceLoader.load(ComputeBackend.class);
        
        for (ComputeBackend backend : loader) {
            if (!backend.getName().equals(backendName)) continue;
            
            Compute4J.backend = backend;
            return;
        }
        
        throw new IllegalStateException("No backend with name '" + backendName + "' was found on this system!");
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
        return priority(candidate.getName()) < priority(current.getName());
    }

    public static ComputeDevice createSystemDevice() {
        return createSystemDevice(0);
    }

    public static ComputeDevice createSystemDevice(int index) {
        return backend.createSystemDevice(index);
    }

    public static ComputeBackend getBackend() {
        return backend;
    }
    
    private static int priority(String name) {
        return switch (name) {
            case "CUDA" -> 0;
            case "Metal" -> 1;
            case "OpenCL" -> 2;
            default -> 99;
        };
    }
}
