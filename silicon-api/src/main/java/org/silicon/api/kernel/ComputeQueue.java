package org.silicon.api.kernel;

import org.silicon.api.function.ComputeFunction;
import org.silicon.api.memory.Freeable;

import java.util.concurrent.CompletableFuture;

public interface ComputeQueue extends Freeable {
    void dispatch(
        ComputeFunction function,
        ComputeSize globalSize,
        ComputeSize groupSize,
        ComputeArgs args
    );
    
    ComputeEvent dispatchAsync(
        ComputeFunction function,
        ComputeSize globalSize,
        ComputeSize groupSize,
        ComputeArgs args
    );

    void await();
}
