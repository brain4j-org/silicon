package org.compute4j;

import java.nio.file.Path;

public interface ComputeBackend {
    ComputeDevice createSystemDevice(int index);
}
