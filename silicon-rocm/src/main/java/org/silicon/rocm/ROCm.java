package org.silicon.rocm;

import org.silicon.api.backend.BackendType;
import org.silicon.api.backend.ComputeBackend;
import org.silicon.api.device.ComputeDevice;

import java.lang.foreign.SymbolLookup;

public class ROCm implements ComputeBackend {

    public static SymbolLookup LOOKUP;

    @Override
    public int deviceCount() {
        return 0;
    }

    @Override
    public boolean available() {
        return false;
    }

    @Override
    public BackendType type() {
        return null;
    }

    @Override
    public void init() {

    }

    @Override
    public ComputeDevice createDevice(int index) {
        return null;
    }
}
