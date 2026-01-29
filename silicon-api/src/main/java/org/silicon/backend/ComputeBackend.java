package org.silicon.backend;

import org.silicon.device.ComputeDevice;

public interface ComputeBackend {
    int deviceCount();

    boolean isAvailable();

    BackendType type();

    ComputeDevice createDevice(int index);

    default String name() {
        return type().formalName();
    }

    default ComputeDevice createDevice() {
        return createDevice(0);
    }
}