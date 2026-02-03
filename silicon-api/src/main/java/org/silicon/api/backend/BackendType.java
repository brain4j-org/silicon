package org.silicon.api.backend;

/**
 * Supported backend types and their selection priority.
 * <p>
 * Lower priority value means preferred during auto-selection.
 */
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

    /**
     * @return human-readable backend name
     */
    public String formalName() {
        return name;
    }
    
    /**
     * @return lower value means higher priority during auto-selection
     */
    public int priority() {
        return priority;
    }
}
