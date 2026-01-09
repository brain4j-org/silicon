package org.silicon.backend;

import org.silicon.device.ComputeDevice;

public interface ComputeBackend {
    int getDeviceCount();
    boolean isAvailable();
    BackendType getType();
    ComputeDevice createSystemDevice(int index);

    default String getName() {
        return getType().getName();
    }

    default ComputeDevice createSystemDevice() {
        return createSystemDevice(0);
    }
}
