package org.silicon.api.device;

/**
 * Represents a compute device exposed by a backend.
 * <p>
 * Use it to create a {@link ComputeContext} for resource allocation and dispatch.
 * Provides metadata and feature queries for capability checks.
 */
public interface ComputeDevice {
    /**
     * Creates a new context bound to this device.
     * @return compute context
     */
    ComputeContext createContext();

    /**
     * @return human-readable device name
     */
    String name();

    /**
     * @return vendor name
     */
    String vendor();

    /**
     * @return total device memory in bytes
     */
    long memorySize();

    /**
     * Checks if a specific device feature is supported.
     * @param feature feature to check
     * @return true if supported
     */
    boolean supports(DeviceFeature feature);
}
