package org.silicon.device;

public interface ComputeDevice {
    ComputeContext createContext();
    String getName();
}
