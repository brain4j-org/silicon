package org.silicon.api.function;

/**
 * Represents a loaded compute module containing one or more functions.
 * <p>
 * Modules are created by a {@link org.silicon.api.device.ComputeContext} and
 * act as a lookup container for compiled kernels.
 */
public interface ComputeModule {
    /**
     * Retrieves a function by name from the module.
     * @param name function name
     * @return compiled function handle
     */
    ComputeFunction getFunction(String name);
}
