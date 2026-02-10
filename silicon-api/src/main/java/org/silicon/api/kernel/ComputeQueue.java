package org.silicon.api.kernel;

import org.silicon.api.function.ComputeFunction;
import org.silicon.api.memory.Freeable;

/**
 * Execution queue for dispatching kernels on a device.
 * <p>
 * Supports synchronous and asynchronous dispatch and can be freed as a resource.
 * Typically used by submitting work and then awaiting completion.
 */
public interface ComputeQueue extends Freeable {
    /**
     * Dispatches a kernel synchronously.
     * Implementations may block until completion.
     * @param function function to execute
     * @param globalSize global grid size
     * @param groupSize work-group size
     * @param args kernel arguments
     */
    void dispatch(
        ComputeFunction function,
        ComputeSize globalSize,
        ComputeSize groupSize,
        ComputeArgs args
    );
    
    /**
     * Dispatches a kernel asynchronously.
     * @param function function to execute
     * @param globalSize global grid size
     * @param groupSize work-group size
     * @param args kernel arguments
     * @return event to synchronize or inspect execution
     */
    ComputeEvent dispatchAsync(
        ComputeFunction function,
        ComputeSize globalSize,
        ComputeSize groupSize,
        ComputeArgs args
    );

    /**
     * Waits for all pending operations in the queue to complete.
     */
    void await();
}
