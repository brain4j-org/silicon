package org.silicon.api.backend;

/**
 * Supported backend types and their selection priority.
 * <p>
 * Lower priority value means preferred during auto-selection.
 */
public enum BackendType {

    CUDA("CUDA", "ptx"),
    METAL("METAL", "metal"),
    OPENCL("OPENCL", null);

    private final String formalName, compileTarget;

    BackendType(String formalName, String compileTarget) {
        this.formalName = formalName;
        this.compileTarget = compileTarget;
    }

    public String formalName() {
        return formalName;
    }

    public String compileTarget() {
        if (compileTarget == null) {
            throw new IllegalStateException(formalName + " is not supported yet.");
        }
        return compileTarget;
    }

    public int priority() {
        return ordinal();
    }

}
