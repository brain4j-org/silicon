package org.silicon.backend;

public enum BackendType {
    CUDA("CUDA", 0),
    METAL("Metal", 1),
    OPENCL("OpenCL", 2);
    
    private final String name;
    private final int priority;
    
    BackendType(String name, int priority) {
        this.name = name;
        this.priority = priority;
    }
    
    public String getName() {
        return name;
    }
    
    public int getPriority() {
        return priority;
    }
}