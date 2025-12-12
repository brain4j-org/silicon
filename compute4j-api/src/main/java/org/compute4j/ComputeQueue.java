package org.compute4j;

public interface ComputeQueue {
    void dispatch(ComputeFunction function, ComputeSize grid, ComputeSize block, ComputeArgs args);
    void awaitCompletion();
}
