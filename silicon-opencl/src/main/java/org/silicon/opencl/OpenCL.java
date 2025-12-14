package org.silicon.opencl;

import org.silicon.BackendType;
import org.silicon.ComputeBackend;
import org.silicon.device.ComputeDevice;

public class OpenCL implements ComputeBackend {
    
    @Override
    public int getDeviceCount() throws Throwable {
        return 0;
    }
    
    @Override
    public boolean isAvailable() {
        return false;
    }
    
    @Override
    public BackendType getType() {
        return null;
    }
    
    @Override
    public ComputeDevice createSystemDevice(int index) throws Throwable {
        return null;
    }
}
