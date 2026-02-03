package org.silicon.api.function;

/**
 * Represents a compiled kernel/function inside a module.
 * <p>
 * Typically retrieved from a {@link ComputeModule} after loading a module.
 */
public interface ComputeFunction {
    /**
     * @return the maximum work-group size supported for this function
     */
    int maxWorkGroupSize();
}
