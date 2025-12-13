package org.compute4j;

import org.compute4j.device.ComputeDevice;

public interface ComputeBackend {
    int getDeviceCount() throws Throwable;
    boolean isAvailable();
    BackendType getType();
    ComputeDevice createSystemDevice(int index) throws Throwable;
}
