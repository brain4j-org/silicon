package org.silicon.device;

public interface ComputeDevice {
    ComputeContext createContext();

    String name();

    String vendor();

    long memorySize();

    boolean supports(DeviceFeature feature);
}
