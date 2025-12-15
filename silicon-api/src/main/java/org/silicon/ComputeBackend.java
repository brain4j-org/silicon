package org.silicon;

import org.silicon.device.ComputeDevice;

public interface ComputeBackend {
    int getDeviceCount() throws Throwable;
    boolean isAvailable();
    BackendType getType();
    ComputeDevice createSystemDevice(int index) throws Throwable;

    default String getName() {
        return getType().getName();
    }

    default ComputeDevice createSystemDevice() throws Throwable {
        return createSystemDevice(0);
    }
}
