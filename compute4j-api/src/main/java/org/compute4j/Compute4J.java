package org.compute4j;

public class Compute4J {

    private static ComputeBackend backend;

    public static ComputeDevice createSystemDevice() {
        return createSystemDevice(0);
    }

    public static ComputeDevice createSystemDevice(int index) {
        return backend.createSystemDevice(index);
    }

    public static ComputeBackend getBackend() {
        return backend;
    }
}
