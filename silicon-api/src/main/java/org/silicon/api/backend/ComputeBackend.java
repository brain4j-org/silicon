package org.silicon.api.backend;

import org.silicon.api.device.ComputeDevice;

/**
 * Backend provider responsible for exposing devices and capabilities.
 * <p>
 * Implementations are discovered via {@link java.util.ServiceLoader}.
 */
public interface ComputeBackend {
    /**
     * @return number of devices exposed by this backend
     */
    int deviceCount();

    /**
     * @return true if this backend is available on the current system
     */
    boolean isAvailable();

    /**
     * @return backend type
     */
    BackendType type();

    /**
     * Creates a device by index.
     * @param index device index
     * @return compute device
     */
    ComputeDevice createDevice(int index);

    /**
     * @return human-readable backend name
     */
    default String name() {
        return type().formalName();
    }

    /**
     * Creates the first device (index 0).
     * @return compute device
     */
    default ComputeDevice createDevice() {
        return createDevice(0);
    }
}
